CREATE SCHEMA IF NOT EXISTS osd_migration;

CREATE TABLE osd_migration.migratable_wellbore_appointments (
  migratable_appointment_id INT
, wellbore_id INT
, wellbore_registration_number TEXT
, appointed_operator_id INT
, appointed_operator_name TEXT
, responsible_from_date DATE
, responsible_to_date DATE
, is_exploration_phase INT
, is_development_phase INT
, is_decommissioning_phase INT
, appointment_source VARCHAR(255)
, legacy_nomination_reference TEXT
);

CREATE TABLE osd_migration.migratable_installation_appointments (
  migratable_appointment_id INT
, installation_id INT
, installation_name TEXT
, appointed_operator_id INT
, appointed_operator_name TEXT
, responsible_from_date DATE
, responsible_to_date DATE
, is_development_phase INT
, is_decommissioning_phase INT
, appointment_source VARCHAR(255)
, legacy_nomination_reference TEXT
);

CREATE TABLE osd_migration.migratable_subarea_appointments (
  migratable_appointment_id INT
, subarea_id TEXT
, subarea_reference TEXT
, appointed_operator_id INT
, appointed_operator_name TEXT
, responsible_from_date DATE
, responsible_to_date DATE
, is_exploration_phase INT
, is_development_phase INT
, is_decommissioning_phase INT
, appointment_source VARCHAR(255)
, legacy_nomination_reference TEXT
, created_by_migratable_appointment_id INT
, asset_status VARCHAR(255)
);

CREATE TABLE osd_migration.subarea_migration_appointment_lookup (
  migratable_appointment_id INT
, appointment_id UUID
);