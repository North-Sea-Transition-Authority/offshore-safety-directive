CREATE TABLE wios_migration.migration_csv_files (
  migration_csv CLOB
, type VARCHAR2(4000) NOT NULL
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.raw_wellbore_appointments_data (
  migratable_appointment_id NUMBER UNIQUE
, wellbore_registration_number CLOB
, appointed_operator_name CLOB
, responsible_from_date CLOB
, responsible_to_date CLOB
, is_exploration_phase VARCHAR2(4000)
, is_development_phase VARCHAR2(4000)
, is_decommissioning_phase VARCHAR2(4000)
, appointment_source VARCHAR2(4000)
, legacy_nomination_reference CLOB
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.raw_installation_appointments_data (
  migratable_appointment_id NUMBER UNIQUE
, installation_name CLOB
, appointed_operator_name CLOB
, responsible_from_date CLOB
, responsible_to_date CLOB
, is_development_phase VARCHAR2(4000)
, is_decommissioning_phase VARCHAR2(4000)
, appointment_source VARCHAR2(4000)
, legacy_nomination_reference CLOB
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.raw_subarea_appointments_data (
  migratable_appointment_id NUMBER UNIQUE
, licence_type CLOB
, licence_number CLOB
, block_reference CLOB
, subarea_name CLOB
, responsible_from_date CLOB
, responsible_to_date CLOB
, is_exploration_phase VARCHAR2(4000)
, is_development_phase VARCHAR2(4000)
, is_decommissioning_phase VARCHAR2(4000)
, appointment_source VARCHAR2(4000)
, legacy_nomination_reference CLOB
) TABLESPACE tbsdata;