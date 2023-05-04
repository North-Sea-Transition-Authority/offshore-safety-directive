CREATE TABLE osd.uploaded_file_details (
    uuid UUID PRIMARY KEY NOT NULL,
    file_uuid UUID NOT NULL,
    file_status TEXT NOT NULL,
    reference_type TEXT NOT NULL,
    reference_id TEXT NOT NULL,
    purpose TEXT,
    uploaded_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT ufd_uploaded_files_fk FOREIGN KEY (file_uuid) REFERENCES uploaded_files (id)
);

CREATE INDEX ufd_uploaded_files_idx1 ON uploaded_file_details(file_uuid);