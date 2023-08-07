CREATE OR REPLACE PACKAGE wios_migration.installation_appointment_migration AS

  K_LOG_PREFIX CONSTANT VARCHAR2(4000) := 'INSTALLATION_APPOINTMENT_MIGRATION: ';

  K_DEEMED_SOURCE CONSTANT wios_migration.raw_installation_appointments_data.appointment_source%TYPE := 'deemed';
  K_OFFLINE_NOMINATION_SOURCE CONSTANT wios_migration.raw_installation_appointments_data.appointment_source%TYPE := 'nominated';

  TYPE t_installation_lookup_type
    IS TABLE OF NUMBER NOT NULL
    INDEX BY VARCHAR2(4000);

  /**
    Procedure to cleanse the installation appointments for preparation to migrate into WIOS.
  */
  PROCEDURE cleanse_installation_appointments;

END installation_appointment_migration;

CREATE OR REPLACE PACKAGE BODY wios_migration.installation_appointment_migration AS

  /**
    Utility procedure to log an error for a installation appointment migration row.
    @param p_migratable_appointment_id The migratable appointment ID to associate the error too
    @p_error_message The error message to associate with migratable appointment
  */
  PROCEDURE add_migration_error(
    p_migratable_appointment_id IN wios_migration.installation_migration_errors.migratable_appointment_id%TYPE
  , p_error_message IN wios_migration.installation_migration_errors.error_message%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

  BEGIN

    INSERT INTO wios_migration.installation_migration_errors(
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
    Utility procedure to map a possible installation names to an installationfrom DEVUK.

    If an exact match is found then the migratable appointment is updated to have the DEVUK installtion ID.

    If no exact match is found then a row is written to the unmatched_installations table for ease of reporting
    back to NSTA.

    @param p_migratable_appointment_id The migratable appointment ID we are working on
    @param p_installation_name The installation name to attempt to find a matching installation from
    @param p_installation_lookup An associative array of all the known DEVUK installation names to their
                                 associated installation ID.
  */
  PROCEDURE migrate_installation_name_to_installation(
    p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
  , p_installation_name IN wios_migration.raw_installation_appointments_data.installation_name%TYPE
  , p_installation_lookup IN t_installation_lookup_type
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_matched_installation_id NUMBER;

    l_possible_matches  wios_migration.unmatched_installations.possible_matches%TYPE;

  BEGIN

    SAVEPOINT sp_before_installation_name_migrate;

    BEGIN

      l_matched_installation_id := p_installation_lookup(TO_CHAR(p_installation_name));

    EXCEPTION WHEN NO_DATA_FOUND THEN

      l_matched_installation_id := NULL;

    END;

    IF l_matched_installation_id IS NOT NULL THEN

      UPDATE wios_migration.installation_appointments ia
      SET
        ia.installation_id = l_matched_installation_id
      , ia.installation_name = TO_CHAR(p_installation_name)
      WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

      IF SQL%ROWCOUNT != 1 THEN

        raise_application_error(
          -20990
        , 'Failed to update wios_migration.installation_appointments for migratable_appointment_id '
            || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
        );

      END IF;

    ELSE

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Could not find matching DEVUK installation for installation name ' ||
          p_installation_name ||  ' for migratable_appointment_id ' || p_migratable_appointment_id
      );

      /*
       Attempt to find some partial matching installations to suggest to
       NSTA for ease of reporting
      */
      SELECT st.join(stagg(f.facility_name))
      INTO l_possible_matches
      FROM devukmgr.facilities f
      WHERE SUBSTR(f.facility_name, 0, 5) = SUBSTR(TO_CHAR(p_installation_name), 0, 5);

      MERGE INTO wios_migration.unmatched_installations ui
      USING (
        SELECT TO_CHAR(p_installation_name) installation_name
        FROM dual
      ) installation
        ON (TO_CHAR(ui.installation_name) = installation.installation_name)
      WHEN NOT MATCHED THEN
        INSERT (installation_name, possible_matches)
        VALUES(p_installation_name, l_possible_matches);

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_installation_name_migrate;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_installation_name_to_installation for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_installation_name_to_installation;

  /**
    Utility procedure to map a possible operator names to an operators from portal.

    If an exact match is found then the migratable appointment is updated to have the portal operator ID.

    @param p_migratable_appointment_id The migratable appointment ID we are working on
    @param p_operator_name The operator name to attempt to find a matching operator from
  */
  PROCEDURE migrate_operator_name_to_operator(
    p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
  , p_operator_name IN wios_migration.raw_installation_appointments_data.appointed_operator_name%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_matched_operator_id NUMBER;
    l_matched_operator_name wios_migration.installation_appointments.appointed_operator_name%TYPE;

  BEGIN

    SAVEPOINT sp_before_operator_mapping;

    l_matched_operator_id := wios_migration.operator_name_mapping.get_operator_from_name(
      p_operator_name => p_operator_name
    );

    IF l_matched_operator_id IS NOT NULL THEN

      SELECT xou.name
      INTO l_matched_operator_name
      FROM decmgr.xview_organisation_units xou
      WHERE xou.organ_id = l_matched_operator_id;

      UPDATE wios_migration.installation_appointments ia
      SET
        ia.appointed_operator_id = l_matched_operator_id
      , ia.appointed_operator_name = l_matched_operator_name
      WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

      IF SQL%ROWCOUNT != 1 THEN

        raise_application_error(
          -20990
        , 'Failed to update wios_migration.installation_appointments operator data for migratable_appointment_id '
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
    p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
  , p_appointment_from_date IN wios_migration.raw_installation_appointments_data.responsible_from_date%TYPE
  , p_appointment_to_date IN wios_migration.raw_installation_appointments_data.responsible_to_date%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    K_DEEMED_DATE CONSTANT DATE := TO_DATE('19/07/2015', 'DD/MM/YYYY');
    K_CURRENT_DATE CONSTANT DATE := TRUNC(SYSDATE);

    l_appointment_from_date wios_migration.installation_appointments.responsible_from_date%TYPE;
    l_appointment_to_date wios_migration.installation_appointments.responsible_to_date%TYPE;

    FUNCTION convert_to_date(
      p_date_as_string IN VARCHAR2
    , p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
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

    IF l_appointment_from_date IS NOT NULL AND l_appointment_from_date < K_DEEMED_DATE THEN

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Appointment from date, ' || l_appointment_from_date || ', is before the deemed date'
      );

    ELSIF l_appointment_from_date IS NOT NULL AND l_appointment_from_date > K_CURRENT_DATE THEN

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Appointment from date, ' || l_appointment_from_date || ', is in the future'
      );

    END IF;

    -- determine if we are using the end date from the spreadsheet or need to
    -- determine the end date from any subsequent appointment
    IF p_appointment_to_date IS NOT NULL THEN

      l_appointment_to_date := convert_to_date(
        p_date_as_string => p_appointment_to_date
      , p_migratable_appointment_id => p_migratable_appointment_id
      );

    ELSE

      DECLARE

        l_inferred_to_date_as_string VARCHAR2(4000);
        l_installation_name VARCHAR2(4000);

      BEGIN

        SELECT TO_CHAR(i.installation_name)
        INTO l_installation_name
        FROM wios_migration.raw_installation_appointments_data i
        WHERE i.migratable_appointment_id = p_migratable_appointment_id;

        SELECT x.next_appointment_from_date
        INTO l_inferred_to_date_as_string
        FROM (
          SELECT
            i.migratable_appointment_id
          , LEAD(TO_CHAR(i.responsible_from_date))
              OVER(
                PARTITION BY TO_CHAR(i.installation_name)
                ORDER BY TO_DATE(TO_CHAR(i.responsible_from_date), 'DD/MM/YYYY')
              ) next_appointment_from_date
          FROM wios_migration.raw_installation_appointments_data i
          WHERE TO_CHAR(i.installation_name) = l_installation_name
        ) x
        WHERE x.migratable_appointment_id = p_migratable_appointment_id;

        l_appointment_to_date := convert_to_date(
          p_date_as_string => l_inferred_to_date_as_string
        , p_migratable_appointment_id => p_migratable_appointment_id
        );

      EXCEPTION WHEN OTHERS THEN

        l_appointment_to_date := NULL;

        add_migration_error(
          p_migratable_appointment_id => p_migratable_appointment_id
        , p_error_message => 'Could not infer responsible to date' || CHR(10) ||
            CHR(10) || SQLERRM || CHR(10) || CHR(10) || dbms_utility.format_error_backtrace()
        );

      END;

    END IF;

    IF l_appointment_to_date IS NOT NULL AND l_appointment_to_date > K_CURRENT_DATE THEN

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Appointment to date, ' || l_appointment_to_date || ', is in the future'
      );

    ELSIF
      l_appointment_to_date IS NOT NULL AND
      l_appointment_from_date IS NOT NULL AND
      l_appointment_to_date < l_appointment_from_date
    THEN

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Appointment to date, ' || l_appointment_to_date || ', is before the appointment from date, ' || l_appointment_from_date
      );

    END IF;

    UPDATE wios_migration.installation_appointments ia
    SET
      ia.responsible_from_date = l_appointment_from_date
    , ia.responsible_to_date = l_appointment_to_date
    WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.installation_appointments responsible dates for migratable_appointment_id '
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

  /**
    Procedure to migrate the phases for an appointment.

    @param p_migratable_appointment_id The ID of the appointment we are migrating
    @param p_is_development_phase Indication of if the appointment is for development
    @param p_is_decommissioning_phase Indication of if the appointment is for decommissioning

  */
  PROCEDURE migrate_appointment_phases(
    p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
  , p_is_development_phase IN wios_migration.raw_installation_appointments_data.is_development_phase%TYPE
  , p_is_decommissioning_phase IN wios_migration.raw_installation_appointments_data.is_decommissioning_phase%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_is_development_phase wios_migration.installation_appointments.is_development_phase%TYPE;
    l_is_decommissioning_phase wios_migration.installation_appointments.is_decommissioning_phase%TYPE;

    FUNCTION is_for_phase(
      p_is_for_phase_text IN VARCHAR2
    ) RETURN NUMBER
    IS

      K_IS_PHASE_TEXT CONSTANT VARCHAR2(3) := 'yes';
      K_NOT_PHASE_TEXT CONSTANT VARCHAR2(3) := 'no';

    BEGIN

      IF LOWER(p_is_for_phase_text) = K_IS_PHASE_TEXT THEN

        RETURN 1;

      ELSIF LOWER(p_is_for_phase_text) = K_NOT_PHASE_TEXT THEN

        RETURN 0;

      ELSE

        add_migration_error(
          p_migratable_appointment_id => p_migratable_appointment_id
        , p_error_message => 'Unexpected phase value "' || NVL(p_is_for_phase_text, 'NULL') ||  '" in migrate_appointment_phases for migratable_appointment_id '
            || p_migratable_appointment_id
        );

        RETURN NULL;

      END IF;

    END is_for_phase;

  BEGIN

    SAVEPOINT sp_before_phase_mapping;

    l_is_development_phase := is_for_phase(
      p_is_for_phase_text => p_is_development_phase
    );

    l_is_decommissioning_phase := is_for_phase(
      p_is_for_phase_text => p_is_decommissioning_phase
    );

    UPDATE wios_migration.installation_appointments ia
    SET
      ia.is_development_phase = l_is_development_phase
    , ia.is_decommissioning_phase = l_is_decommissioning_phase
    WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.installation_appointments phases for migratable_appointment_id '
          || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
      );

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_phase_mapping;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_appointment_phases for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_appointment_phases;

  /**
    Procedure to migrate the appointment source.
    @param p_migratable_appointment_id The appointment ID we are working on
    @param p_appointment_source The appointment source to migrate
  */
  PROCEDURE migrate_appointment_source(
    p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
  , p_appointment_source IN wios_migration.raw_installation_appointments_data.appointment_source%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_appointment_source wios_migration.installation_appointments.appointment_source%TYPE;

  BEGIN

    SAVEPOINT sp_before_appointment_source_mapping;

    IF LOWER(p_appointment_source) = K_DEEMED_SOURCE THEN

      l_appointment_source := 'DEEMED';

    ELSIF LOWER(p_appointment_source) = K_OFFLINE_NOMINATION_SOURCE THEN

      l_appointment_source := 'OFFLINE_NOMINATION';

    ELSE

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Unexpected installation appointment source: ' || p_appointment_source
      );

    END IF;

    UPDATE wios_migration.installation_appointments ia
    SET ia.appointment_source = l_appointment_source
    WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.installation_appointments phases for migratable_appointment_id '
          || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
      );

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_appointment_source_mapping;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_appointment_source for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_appointment_source;

  /**
    Procedure to migrate the legacy nomination references.
    @param p_migratable_appointment_id The appointment ID we are working on
    @param p_legacy_nomination_reference The legacy nomination reference
  */
  PROCEDURE migrate_legacy_nomination_reference(
    p_migratable_appointment_id IN wios_migration.raw_installation_appointments_data.migratable_appointment_id%TYPE
  , p_legacy_nomination_reference IN wios_migration.raw_installation_appointments_data.legacy_nomination_reference%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

  BEGIN

    SAVEPOINT sp_before_nomination_mapping;

    UPDATE wios_migration.installation_appointments ia
    SET ia.legacy_nomination_reference = p_legacy_nomination_reference
    WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.installation_appointments nomination reference for migratable_appointment_id '
          || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
      );

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_nomination_mapping;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_legacy_nomination_reference for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_legacy_nomination_reference;

  PROCEDURE cleanse_installation_appointments
  IS

    t_installation_lookup t_installation_lookup_type;

  BEGIN

    -- create a lookup of installation names to installation IDs
    FOR devuk_installation IN (
      SELECT
        f.facility_name name
      , f.identifier id
      FROM devukmgr.facilities f
    )
    LOOP

      t_installation_lookup(devuk_installation.name) := devuk_installation.id;

    END LOOP;

    FOR migratable_installation_appointment IN (
      SELECT
        iad.migratable_appointment_id
      , iad.installation_name
      , iad.appointed_operator_name
      , LOWER(iad.appointment_source) appointment_source
      , iad.is_decommissioning_phase
      , iad.is_development_phase
      , iad.legacy_nomination_reference
      , iad.responsible_from_date
      , iad.responsible_to_date
      , ROWNUM row_index
      , COUNT(*) OVER() total_rows
      FROM wios_migration.raw_installation_appointments_data iad
    )
    LOOP

      logger.debug(
        K_LOG_PREFIX || 'Starting installation appointment migration for migratable_appointment_id ' ||
        migratable_installation_appointment.migratable_appointment_id || ' (' ||
        migratable_installation_appointment.row_index || '/' || migratable_installation_appointment.total_rows || ')'
      );

      INSERT INTO wios_migration.installation_appointments(migratable_appointment_id)
      VALUES(migratable_installation_appointment.migratable_appointment_id);

      COMMIT;

      BEGIN

        SAVEPOINT sp_before_appointment_migration;

        migrate_installation_name_to_installation(
          p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
        , p_installation_name => migratable_installation_appointment.installation_name
        , p_installation_lookup => t_installation_lookup
        );

        migrate_operator_name_to_operator(
          p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
        , p_operator_name => migratable_installation_appointment.appointed_operator_name
        );

        migrate_appointment_dates(
          p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
        , p_appointment_from_date => migratable_installation_appointment.responsible_from_date
        , p_appointment_to_date => migratable_installation_appointment.responsible_to_date
        );


        migrate_appointment_phases(
          p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
        , p_is_development_phase => migratable_installation_appointment.is_development_phase
        , p_is_decommissioning_phase => migratable_installation_appointment.is_decommissioning_phase
        );

        migrate_appointment_source(
          p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
        , p_appointment_source => migratable_installation_appointment.appointment_source
        );

        IF LOWER(migratable_installation_appointment.appointment_source) = K_OFFLINE_NOMINATION_SOURCE AND migratable_installation_appointment.legacy_nomination_reference IS NOT NULL THEN

          migrate_legacy_nomination_reference(
            p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
          , p_legacy_nomination_reference => migratable_installation_appointment.legacy_nomination_reference
          );

        ELSIF LOWER(migratable_installation_appointment.appointment_source) != K_OFFLINE_NOMINATION_SOURCE AND migratable_installation_appointment.legacy_nomination_reference IS NOT NULL THEN

          add_migration_error(
            p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
          , p_error_message => 'Legacy nomination reference provided for non ' || K_OFFLINE_NOMINATION_SOURCE || ' appointment for migratable_appointment_id ' || migratable_installation_appointment.migratable_appointment_id
          );

        END IF;

        logger.debug(
          K_LOG_PREFIX || 'Finished installation appointment migration for migratable_appointment_id ' ||
          migratable_installation_appointment.migratable_appointment_id || ' (' ||
          migratable_installation_appointment.row_index || '/' || migratable_installation_appointment.total_rows || ')'
        );

        COMMIT;

      EXCEPTION WHEN OTHERS THEN

        ROLLBACK TO SAVEPOINT sp_before_appointment_migration;

        add_migration_error(
          p_migratable_appointment_id => migratable_installation_appointment.migratable_appointment_id
        , p_error_message => 'Unexpected error in cleanse_installation_appointments: ' || CHR(10) || CHR(10) ||
            SQLERRM || CHR(10) || CHR(10) || dbms_utility.format_error_backtrace()
        );

        COMMIT;

      END;

    END LOOP;

    FOR appointment_with_inconsistent_date IN(
      SELECT *
      FROM (
        SELECT
          ia.migratable_appointment_id
        , ia.responsible_from_date
        , ia.responsible_to_date
        , LEAD(ia.responsible_from_date) OVER(PARTITION BY ia.installation_id ORDER BY ia.responsible_from_date) next_responsible_from_date
        FROM wios_migration.installation_appointments ia
      ) t
      WHERE t.responsible_to_date != t.next_responsible_from_date
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => appointment_with_inconsistent_date.migratable_appointment_id
      , p_error_message => 'The next appointment for this asset does not start from the previous appointments to date.'
      );

    END LOOP;

    FOR asset_with_more_than_one_deemed_appointment IN (
      WITH appointments_by_source AS (
        SELECT ia.installation_id, ia.appointment_source, COUNT(*) count
        FROM wios_migration.installation_appointments ia
        GROUP BY ia.installation_id, ia.appointment_source
      )
      SELECT ia.migratable_appointment_id
      FROM wios_migration.installation_appointments ia
      LEFT JOIN appointments_by_source abs ON abs.installation_id = ia.installation_id AND abs.appointment_source = 'DEEMED'
      WHERE COALESCE(abs.count, 0) > 1
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => asset_with_more_than_one_deemed_appointment.migratable_appointment_id
      , p_error_message => 'Asset has more than one deemed appointment'
      );

    END LOOP;

    FOR asset_with_more_than_one_current_appointment IN(
      SELECT ia.migratable_appointment_id
      FROM wios_migration.installation_appointments ia
      WHERE ia.installation_id IN (
        SELECT current_appointment.installation_id
        FROM wios_migration.installation_appointments current_appointment
        WHERE current_appointment.responsible_to_date IS NULL
        GROUP BY current_appointment.installation_id
        HAVING COUNT(*) > 1
      )
      AND ia.responsible_to_date IS NULL
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => asset_with_more_than_one_current_appointment.migratable_appointment_id
      , p_error_message => 'Asset has more than one active appointment'
      );

    END LOOP;

  END cleanse_installation_appointments;

END installation_appointment_migration;