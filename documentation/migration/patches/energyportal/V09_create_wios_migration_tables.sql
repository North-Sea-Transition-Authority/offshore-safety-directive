CREATE SCHEMA IF NOT EXISTS osd_migration;

CREATE TABLE osd_migration.migratable_wellbore_appointments (
  migratable_appointment_id TEXT
, wellbore_id TEXT
, wellbore_registration_number TEXT
, appointed_operator_id TEXT
, appointed_operator_name TEXT
, responsible_from_date TEXT
, responsible_to_date TEXT
, is_exploration_phase TEXT
, is_development_phase TEXT
, is_decommissioning_phase TEXT
, appointment_source VARCHAR(255)
, legacy_nomination_reference TEXT
);

CREATE TABLE osd_migration.migratable_installation_appointments (
  migratable_appointment_id TEXT
, installation_id TEXT
, installation_name TEXT
, appointed_operator_id TEXT
, appointed_operator_name TEXT
, responsible_from_date TEXT
, responsible_to_date TEXT
, is_development_phase TEXT
, is_decommissioning_phase TEXT
, appointment_source VARCHAR(255)
, legacy_nomination_reference TEXT
);

CREATE TABLE osd_migration.migratable_subarea_appointments (
  migratable_appointment_id TEXT
, subarea_id TEXT
, subarea_reference TEXT
, appointed_operator_id TEXT
, appointed_operator_name TEXT
, responsible_from_date TEXT
, responsible_to_date TEXT
, is_exploration_phase TEXT
, is_development_phase TEXT
, is_decommissioning_phase TEXT
, appointment_source VARCHAR(255)
, legacy_nomination_reference TEXT
, created_by_migratable_appointment_id TEXT
, asset_status VARCHAR(255)
);

CREATE TABLE osd_migration.subarea_migration_appointment_lookup (
  migratable_appointment_id TEXT
, appointment_id UUID
);