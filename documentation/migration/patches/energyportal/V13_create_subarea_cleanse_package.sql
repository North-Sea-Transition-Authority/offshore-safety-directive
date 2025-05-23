CREATE OR REPLACE PACKAGE wios_migration.subarea_appointment_migration AS

  K_LOG_PREFIX CONSTANT VARCHAR2(4000) := 'SUBAREA_APPOINTMENT_MIGRATION: ';

  K_DEEMED_SOURCE CONSTANT wios_migration.raw_subarea_appointments_data.appointment_source%TYPE := 'deemed';
  K_OFFLINE_NOMINATION_SOURCE CONSTANT wios_migration.raw_subarea_appointments_data.appointment_source%TYPE := 'nominated';

  TYPE t_subarea_lookup_type
    IS TABLE OF VARCHAR2(4000) NOT NULL
    INDEX BY VARCHAR2(4000);

  /**
    Procedure to cleanse the subarea appointments for preparation to migrate into WIOS.
  */
  PROCEDURE cleanse_subarea_appointments;

  /**
    Function to create a single string representing a subarea identifier to be used in this
    package.

    @param p_licence_type The licence type, e.g. P of the licence the subarea is in
    @param p_licence_number The number of the licence the subarea is in
    @param p_block_reference The reference of the block the subarea is in
    @param p_subarea_name The name of the subarea
    @return a single string identifier for the subarea in the following format,
            p_licence_type || p licence_number || '-' || p_block_reference || '-' || p_subarea_name
  */
  FUNCTION create_subarea_identifier_sql(
    p_licence_type IN VARCHAR2
  , p_licence_number IN NUMBER
  , p_block_reference IN VARCHAR2
  , p_subarea_name IN VARCHAR2
  ) RETURN VARCHAR2 DETERMINISTIC;

END subarea_appointment_migration;

