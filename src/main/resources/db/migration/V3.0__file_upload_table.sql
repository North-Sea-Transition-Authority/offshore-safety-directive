CREATE TABLE osd.uploaded_files (
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