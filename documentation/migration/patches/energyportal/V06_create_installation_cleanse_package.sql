CREATE OR REPLACE PACKAGE wios_migration.installation_appointment_migration AS

  K_LOG_PREFIX CONSTANT VARCHAR2(4000) := 'INSTALLATION_APPOINTMENT_MIGRATION: ';

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
        iad.*
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

  END cleanse_installation_appointments;

END installation_appointment_migration;