CREATE TABLE osd.nomination_detail_files (
    uuid UUID PRIMARY KEY,
    nomination_detail_id INTEGER NOT NULL,
    file_uuid UUID NOT NULL,
    file_status TEXT NOT NULL,
    CONSTRAINT ndf_nomination_details_fk FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id),
    CONSTRAINT ndf_uploaded_files_fk FOREIGN KEY (file_uuid) REFERENCES uploaded_files (id)
);

CREATE INDEX ndf_nomination_detail_id_idx ON nomination_detail_files(nomination_detail_id);
CREATE INDEX ndf_file_uuid_idx ON nomination_detail_files(file_uuid);

CREATE TABLE osd.case_event_files (
    uuid UUID PRIMARY KEY,
    case_event_uuid UUID NOT NULL,
    file_uuid UUID NOT NULL,
    CONSTRAINT cef_case_events_fk FOREIGN KEY (case_event_uuid) REFERENCES case_events (uuid),
    CONSTRAINT cef_uploaded_files_fk FOREIGN KEY (file_uuid) REFERENCES uploaded_files (id)
);

CREATE INDEX cef_case_event_uuid_idx ON case_event_files(case_event_uuid);
CREATE INDEX cef_uploaded_files_id_idx ON case_event_files(file_uuid);