CREATE OR REPLACE PACKAGE BODY wios_migration.subarea_appointment_migration AS

  /**
    Utility procedure to log an error for a subarea appointment migration row.
    @param p_migratable_appointment_id The migratable appointment ID to associate the error too
    @p_error_message The error message to associate with migratable appointment
  */
  PROCEDURE add_migration_error(
    p_migratable_appointment_id IN wios_migration.subarea_migration_errors.migratable_appointment_id%TYPE
  , p_error_message IN wios_migration.subarea_migration_errors.error_message%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

  BEGIN

    INSERT INTO wios_migration.subarea_migration_errors(
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
    Utility procedure to log a migration warning for a given migratable appointment

    @param p_migratable_appointment_id The ID of the appointment being migrated
    @param p_warning_message The warning message to log
   */
  PROCEDURE add_migration_warning (
    p_migratable_appointment_id IN wios_migration.migration_warnings.migratable_appointment_id%TYPE
  , p_warning_message IN wios_migration.migration_warnings.warning_message%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

  BEGIN

    INSERT INTO wios_migration.migration_warnings(
      migratable_appointment_id
    , warning_message
    )
    VALUES(
      p_migratable_appointment_id
    , p_warning_message
    );

    COMMIT;

  END add_migration_warning;

  FUNCTION create_subarea_identifier_sql(
    p_licence_type IN VARCHAR2
  , p_licence_number IN NUMBER
  , p_block_reference IN VARCHAR2
  , p_subarea_name IN VARCHAR2
  ) RETURN VARCHAR2 DETERMINISTIC
  IS

    l_licence_id pedmgr.ped_licence_master.id%TYPE;

  BEGIN

    SELECT plm.id
    INTO l_licence_id
    FROM pedmgr.ped_licence_master plm
    WHERE plm.licence_type = p_licence_type
    AND plm.licence_no = p_licence_number;

    RETURN pedmgr.ped_utils.get_portal_api_subarea_id(
      p_subarea_name => p_subarea_name
    , p_block_reference => p_block_reference
    , p_licence_master_id => l_licence_id
    );

  END create_subarea_identifier_sql;

  /**
    Function to create a single string representing a subarea identifier to be used in this
    package.

    @param p_migratable_appointment_id The migratable appointment ID being processed
    @param p_licence_type The licence type, e.g. P of the licence the subarea is in
    @param p_licence_number The number of the licence the subarea is in
    @param p_block_reference The reference of the block the subarea is in
    @param p_subarea_name The name of the subarea
    @return a single string identifier for the subarea which is a hash of the
            subarea name, block reference and licence ID
  */
  FUNCTION create_subarea_identifier(
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_licence_type IN wios_migration.raw_subarea_appointments_data.licence_type%TYPE
  , p_licence_number IN wios_migration.raw_subarea_appointments_data.licence_number%TYPE
  , p_block_reference IN wios_migration.raw_subarea_appointments_data.block_reference%TYPE
  , p_subarea_name IN wios_migration.raw_subarea_appointments_data.subarea_name%TYPE
  ) RETURN VARCHAR2 DETERMINISTIC
  IS

  BEGIN

    RETURN create_subarea_identifier_sql(
      p_licence_type => TO_CHAR(p_licence_type)
    , p_licence_number => TO_NUMBER(p_licence_number)
    , p_block_reference => TO_CHAR(p_block_reference)
    , p_subarea_name => TO_CHAR(p_subarea_name)
    );

  EXCEPTION

    WHEN INVALID_NUMBER OR VALUE_ERROR THEN

      raise_application_error(
        -20999
      , 'Could not convert licence number "' || p_licence_number ||
          '" into a valid number for migratable_appointment_id ' || p_migratable_appointment_id
      );

    WHEN OTHERS THEN

      raise_application_error(
        -20999
      , 'Unexpected error in create_subarea_identifier for migratable_appointment_id ' ||
          p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
          CHR(10) || dbms_utility.format_error_backtrace()
      );

  END create_subarea_identifier;

  /**
    Utility procedure to map a possible subarea identifier to a subarea from PEARS.

    If an exact match is found then the migratable appointment is updated to have the PEARS subarea ID.

    If no exact match is found then a row is written to the unmatched_subareas table for ease of reporting
    back to NSTA.

    @param p_migratable_appointment_id The migratable appointment ID we are working on
    @param p_subarea_registration_number The subarea registration number to attempt to find a matching subarea from
    @param p_subarea_lookup An associative array of all the known PEARS subarea identifiers to their
                             associated subarea ID.
  */
  PROCEDURE migrate_subarea_reference_to_subarea(
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_licence_type IN wios_migration.raw_subarea_appointments_data.licence_type%TYPE
  , p_licence_number IN wios_migration.raw_subarea_appointments_data.licence_number%TYPE
  , p_block_reference IN wios_migration.raw_subarea_appointments_data.block_reference%TYPE
  , p_subarea_name IN wios_migration.raw_subarea_appointments_data.subarea_name%TYPE
  , p_subarea_lookup IN t_subarea_lookup_type
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_subarea_identifier VARCHAR2(4000);

    l_matched_subarea_id VARCHAR2(4000);

    l_possible_matches  wios_migration.unmatched_subareas.possible_matches%TYPE;

  BEGIN

    SAVEPOINT sp_before_subarea_name_migrate;

    BEGIN

      l_subarea_identifier := create_subarea_identifier(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_licence_type => p_licence_type
      , p_licence_number => p_licence_number
      , p_block_reference => p_block_reference
      , p_subarea_name => p_subarea_name
      );

      l_matched_subarea_id := p_subarea_lookup(l_subarea_identifier);

    EXCEPTION WHEN NO_DATA_FOUND THEN

      l_matched_subarea_id := NULL;

    END;

    IF l_matched_subarea_id IS NOT NULL THEN

      UPDATE wios_migration.subarea_appointments sa
      SET
        sa.subarea_id = l_matched_subarea_id
      , sa.subarea_reference = p_licence_type || p_licence_number || ' ' ||p_block_reference || ' ' || p_subarea_name
      WHERE sa.migratable_appointment_id = p_migratable_appointment_id;

      IF SQL%ROWCOUNT != 1 THEN

        raise_application_error(
          -20990
        , 'Failed to update wios_migration.subarea_appointments for migratable_appointment_id '
            || p_migratable_appointment_id || '. Expected to update 1 row but attempted to update ' || SQL%ROWCOUNT
        );

      END IF;

    ELSE

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Could not find matching PEARS subarea for licence type "' ||
          p_licence_type ||  '", licence number "' || p_licence_number || '", block reference "' ||
          p_block_reference || '" and subarea name "' || p_subarea_name || '" for migratable_appointment_id ' || p_migratable_appointment_id
      );

      SELECT st.join(stagg(DISTINCT esv.short_name))
      INTO l_possible_matches
      FROM pedmgr.epa_subareas_mv esv
      WHERE esv.licence_reference = TO_CHAR(p_licence_type) || TO_CHAR(p_licence_number)
      AND esv.block_reference = TO_CHAR(p_block_reference);

      MERGE INTO wios_migration.unmatched_subareas us
      USING (
        SELECT
          TO_CHAR(p_licence_type) licence_type
        , TO_CHAR(p_licence_number) licence_number
        , TO_CHAR(p_block_reference) block_reference
        FROM dual
      ) subarea
        ON (
          TO_CHAR(us.licence_type) = subarea.licence_type AND
          TO_CHAR(us.licence_number) = subarea.licence_number AND
          TO_CHAR(us.block_reference) = subarea.block_reference
        )
      WHEN NOT MATCHED THEN
        INSERT (licence_type, licence_number, block_reference, possible_matches)
        VALUES(p_licence_type, p_licence_number, p_block_reference, l_possible_matches);

    END IF;

    COMMIT;

  EXCEPTION WHEN OTHERS THEN

    ROLLBACK TO SAVEPOINT sp_before_subarea_name_migrate;

    add_migration_error(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_error_message => 'Unexpected error in migrate_subarea_reference_to_subarea for migratable_appointment_id ' ||
        p_migratable_appointment_id || CHR(10) || CHR(10) || SQLERRM || CHR(10) ||
        CHR(10) || dbms_utility.format_error_backtrace()
    );

    COMMIT;

  END migrate_subarea_reference_to_subarea;

  /**
    Utility procedure to map a possible operator names to an operators from portal.

    If an exact match is found then the migratable appointment is updated to have the portal operator ID.

    @param p_migratable_appointment_id The migratable appointment ID we are working on
    @param p_operator_name The operator name to attempt to find a matching operator from
  */
  PROCEDURE migrate_operator_name_to_operator(
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_operator_name IN wios_migration.raw_subarea_appointments_data.appointed_operator_name%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_matched_operator_id NUMBER;
    l_matched_operator_name wios_migration.subarea_appointments.appointed_operator_name%TYPE;

  BEGIN

    SAVEPOINT sp_before_operator_mapping;

    l_matched_operator_id := wios_migration.operator_name_mapping.get_operator_from_name(
      p_migratable_appointment_id => p_migratable_appointment_id
    , p_operator_name => p_operator_name
    );

    IF l_matched_operator_id IS NOT NULL THEN

      SELECT xou.name
      INTO l_matched_operator_name
      FROM decmgr.xview_organisation_units xou
      WHERE xou.organ_id = l_matched_operator_id;

      UPDATE wios_migration.subarea_appointments ia
      SET
        ia.appointed_operator_id = l_matched_operator_id
      , ia.appointed_operator_name = l_matched_operator_name
      WHERE ia.migratable_appointment_id = p_migratable_appointment_id;

      IF SQL%ROWCOUNT != 1 THEN

        raise_application_error(
          -20990
        , 'Failed to update wios_migration.subarea_appointments operator data for migratable_appointment_id '
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
    Procedure to migrate the appointment to and from dates for a subarea

    @param p_migratable_appointment_id The ID of the appointment we are working with
    @param p_appointment_from_date The date the appointment is valid from
    @param p_appointment_to_date The date the appointment is valid to
  */
  PROCEDURE migrate_appointment_dates(
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_appointment_from_date IN wios_migration.raw_subarea_appointments_data.responsible_from_date%TYPE
  , p_appointment_to_date IN wios_migration.raw_subarea_appointments_data.responsible_to_date%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    K_DEEMED_DATE CONSTANT DATE := TO_DATE('19/07/2015', 'DD/MM/YYYY');
    K_CURRENT_DATE CONSTANT DATE := TRUNC(SYSDATE);

    l_appointment_from_date wios_migration.subarea_appointments.responsible_from_date%TYPE;
    l_appointment_to_date wios_migration.subarea_appointments.responsible_to_date%TYPE;

    FUNCTION convert_to_date(
      p_date_as_string IN VARCHAR2
    , p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
    ) RETURN DATE
    IS

     BEGIN

      RETURN TO_DATE(p_date_as_string, 'DD/MM/RRRR');

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
        l_licence_type VARCHAR2(4000);
        l_licence_number VARCHAR2(4000);
        l_block_reference VARCHAR2(4000);
        l_subarea_name VARCHAR2(4000);

      BEGIN

        SELECT
          TO_CHAR(s.licence_type)
        , TO_CHAR(s.licence_number)
        , TO_CHAR(s.block_reference)
        , TO_CHAR(s.subarea_name)
        INTO
          l_licence_type
        , l_licence_number
        , l_block_reference
        , l_subarea_name
        FROM wios_migration.raw_subarea_appointments_data s
        WHERE s.migratable_appointment_id = p_migratable_appointment_id;

        SELECT x.next_appointment_from_date
        INTO l_inferred_to_date_as_string
        FROM (
          SELECT
            s.migratable_appointment_id
          , LEAD(TO_CHAR(s.responsible_from_date))
              OVER(
                PARTITION BY TO_CHAR(s.licence_type), TO_CHAR(s.licence_number), TO_CHAR(s.block_reference), TO_CHAR(s.subarea_name)
                ORDER BY TO_DATE(TO_CHAR(s.responsible_from_date), 'DD/MM/RRRR')
              ) next_appointment_from_date
          FROM wios_migration.raw_subarea_appointments_data s
          WHERE TO_CHAR(s.licence_type) = l_licence_type
          AND TO_CHAR(s.licence_number) = l_licence_number
          AND TO_CHAR(s.block_reference) = l_block_reference
          AND TO_CHAR(s.subarea_name) = l_subarea_name
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

    UPDATE wios_migration.subarea_appointments wa
    SET
      wa.responsible_from_date = l_appointment_from_date
    , wa.responsible_to_date = l_appointment_to_date
    WHERE wa.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.subarea_appointments responsible dates for migratable_appointment_id '
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
    @param p_is_exploration_phase Indication of if the appointment is for exploration
    @param p_is_development_phase Indication of if the appointment is for development
    @param p_is_decommissioning_phase Indication of if the appointment is for decommissioning

  */
  PROCEDURE migrate_appointment_phases(
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_is_exploration_phase IN wios_migration.raw_subarea_appointments_data.is_exploration_phase%TYPE
  , p_is_development_phase IN wios_migration.raw_subarea_appointments_data.is_development_phase%TYPE
  , p_is_decommissioning_phase IN wios_migration.raw_subarea_appointments_data.is_decommissioning_phase%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_is_exploration_phase wios_migration.subarea_appointments.is_exploration_phase%TYPE;
    l_is_development_phase wios_migration.subarea_appointments.is_development_phase%TYPE;
    l_is_decommissioning_phase wios_migration.subarea_appointments.is_decommissioning_phase%TYPE;

    FUNCTION is_for_phase(
      p_is_for_phase_text IN VARCHAR2
    ) RETURN NUMBER
    IS

      K_IS_PHASE_TEXT CONSTANT VARCHAR2(3) := 'yes';
      K_NOT_PHASE_TEXT CONSTANT VARCHAR2(3) := 'no';

    BEGIN

      IF LOWER(p_is_for_phase_text) = K_IS_PHASE_TEXT THEN

        RETURN 1;

      ELSIF (LOWER(p_is_for_phase_text) = K_NOT_PHASE_TEXT) OR (p_is_for_phase_text IS NULL) THEN

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

    l_is_exploration_phase := is_for_phase(
      p_is_for_phase_text => p_is_exploration_phase
    );

    l_is_development_phase := is_for_phase(
      p_is_for_phase_text => p_is_development_phase
    );

    l_is_decommissioning_phase := is_for_phase(
      p_is_for_phase_text => p_is_decommissioning_phase
    );

    UPDATE wios_migration.subarea_appointments wa
    SET
      wa.is_exploration_phase = l_is_exploration_phase
    , wa.is_development_phase = l_is_development_phase
    , wa.is_decommissioning_phase = l_is_decommissioning_phase
    WHERE wa.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.subarea_appointments phases for migratable_appointment_id '
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
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_appointment_source IN wios_migration.raw_subarea_appointments_data.appointment_source%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_appointment_source wios_migration.subarea_appointments.appointment_source%TYPE;

  BEGIN

    SAVEPOINT sp_before_appointment_source_mapping;

    IF LOWER(p_appointment_source) = K_DEEMED_SOURCE THEN

      l_appointment_source := 'DEEMED';

    ELSIF LOWER(p_appointment_source) = K_OFFLINE_NOMINATION_SOURCE THEN

      l_appointment_source := 'OFFLINE_NOMINATION';

    ELSE

      add_migration_error(
        p_migratable_appointment_id => p_migratable_appointment_id
      , p_error_message => 'Unexpected subarea appointment source: ' || p_appointment_source
      );

    END IF;

    UPDATE wios_migration.subarea_appointments wa
    SET wa.appointment_source = l_appointment_source
    WHERE wa.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.subarea_appointments phases for migratable_appointment_id '
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
    p_migratable_appointment_id IN wios_migration.raw_subarea_appointments_data.migratable_appointment_id%TYPE
  , p_legacy_nomination_reference IN wios_migration.raw_subarea_appointments_data.legacy_nomination_reference%TYPE
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

  BEGIN

    SAVEPOINT sp_before_nomination_mapping;

    UPDATE wios_migration.subarea_appointments wa
    SET wa.legacy_nomination_reference = p_legacy_nomination_reference
    WHERE wa.migratable_appointment_id = p_migratable_appointment_id;

    IF SQL%ROWCOUNT != 1 THEN

      raise_application_error(
        -20990
      , 'Failed to update wios_migration.subarea_appointments nomination reference for migratable_appointment_id '
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

  PROCEDURE cleanse_subarea_appointments
  IS

    t_subarea_lookup t_subarea_lookup_type;
    l_subarea_is_extant_following_pears_changes NUMBER := 0;
    l_copy_forward_subarea_id VARCHAR2(4000);
    l_copy_forward_subarea_name VARCHAR2(4000);
    l_max_migratable_appointment_id wios_migration.subarea_appointments.migratable_appointment_id%TYPE;
    l_original_subarea_end_date DATE;
    l_earliest_responsible_from_date_for_subarea DATE;

  BEGIN

    -- create a lookup of subarea identifiers
    FOR pears_subarea IN (
      SELECT DISTINCT
        create_subarea_identifier_sql(
          p_licence_type => plm.licence_type
        , p_licence_number => plm.licence_no
        , p_block_reference => esv.block_reference
        , p_subarea_name => esv.name
        ) subarea_reference
      , esv.public_subarea_id id
      FROM pedmgr.epa_subareas_mv esv
      JOIN pedmgr.ped_licence_master plm ON plm.id = esv.licence_id
      WHERE esv.shore_location = 'OFFSHORE'
    )
    LOOP

      t_subarea_lookup(pears_subarea.subarea_reference) := pears_subarea.id;

    END LOOP;

    FOR migratable_subarea_appointment IN (
      SELECT
        sad.migratable_appointment_id
      , sad.licence_type
      , sad.licence_number
      , sad.block_reference
      , sad.subarea_name
      , sad.appointed_operator_name
      , LOWER(sad.appointment_source) appointment_source
      , sad.is_decommissioning_phase
      , sad.is_development_phase
      , sad.is_exploration_phase
      , sad.legacy_nomination_reference
      , sad.responsible_from_date
      , sad.responsible_to_date
      , ROWNUM row_index
      , COUNT(*) OVER() total_rows
      FROM wios_migration.raw_subarea_appointments_data sad
    )
    LOOP

      logger.debug(
        K_LOG_PREFIX || 'Starting subarea appointment migration for migratable_appointment_id ' ||
        migratable_subarea_appointment.migratable_appointment_id || ' (' ||
        migratable_subarea_appointment.row_index || '/' || migratable_subarea_appointment.total_rows || ')'
      );

      INSERT INTO wios_migration.subarea_appointments(migratable_appointment_id)
      VALUES(migratable_subarea_appointment.migratable_appointment_id);

      COMMIT;

      BEGIN

        SAVEPOINT sp_before_appointment_migration;

        migrate_subarea_reference_to_subarea(
          p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
        , p_licence_type => TO_CHAR(migratable_subarea_appointment.licence_type)
        , p_licence_number => TO_CHAR(migratable_subarea_appointment.licence_number)
        , p_block_reference => TO_CHAR(migratable_subarea_appointment.block_reference)
        , p_subarea_name => TO_CHAR(migratable_subarea_appointment.subarea_name)
        , p_subarea_lookup => t_subarea_lookup
        );

        migrate_operator_name_to_operator(
          p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
        , p_operator_name => migratable_subarea_appointment.appointed_operator_name
        );

        migrate_appointment_dates(
          p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
        , p_appointment_from_date => migratable_subarea_appointment.responsible_from_date
        , p_appointment_to_date => migratable_subarea_appointment.responsible_to_date
        );

        migrate_appointment_phases(
          p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
        , p_is_exploration_phase => migratable_subarea_appointment.is_exploration_phase
        , p_is_development_phase => migratable_subarea_appointment.is_development_phase
        , p_is_decommissioning_phase => migratable_subarea_appointment.is_decommissioning_phase
        );

        migrate_appointment_source(
          p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
        , p_appointment_source => migratable_subarea_appointment.appointment_source
        );

        IF LOWER(migratable_subarea_appointment.appointment_source) = K_OFFLINE_NOMINATION_SOURCE AND migratable_subarea_appointment.legacy_nomination_reference IS NOT NULL THEN

          migrate_legacy_nomination_reference(
            p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
          , p_legacy_nomination_reference => migratable_subarea_appointment.legacy_nomination_reference
          );

        ELSIF LOWER(migratable_subarea_appointment.appointment_source) != K_OFFLINE_NOMINATION_SOURCE AND migratable_subarea_appointment.legacy_nomination_reference IS NOT NULL THEN

          add_migration_error(
            p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
          , p_error_message => 'Legacy nomination reference provided for non ' || K_OFFLINE_NOMINATION_SOURCE || ' appointment for migratable_appointment_id ' || migratable_subarea_appointment.migratable_appointment_id
          );

        END IF;

        logger.debug(
          K_LOG_PREFIX || 'Finished subarea appointment migration for migratable_appointment_id ' ||
          migratable_subarea_appointment.migratable_appointment_id || ' (' ||
          migratable_subarea_appointment.row_index || '/' || migratable_subarea_appointment.total_rows || ')'
        );

        COMMIT;

      EXCEPTION WHEN OTHERS THEN

        ROLLBACK TO SAVEPOINT sp_before_appointment_migration;

        add_migration_error(
          p_migratable_appointment_id => migratable_subarea_appointment.migratable_appointment_id
        , p_error_message => 'Unexpected error in cleanse_subarea_appointments: ' || CHR(10) || CHR(10) ||
            SQLERRM || CHR(10) || CHR(10) || dbms_utility.format_error_backtrace()
        );

        COMMIT;

      END;

    END LOOP;

    -- migration of forward area approval appointment will error when the subarea
    -- the appointment is for did not exist on the appointment date
    FOR subarea_not_existing_at_appointment_time IN (
      SELECT sa.migratable_appointment_id
      FROM wios_migration.subarea_appointments sa
      JOIN wios_migration.raw_subarea_appointments_data sad ON sad.migratable_appointment_id = sa.migratable_appointment_id
      WHERE NOT EXISTS (
        SELECT 1
        FROM pedmgr.ped_current_data_points pcdp
        JOIN pedmgr.ped_licence_blocks plb ON plb.ped_dp_id = pcdp.id
        JOIN pedmgr.ped_licence_block_refs plbr ON plbr.plb_id = plb.id
          AND plbr.block_ref = TO_CHAR(sad.block_reference)
        JOIN pedmgr.ped_subareas ps ON ps.ped_lb_id = plb.id
          AND ps.title = TO_CHAR(sad.subarea_name)
        WHERE sa.responsible_from_date BETWEEN ps.start_datetime AND NVL(ps.end_datetime, SYSDATE)
        AND pcdp.ped_sim_id = 0
        AND pcdp.licence_type = TO_CHAR(sad.licence_type)
        AND pcdp.licence_no = TO_NUMBER(sad.licence_number)
      )
      AND sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => subarea_not_existing_at_appointment_time.migratable_appointment_id
      , p_error_message => 'Subarea does not exist during the appointment period'
      );

    END LOOP;

    -- migration of a forward area approval appointment will occur for the current extant versions of the subarea when
    -- the subarea the appointment is for existed on the appointment date (as per PEARS timeline) AND
    -- the subarea exists as one or more extant subareas after taking into account operations that have taken place since the appointment date
    -- the original subarea will also be migrated as REMOVED with the appointment migrated as ended on the date the subarea was ended in pears
    FOR asset IN (
      SELECT DISTINCT sa.subarea_id
      FROM wios_migration.subarea_appointments sa
      WHERE sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      SAVEPOINT sp_before_subarea_copy_forward;

      BEGIN

        SELECT MIN(sa.responsible_from_date)
        INTO l_earliest_responsible_from_date_for_subarea
        FROM wios_migration.subarea_appointments sa
        WHERE sa.subarea_id = asset.subarea_id
        AND sa.migratable_appointment_id NOT IN(
          SELECT DISTINCT sme.migratable_appointment_id
          FROM wios_migration.subarea_migration_errors sme
        );

        FOR subarea_history IN (
          SELECT
            h.subarea_history_id id
            -- for null end subareas set end date to tomorrow, we will have no future appointments
            -- and this just makes null checking easier later in the script
          , MAX(COALESCE(h.end_datetime, TRUNC(SYSDATE + 1))) end_date
          FROM pedmgr.subarea_history h
          JOIN pedmgr.epa_subareas esm ON esm.licence_reference = (h.licence_type || h.licence_no) AND esm.block_reference = h.block_ref AND esm.name = h.title
          WHERE esm.id = asset.subarea_id
          AND (h.end_datetime IS NULL OR TRUNC(h.end_datetime) >= l_earliest_responsible_from_date_for_subarea)
          GROUP BY h.subarea_history_id
        )
        LOOP

          BEGIN

            SELECT
              es.id
            , es.licence_reference || ' ' || es.block_reference || ' ' ||  es.name
            INTO
              l_copy_forward_subarea_id
            , l_copy_forward_subarea_name
            FROM pedmgr.subarea_history h
            JOIN pedmgr.epa_subareas_mv es ON es.name = h.title AND es.block_reference = h.block_ref AND es.licence_reference = (h.licence_type || h.licence_no)
            WHERE h.subarea_history_id = subarea_history.id
            AND h.history_sequence_id = 1
            AND h.end_datetime IS NULL
            -- exclude the current subarea
            AND es.public_subarea_id != asset.subarea_id;

          EXCEPTION WHEN NO_DATA_FOUND THEN

            -- nothing to copy forward as no extant subarea
            -- exists. A future subarea history record may have
            -- an extant subarea to copy
            CONTINUE;

          END;

          SELECT MAX(sa.migratable_appointment_id)
          INTO l_max_migratable_appointment_id
          FROM wios_migration.subarea_appointments sa;

          INSERT INTO wios_migration.subarea_appointments (
            migratable_appointment_id
          , subarea_id
          , subarea_reference
          , appointed_operator_id
          , appointed_operator_name
          , responsible_from_date
          , responsible_to_date
          , appointment_source
          , is_exploration_phase
          , is_development_phase
          , is_decommissioning_phase
          , legacy_nomination_reference
          , created_by_migratable_appointment_id
          , asset_status
          , status
          )
          SELECT
            (l_max_migratable_appointment_id + rownum)
          , l_copy_forward_subarea_id
          , l_copy_forward_subarea_name
          , sa.appointed_operator_id
          , sa.appointed_operator_name
          , sa.responsible_from_date
          -- null responsible_to_date if past end date of subarea, subsequent
          -- checks will fill in correct date based on subsequent appointments for
          -- this subarea or leave as active appointment if there are no subsequent
          -- appointments.
          , CASE
              WHEN sa.responsible_to_date > subarea_history.end_date THEN NULL
              ELSE sa.responsible_to_date
            END
          , sa.appointment_source
          , sa.is_exploration_phase
          , sa.is_development_phase
          , sa.is_decommissioning_phase
          , sa.legacy_nomination_reference
          , sa.migratable_appointment_id
          , 'EXTANT' -- asset
          , 'EXTANT' -- appointment
          FROM wios_migration.subarea_appointments sa
          -- this will insert multiple rows if we have multiple rows apps for a subarea
          WHERE sa.subarea_id = asset.subarea_id
          AND sa.responsible_from_date < subarea_history.end_date;

        END LOOP;

        COMMIT;

      EXCEPTION WHEN OTHERS THEN

        ROLLBACK TO SAVEPOINT sp_before_subarea_copy_forward;

        raise_application_error(
          -20999
        , 'Unexpected error in subarea copy forward for subarea ' || asset.subarea_id
            || CHR(10) || CHR(10) || SQLERRM || CHR(10)
            || CHR(10) || dbms_utility.format_error_backtrace()
        );

      END;

    END LOOP;

    -- any subareas which are no longer extant should be marked as removed and the appointment ended
    -- on the date the subarea ended
    FOR ended_asset IN (
      SELECT DISTINCT sa.subarea_id, TRUNC(es.end_datetime) end_date
      FROM wios_migration.subarea_appointments sa
      JOIN pedmgr.epa_subareas_mv es ON es.public_subarea_id = sa.subarea_id AND es.end_datetime IS NOT NULL
      WHERE sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      UPDATE wios_migration.subarea_appointments sa
      SET
        sa.asset_status = 'REMOVED'
      , sa.responsible_to_date = LEAST(NVL(sa.responsible_to_date, SYSDATE), ended_asset.end_date)
      WHERE sa.subarea_id = ended_asset.subarea_id;

    END LOOP;

    -- after the subarea cascade process has run, re-check if we need to end
    -- appointments due to no end dates being provided in the spreadsheet and
    -- subsequent appointments for the same asset existing
    FOR current_appointment IN (
      SELECT
        sa.subarea_id
      , sa.migratable_appointment_id
      FROM wios_migration.subarea_appointments sa
      WHERE sa.responsible_to_date IS NULL
      AND sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      DECLARE

        l_next_appointment_from_date DATE;

      BEGIN

        SELECT t.next_appointment_from_date
        INTO l_next_appointment_from_date
        FROM (
          SELECT
            s.migratable_appointment_id
          , LEAD(s.responsible_from_date) OVER(PARTITION BY s.subarea_id ORDER BY s.responsible_from_date) next_appointment_from_date
          FROM wios_migration.subarea_appointments s
          WHERE s.subarea_id = current_appointment.subarea_id
        ) t
        WHERE t.migratable_appointment_id = current_appointment.migratable_appointment_id;

        IF l_next_appointment_from_date IS NOT NULL THEN

          UPDATE wios_migration.subarea_appointments s
          SET s.responsible_to_date = l_next_appointment_from_date
          WHERE s.migratable_appointment_id = current_appointment.migratable_appointment_id;

        END IF;

      END;

    END LOOP;

    FOR appointment_with_inconsistent_date IN(
      SELECT *
      FROM (
        SELECT
          wa.migratable_appointment_id
        , wa.responsible_from_date
        , wa.responsible_to_date
        , LEAD(wa.responsible_from_date) OVER(PARTITION BY wa.subarea_id ORDER BY wa.responsible_from_date) next_responsible_from_date
        FROM wios_migration.subarea_appointments wa
        WHERE wa.migratable_appointment_id NOT IN(
          SELECT DISTINCT sme.migratable_appointment_id
          FROM wios_migration.subarea_migration_errors sme
        )
      ) t
      WHERE t.responsible_to_date != t.next_responsible_from_date
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => appointment_with_inconsistent_date.migratable_appointment_id
      , p_error_message => 'The next appointment for this asset does not start from the previous appointments to date.'
      );

    END LOOP;

    -- migration of a forward area approval appointment will error when
    -- the subarea the appointment is for existed on the appointment date (catered for above) AND
    -- the subarea does not exist as an extant subarea after taking into account operations that have taken place since the appointment date
    -- the appointment does not have an end date
    FOR current_appointment IN (
      SELECT
        sa.migratable_appointment_id
      , TO_CHAR(rd.licence_type) licence_type
      , TO_NUMBER(rd.licence_number) licence_number
      , TO_CHAR(rd.block_reference) block_reference
      , TO_CHAR(rd.subarea_name) subarea_name
      , sa.responsible_from_date
      FROM wios_migration.subarea_appointments sa
      JOIN wios_migration.raw_subarea_appointments_data rd ON rd.migratable_appointment_id = sa.migratable_appointment_id
      WHERE sa.responsible_to_date IS NULL
      AND sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      SELECT DECODE(COUNT(*), 0, 0, 1)
      INTO l_subarea_is_extant_following_pears_changes
      FROM pedmgr.subarea_history h
      WHERE h.licence_type = current_appointment.licence_type
      AND h.licence_no = current_appointment.licence_number
      AND h.block_ref = current_appointment.block_reference
      AND h.title = current_appointment.subarea_name
      AND current_appointment.responsible_from_date BETWEEN h.start_datetime AND NVL(h.end_datetime, SYSDATE);

      IF l_subarea_is_extant_following_pears_changes = 0 THEN

        add_migration_error(
          p_migratable_appointment_id => current_appointment.migratable_appointment_id
        , p_error_message => 'Subarea appointment is current but subarea does not currently exist as extant taking into account pears changes'
        );

      END IF;

    END LOOP;

    FOR asset_with_more_than_one_deemed_appointment IN (
      WITH appointments_by_source AS (
        SELECT wa.subarea_id, wa.appointment_source, COUNT(*) count
        FROM wios_migration.subarea_appointments wa
        GROUP BY wa.subarea_id, wa.appointment_source
      )
      SELECT wa.migratable_appointment_id
      FROM wios_migration.subarea_appointments wa
      LEFT JOIN appointments_by_source abs ON abs.subarea_id = wa.subarea_id AND abs.appointment_source = 'DEEMED'
      WHERE COALESCE(abs.count, 0) > 1
      AND wa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => asset_with_more_than_one_deemed_appointment.migratable_appointment_id
      , p_error_message => 'Asset does not have one deemed appointment'
      );

    END LOOP;

    FOR asset_with_more_than_one_current_appointment IN(
      SELECT wa.migratable_appointment_id
      FROM wios_migration.subarea_appointments wa
      WHERE wa.subarea_id IN (
        SELECT current_appointment.subarea_id
        FROM wios_migration.subarea_appointments current_appointment
        WHERE current_appointment.responsible_to_date IS NULL
        GROUP BY current_appointment.subarea_id
        HAVING COUNT(*) > 1
      )
      AND wa.responsible_to_date IS NULL
      AND wa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => asset_with_more_than_one_current_appointment.migratable_appointment_id
      , p_error_message => 'Asset has more than one active appointment'
      );

    END LOOP;

    FOR deemed_appointment_not_on_deemed_date IN (
      SELECT sa.migratable_appointment_id
      FROM wios_migration.subarea_appointments sa
      WHERE sa.appointment_source = 'DEEMED'
      AND sa.responsible_from_date != TO_DATE('19/07/2015', 'DD/MM/YYYY')
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => deemed_appointment_not_on_deemed_date.migratable_appointment_id
      , p_error_message => 'Asset has a deemed appointment that does not start on the deeming date'
      );

    END LOOP;

    FOR appointment_with_start_date_after_end_date IN (
      SELECT sa.migratable_appointment_id
      FROM wios_migration.subarea_appointments sa
      WHERE sa.responsible_to_date IS NOT NULL
      AND sa.responsible_from_date > sa.responsible_to_date
      AND sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => appointment_with_start_date_after_end_date.migratable_appointment_id
      , p_error_message => 'Appointment start date is after the appointment end date'
      );

    END LOOP;

    FOR appointment_with_start_date_in_future IN(
      SELECT sa.migratable_appointment_id
      FROM wios_migration.subarea_appointments sa
      WHERE sa.responsible_from_date > TRUNC(SYSDATE)
      AND sa.migratable_appointment_id NOT IN(
        SELECT DISTINCT sme.migratable_appointment_id
        FROM wios_migration.subarea_migration_errors sme
      )
    )
    LOOP

      add_migration_error(
        p_migratable_appointment_id => appointment_with_start_date_in_future.migratable_appointment_id
      , p_error_message => 'Appointment start date is in the future'
      );

    END LOOP;

  END cleanse_subarea_appointments;

END subarea_appointment_migration;