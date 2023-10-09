CREATE TABLE uploaded_files_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, id UUID
, file_key TEXT
, bucket_name TEXT
, virtual_folder TEXT
, filename TEXT
, file_content_type TEXT
, file_size_bytes NUMERIC
, uploaded_time_stamp TIMESTAMPTZ
, description TEXT
, PRIMARY KEY (audit_revision_id, id)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);

CREATE TABLE file_associations_audit (
  audit_revision_id SERIAL
, audit_type NUMERIC
, uuid UUID
, file_status TEXT
, reference_type TEXT
, reference_id TEXT
, purpose TEXT
, uploaded_timestamp TIMESTAMPTZ
, PRIMARY KEY (audit_revision_id, uuid)
, FOREIGN KEY (audit_revision_id) REFERENCES audit_revisions(audit_revision_id)
);