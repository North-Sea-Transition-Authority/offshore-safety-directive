CREATE TABLE nomination_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, created_datetime TIMESTAMPTZ
, version INT
, status TEXT
, submitted_datetime TIMESTAMPTZ
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_block_subarea_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, valid_for_future_wells_in_subarea BOOLEAN
, for_all_well_phases BOOLEAN
, exploration_and_appraisal_phase BOOLEAN
, development_phase BOOLEAN
, decommissioning_phase BOOLEAN
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_installation_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, for_all_installation_phases BOOLEAN
, development_design_phase BOOLEAN
, development_construction_phase BOOLEAN
, development_installation_phase BOOLEAN
, development_commissioning_phase BOOLEAN
, development_production_phase BOOLEAN
, decommissioning_phase BOOLEAN
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_installations_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, installation_id INT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE applicant_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, portal_organisation_id INT
, applicant_reference TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE excluded_well_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, has_wells_to_exclude BOOLEAN
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE excluded_wells_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, wellbore_id INT
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE installation_inclusion_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, include_installations_in_nomination BOOLEAN
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_licence_block_subareas_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, block_subarea_id TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_subarea_wells_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, wellbore_id INT
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_well_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, for_all_well_phases BOOLEAN
, exploration_and_appraisal_phase BOOLEAN
, development_phase BOOLEAN
, decommissioning_phase BOOLEAN
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominated_wells_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, well_id INT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nomination_licences_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, licence_id INT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE nominee_details_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, nominated_organisation_id INT
, reason_for_nomination TEXT
, planned_start_date DATE
, operator_has_authority BOOLEAN
, operator_has_capacity BOOLEAN
, licensee_acknowledge_operator_requirements BOOLEAN
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE related_information_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, related_to_fields BOOLEAN
, related_to_licence_applications BOOLEAN
, related_licence_applications TEXT
, related_to_well_applications BOOLEAN
, related_well_applications TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE related_information_fields_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, field_id INT
, field_name TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE well_selection_setup_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, selection_type VARCHAR(255)
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);