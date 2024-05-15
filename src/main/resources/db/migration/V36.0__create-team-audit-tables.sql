CREATE TABLE teams_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, type VARCHAR
, display_name TEXT
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE team_member_roles_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, wua_id INT
, role TEXT
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE team_scopes_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, portal_id TEXT
, portal_team_type TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);