CREATE TABLE nomination_portal_references_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, portal_reference_type TEXT
, portal_references TEXT
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE case_events_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, type TEXT
, nomination_version INT
, created_by INT
, event_timestamp TIMESTAMPTZ
, created_timestamp TIMESTAMPTZ
, comment TEXT
, title TEXT
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);