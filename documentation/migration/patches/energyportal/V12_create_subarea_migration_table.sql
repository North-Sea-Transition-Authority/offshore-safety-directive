GRANT SELECT ON pedmgr.epa_subareas_mv TO wios_migration;

GRANT SELECT ON pedmgr.ped_licence_master TO wios_migration;

CREATE TABLE wios_migration.subarea_appointments (
  migratable_appointment_id NUMBER
, subarea_id VARCHAR2(4000)
, subarea_reference VARCHAR2(4000)
, appointed_operator_id NUMBER
, appointed_operator_name VARCHAR2(4000)
, responsible_from_date DATE
, responsible_to_date DATE
, is_exploration_phase NUMBER
, is_development_phase NUMBER
, is_decommissioning_phase NUMBER
, appointment_source VARCHAR2(4000)
, legacy_nomination_reference VARCHAR2(4000)
, CONSTRAINT subarea_migration_fkey
    FOREIGN KEY (migratable_appointment_id) REFERENCES wios_migration.raw_subarea_appointments_data(migratable_appointment_id)
, CONSTRAINT subarea_exploration_check CHECK(is_exploration_phase IN (0, 1))
, CONSTRAINT subarea_development_check CHECK(is_development_phase IN (0, 1))
, CONSTRAINT subarea_decommissioning_check CHECK(is_decommissioning_phase IN (0, 1))
, CONSTRAINT subarea_appointment_source_check CHECK(appointment_source IN ('DEEMED', 'OFFLINE_NOMINATION', 'FORWARD_APPROVED'))
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.unmatched_subareas(
  licence_type CLOB
, licence_number CLOB
, block_reference CLOB
, possible_matches CLOB
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.subarea_migration_errors (
  migratable_appointment_id NUMBER
, error_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP
, error_message CLOB NOT NULL
) TABLESPACE tbsdata;