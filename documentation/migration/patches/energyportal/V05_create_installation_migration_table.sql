GRANT SELECT ON devukmgr.facilities TO wios_migration;

CREATE TABLE wios_migration.installation_appointments (
  migratable_appointment_id NUMBER
, installation_id NUMBER
, installation_name VARCHAR2(4000)
, appointed_operator_id NUMBER
, appointed_operator_name VARCHAR2(4000)
, responsible_from_date DATE
, responsible_to_date DATE
, is_development_phase NUMBER
, is_decommissioning_phase NUMBER
, appointment_source VARCHAR2(4000)
, legacy_nomination_reference VARCHAR2(4000)
, CONSTRAINT installation_migration_fkey
    FOREIGN KEY (migratable_appointment_id) REFERENCES wios_migration.raw_installation_appointments_data(migratable_appointment_id)
, CONSTRAINT installation_development_check CHECK(is_development_phase IN (0, 1))
, CONSTRAINT installation_decommissioning_check CHECK(is_decommissioning_phase IN (0, 1))
, CONSTRAINT installation_appointment_source_check CHECK(appointment_source IN ('DEEMED', 'OFFLINE_NOMINATION', 'FORWARD_APPROVED'))
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.unmatched_installations (
  installation_name CLOB
, possible_matches CLOB
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.installation_migration_errors (
  migratable_appointment_id NUMBER
, error_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP
, error_message CLOB NOT NULL
) TABLESPACE tbsdata;