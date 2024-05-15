ALTER TABLE applicant_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE applicant_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE applicant_details_audit
RENAME TO applicant_details_aud;


ALTER TABLE appointment_corrections_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE appointment_corrections_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE appointment_corrections_audit
RENAME TO appointment_corrections_aud;


ALTER TABLE appointment_terminations_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE appointment_terminations_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE appointment_terminations_audit
RENAME TO appointment_terminations_aud;


ALTER TABLE appointments_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE appointments_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE appointments_audit
RENAME TO appointments_aud;


ALTER TABLE asset_phases_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE asset_phases_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE asset_phases_audit
RENAME TO asset_phases_aud;


ALTER TABLE assets_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE assets_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE assets_audit
RENAME TO assets_aud;


ALTER TABLE case_events_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE case_events_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE case_events_audit
RENAME TO case_events_aud;


ALTER TABLE excluded_well_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE excluded_well_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE excluded_well_details_audit
RENAME TO excluded_well_details_aud;


ALTER TABLE excluded_wells_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE excluded_wells_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE excluded_wells_audit
RENAME TO excluded_wells_aud;


ALTER TABLE file_associations_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE file_associations_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE file_associations_audit
RENAME TO file_associations_aud;


ALTER TABLE installation_inclusion_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE installation_inclusion_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE installation_inclusion_audit
RENAME TO installation_inclusion_aud;


ALTER TABLE nominated_block_subarea_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_block_subarea_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_block_subarea_details_audit
RENAME TO nominated_block_subarea_details_aud;


ALTER TABLE nominated_installation_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_installation_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_installation_details_audit
RENAME TO nominated_installation_details_aud;


ALTER TABLE nominated_installations_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_installations_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_installations_audit
RENAME TO nominated_installations_aud;


ALTER TABLE nominated_licence_block_subareas_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_licence_block_subareas_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_licence_block_subareas_audit
RENAME TO nominated_licence_block_subareas_aud;


ALTER TABLE nominated_subarea_wells_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_subarea_wells_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_subarea_wells_audit
RENAME TO nominated_subarea_wells_aud;


ALTER TABLE nominated_well_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_well_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_well_details_audit
RENAME TO nominated_well_details_aud;


ALTER TABLE nominated_wells_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominated_wells_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominated_wells_audit
RENAME TO nominated_wells_aud;


ALTER TABLE nomination_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nomination_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nomination_details_audit
RENAME TO nomination_details_aud;


ALTER TABLE nomination_licences_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nomination_licences_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nomination_licences_audit
RENAME TO nomination_licences_aud;


ALTER TABLE nomination_portal_references_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nomination_portal_references_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nomination_portal_references_audit
RENAME TO nomination_portal_references_aud;


ALTER TABLE nominations_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominations_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominations_audit
RENAME TO nominations_aud;


ALTER TABLE nominee_details_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE nominee_details_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE nominee_details_audit
RENAME TO nominee_details_aud;


ALTER TABLE related_information_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE related_information_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE related_information_audit
RENAME TO related_information_aud;


ALTER TABLE related_information_fields_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE related_information_fields_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE related_information_fields_audit
RENAME TO related_information_fields_aud;


ALTER TABLE team_member_roles_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE team_member_roles_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE team_member_roles_audit
RENAME TO team_member_roles_aud;


ALTER TABLE team_scopes_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE team_scopes_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE team_scopes_audit
RENAME TO team_scopes_aud;


ALTER TABLE teams_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE teams_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE teams_audit
RENAME TO teams_aud;


ALTER TABLE uploaded_files_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE uploaded_files_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE uploaded_files_audit
RENAME TO uploaded_files_aud;


ALTER TABLE well_selection_setup_audit
RENAME COLUMN audit_revision_id TO rev;

ALTER TABLE well_selection_setup_audit
RENAME COLUMN audit_type TO revtype;

ALTER TABLE well_selection_setup_audit
RENAME TO well_selection_setup_aud;