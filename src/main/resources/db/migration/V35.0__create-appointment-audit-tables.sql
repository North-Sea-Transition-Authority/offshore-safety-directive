CREATE TABLE appointment_corrections_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, created_timestamp TIMESTAMPTZ NOT NULL
, corrected_by_wua_id INT NOT NULL
, reason_for_correction TEXT NOT NULL
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE appointment_terminations_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, reason_for_termination TEXT
, termination_date DATE
, terminated_by_wua_id INT
, created_timestamp TIMESTAMPTZ
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE appointments_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, appointed_portal_operator_id VARCHAR(255)
, responsible_from_date DATE
, responsible_to_date DATE
, type VARCHAR(255)
, created_by_nomination_id UUID
, created_by_legacy_nomination_reference TEXT
, created_by_appointment_id UUID
, created_datetime TIMESTAMPTZ
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE assets_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, portal_asset_id TEXT
, portal_asset_type VARCHAR(255)
, asset_name TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE asset_phases_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, phase VARCHAR(255)
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);