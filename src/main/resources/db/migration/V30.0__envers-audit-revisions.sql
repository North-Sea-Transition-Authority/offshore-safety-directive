CREATE TABLE audit_revisions (
  audit_revision_id SERIAL PRIMARY KEY,
  updated_at TIMESTAMPTZ,
  updated_by_user_id VARCHAR(255),
  updated_by_proxy_user_id VARCHAR(255)
);

CREATE TABLE nominations_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, created_datetime TIMESTAMPTZ
, reference TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);