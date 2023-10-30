GRANT SELECT ON wellmgr.xview_wons_wellbore_search TO wios_migration;

CREATE TABLE wios_migration.wellbore_appointments (
  migratable_appointment_id NUMBER
, wellbore_id NUMBER
, wellbore_registration_number VARCHAR2(4000)
, appointed_operator_id NUMBER
, appointed_operator_name VARCHAR2(4000)
, responsible_from_date DATE
, responsible_to_date DATE
, is_exploration_phase NUMBER
, is_development_phase NUMBER
, is_decommissioning_phase NUMBER
, appointment_source VARCHAR2(4000)
, legacy_nomination_reference VARCHAR2(4000)
, CONSTRAINT migration_fkey
    FOREIGN KEY (migratable_appointment_id) REFERENCES wios_migration.raw_wellbore_appointments_data(migratable_appointment_id)
, CONSTRAINT exploration_check CHECK(is_exploration_phase IN (0, 1))
, CONSTRAINT development_check CHECK(is_development_phase IN (0, 1))
, CONSTRAINT decommissioning_check CHECK(is_decommissioning_phase IN (0, 1))
, CONSTRAINT appointment_source_check CHECK(appointment_source IN ('DEEMED', 'OFFLINE_NOMINATION', 'FORWARD_APPROVED'))
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.unmatched_wellbores (
  wellbore_registration_number CLOB
, possible_matches CLOB
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.wellbore_migration_errors (
  migratable_appointment_id NUMBER
, error_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP
, error_message CLOB NOT NULL
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.migration_warnings (
  migratable_appointment_id NUMBER
, warning_message CLOB
) TABLESPACE tbsdata;