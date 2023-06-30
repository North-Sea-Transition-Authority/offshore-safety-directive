CREATE TABLE uploaded_files (
    id UUID PRIMARY KEY,
    file_key TEXT NOT NULL,
    bucket_name TEXT NOT NULL,
    virtual_folder TEXT,
    filename TEXT,
    file_content_type TEXT,
    file_size_bytes NUMERIC,
    uploaded_time_stamp TIMESTAMP NOT NULL,
    description TEXT
);

CREATE TABLE file_associations (
    uuid UUID PRIMARY KEY NOT NULL,
    file_uuid UUID NOT NULL,
    file_status TEXT NOT NULL,
    reference_type TEXT NOT NULL,
    reference_id TEXT NOT NULL,
    purpose TEXT NOT NULL,
    uploaded_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT file_associations_file_uuid_fk FOREIGN KEY (file_uuid) REFERENCES uploaded_files (id)
);

CREATE INDEX file_associations_file_uuid_idx ON file_associations(file_uuid);
CREATE INDEX file_associations_reference_type_idx ON file_associations(reference_type);
CREATE INDEX file_associations_reference_id_idx ON file_associations(reference_id);
CREATE INDEX file_associations_purpose_idx ON file_associations(purpose);