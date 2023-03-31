CREATE OR REPLACE PACKAGE wios_migration.wellbore_appointment_migration AS

  K_LOG_PREFIX CONSTANT VARCHAR2(4000) := 'WELLBORE_APPOINTMENT_MIGRATION: ';

  TYPE t_wellbore_lookup_type
    IS TABLE OF NUMBER NOT NULL
    INDEX BY VARCHAR2(4000);

  /**
    Procedure to cleanse the wellbore appointments for preparation to migrate into WIOS.
  */
  PROCEDURE cleanse_wellbore_appointments;

END wellbore_appointment_migration;

CREATE OR REPLACE PACKAGE BODY wios_migration.wellbore_appointment_migration AS

  /**
    Utility procedure to log an error for a wellbore appointment migration row.
    @param p_migratable_appointment_id The migratable appointment ID to associate the error too
    @p_error_message The error message to associate with migratable appointment
  */
  PROCEDURE add_migration_error(
    p_migratable_appointment_id IN wios_migration.wellbore_migration_errors.migratable_appointment_id%TYPE
  , p_error_message IN wios_migration.wellbore_migration_errors.error_message%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

  BEGIN

    INSERT INTO wios_migration.wellbore_migration_errors(
      migratable_appointment_id
    , error_message
    )
    VALUES(
      p_migratable_appointment_id
    , p_error_message
    );

    COMMIT;

  END add_migration_error;

  /**
    Utility procedure to map a possible wellbore registration number to a wellbore from WONS.

    If an exact match is found then the migratable appointment is updated to have the WONS wellbore ID.

    If no exact match is found then a row is written to the unmatched_wellbores table for ease of reporting
    back to NSTA.

    @param p_migratable_appointment_id The migratable appointment ID we are working on
    @param p_wellbore_registration_number The wellbore registration number to attempt to find a matching wellbore from
    @param p_wellbore_lookup An associative array of all the known WONS wellbore registration numbers to their
                             associated wellbore ID.
  */
  PROCEDURE migrate_registration_number_to_wellbore(
    p_migratable_appointment_id IN wios_migration.raw_wellbore_appointments_data.migratable_appointment_id%TYPE
  , p_wellbore_registration_number IN wios_migration.raw_wellbore_appointments_data.wellbore_registration_number%TYPE
  , p_wellbore_lookup IN t_wellbore_lookup_type
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_matched_wellbore_id NUMBER;

    l_possible_matches  wios_migration.unmatched_wellbores.possible_matches%TYPE;

  BEGIN

    SAVEPOINT sp_before_registration_number_migrate;

    BEGIN

      l_matched_wellbore_id := p_wellbore_lookup(TO_CHAR(p_wellbore_registration_number));

    EXCEPTION WHEN NO_DATA_FOUND THEN

      l_matched_wellbore_id := NULL;

    END;

    IF l_matched_wellbore_id IS NOT NULL THEN

      UPDATE wios_migration.wellbore_appointments wa
      SET
        wa.wellbore_id = l_matched_wellbore_id
      , wa.wellbore_registration_number = TO_CHAR(p_wellbore_registration_number)
      WHERE wa.migratable_appointment_id = p_migratable_appointment_id;

      IF SQL%ROWCOUNT != 1 THEN

        raise_application_error(
          -20990
        , 'Failed to update wios_migration.wellbore_appointments for migratable_appointment_id '
            || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
        );

      END IF;

    ELSE

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Could not find matching WONS wellbore for registration number ' ||
          p_wellbore_registration_number ||  ' for migratable_appointment_id ' || p_migratable_appointment_id
      );

      /*
       replace spaces and dashes in the well registration number to see
       if any wellbores match without those characters and write the possible
       matches to the table. These are the characters that are likely to have been
       entered incorrectly.
      */
      SELECT st.join(stagg(xwws.well_registration_no))
      INTO l_possible_matches
      FROM wellmgr.xview_wons_wellbore_search xwws
      WHERE REGEXP_REPLACE(xwws.well_registration_no, ' |-') = REGEXP_REPLACE(TO_CHAR(p_wellbore_registration_number), ' |-')
      AND xwws.status_control = 'C';

      MERGE INTO wios_migration.unmatched_wellbores uw
      USING (
        SELECT TO_CHAR(p_wellbore_registration_number) wellbore_registration_number
        FROM dual
      ) wellbore
        ON (TO_CHAR(uw.wellbore_registration_number) = wellbore.wellbore_registration_number)
      WHEN NOT MATCHED THEN
        INSERT (wellbore_registration_number, possible_matches)
        VALUES(p_wellbore_registration_number, l_possible_matches);

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_registration_number_migrate;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_registration_number_to_wellbore for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_registration_number_to_wellbore;

  /**
    Utility procedure to map a possible operator names to an operators from portal.

    If an exact match is found then the migratable appointment is updated to have the portal operator ID.

    @param p_migratable_appointment_id The migratable appointment ID we are working on
    @param p_operator_name The operator name to attempt to find a matching operator from
    @param p_operator_lookup An associative array of all the known portal operator names to their
                             associated operator ID.
  */
  PROCEDURE migrate_operator_name_to_operator(
    p_migratable_appointment_id IN wios_migration.raw_wellbore_appointments_data.migratable_appointment_id%TYPE
  , p_operator_name IN wios_migration.raw_wellbore_appointments_data.appointed_operator_name%TYPE
  , p_operator_lookup IN wios_migration.operator_name_mapping.t_operator_lookup_type
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_matched_operator_id NUMBER;

  BEGIN

    SAVEPOINT sp_before_operator_mapping;

    l_matched_operator_id := wios_migration.operator_name_mapping.get_operator_from_name(
      p_operator_name => p_operator_name
    , p_operator_lookup_type => p_operator_lookup
    );

    IF l_matched_operator_id IS NOT NULL THEN

      UPDATE wios_migration.wellbore_appointments ia
      SET
        ia.appointed_operator_id = l_matched_operator_id
      , ia.appointed_operator_name = TO_CHAR(p_operator_name)
      WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

      IF SQL%ROWCOUNT != 1 THEN

        raise_application_error(
          -20990
        , 'Failed to update wios_migration.wellbore_appointments operator data for migratable_appointment_id '
            || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
        );

      END IF;

    ELSE

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Could not find matching operator for operator name ' ||
          p_operator_name ||  ' for migratable_appointment_id ' || p_migratable_appointment_id
      );

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_operator_mapping;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_operator_name_to_operator for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_operator_name_to_operator;

  /**
    Procedure to migrate the appointment to and from dates for a wellbore

    @param p_migratable_appointment_id The ID of the appointment we are working with
    @param p_appointment_from_date The date the appointment is valid from
    @param p_appointment_to_date The date the appointment is valid to
  */
  PROCEDURE migrate_appointment_dates(
    p_migratable_appointment_id IN wios_migration.raw_wellbore_appointments_data.migratable_appointment_id%TYPE
  , p_appointment_from_date IN wios_migration.raw_wellbore_appointments_data.responsible_from_date%TYPE
  , p_appointment_to_date IN wios_migration.raw_wellbore_appointments_data.responsible_to_date%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_appointment_from_date wios_migration.wellbore_appointments.responsible_from_date%TYPE;
    l_appointment_to_date wios_migration.wellbore_appointments.responsible_to_date%TYPE;

    FUNCTION convert_to_date(
      p_date_as_string IN VARCHAR2
    , p_migratable_appointment_id IN wios_migration.raw_wellbore_appointments_data.migratable_appointment_id%TYPE
    ) RETURN DATE
    IS

     BEGIN

      RETURN TO_DATE(p_date_as_string, 'DD/MM/YYYY');

    EXCEPTION WHEN OTHERS THEN

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Unable to convert responsible date "' || p_date_as_string || '" to date for migratable_appointment_id ' ||
          p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
          CHR(10) || dbms_utility.format_error_backtrace()
      );

      RETURN NULL;

    END convert_to_date;

  BEGIN

    SAVEPOINT sp_before_date_mapping;

    l_appointment_from_date := convert_to_date(
      p_date_as_string => p_appointment_from_date
    , p_migratable_appointment_id => p_migratable_appointment_id
    );

    l_appointment_to_date := convert_to_date(
      p_date_as_string => p_appointment_to_date
    , p_migratable_appointment_id => p_migratable_appointment_id
    );

    UPDATE wios_migration.wellbore_appointments wa
    SET
      wa.responsible_from_date = l_appointment_from_date
    , wa.responsible_to_date = l_appointment_to_date
    WHERE wa.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.wellbore_appointments responsible dates for migratable_appointment_id '
          || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
      );

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_date_mapping;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_appointment_dates for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_appointment_dates;

  PROCEDURE cleanse_wellbore_appointments
  IS

    t_wellbore_lookup t_wellbore_lookup_type;
    t_operator_lookup wios_migration.operator_name_mapping.t_operator_lookup_type;

  BEGIN

    -- create a lookup of wellbore registration numbers to wellbore IDs
    FOR wons_wellbore IN (
      SELECT
        xwws.well_registration_no
      , xwws.w_id id
      FROM wellmgr.xview_wons_wellbore_search xwws
      WHERE xwws.well_registration_no IS NOT NULL
      AND xwws.status_control = 'C'
    )
    LOOP

      t_wellbore_lookup(wons_wellbore.well_registration_no) := wons_wellbore.id;

    END LOOP;

    t_operator_lookup := wios_migration.operator_name_mapping.initialise_operator_lookup();

    FOR migratable_wellbore_appointment IN (
      SELECT
        wad.*
      , ROWNUM row_index
      , COUNT(*) OVER() total_rows
      FROM wios_migration.raw_wellbore_appointments_data wad
    )
    LOOP

      logger.debug(
        K_LOG_PREFIX || 'Starting wellbore appointment migration for migratable_appointment_id ' ||
        migratable_wellbore_appointment.migratable_appointment_id || ' (' ||
        migratable_wellbore_appointment.row_index || '/' || migratable_wellbore_appointment.total_rows || ')'
      );

      INSERT INTO wios_migration.wellbore_appointments(migratable_appointment_id)
      VALUES(migratable_wellbore_appointment.migratable_appointment_id);

      COMMIT;

      BEGIN

        SAVEPOINT sp_before_appointment_migration;

        migrate_registration_number_to_wellbore(
          p_migratable_appointment_id => migratable_wellbore_appointment.migratable_appointment_id
        , p_wellbore_registration_number => migratable_wellbore_appointment.wellbore_registration_number
        , p_wellbore_lookup => t_wellbore_lookup
        );

        migrate_operator_name_to_operator(
          p_migratable_appointment_id => migratable_wellbore_appointment.migratable_appointment_id
        , p_operator_name => migratable_wellbore_appointment.appointed_operator_name
        , p_operator_lookup => t_operator_lookup
        );

        migrate_appointment_dates(
          p_migratable_appointment_id => migratable_wellbore_appointment.migratable_appointment_id
        , p_appointment_from_date => migratable_wellbore_appointment.responsible_from_date
        , p_appointment_to_date => migratable_wellbore_appointment.responsible_to_date
        );

        logger.debug(
          K_LOG_PREFIX || 'Finished wellbore appointment migration for migratable_appointment_id ' ||
          migratable_wellbore_appointment.migratable_appointment_id || ' (' ||
          migratable_wellbore_appointment.row_index || '/' || migratable_wellbore_appointment.total_rows || ')'
        );

        COMMIT;

      EXCEPTION WHEN OTHERS THEN

        ROLLBACK TO SAVEPOINT sp_before_appointment_migration;

        add_migration_error(
          p_migratable_appointment_id => migratable_wellbore_appointment.migratable_appointment_id
        , p_error_message => 'Unexpected error in cleanse_wellbore_appointments: ' || CHR(10) || CHR(10) ||
            SQLERRM || CHR(10) || CHR(10) || dbms_utility.format_error_backtrace()
        );

        COMMIT;

      END;

    END LOOP;

  END cleanse_wellbore_appointments;

END wellbore_appointment_migration;