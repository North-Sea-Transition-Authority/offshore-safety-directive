ALTER TABLE osd.uploaded_file_details RENAME TO file_associations;
ALTER TABLE osd.file_associations ALTER COLUMN purpose SET NOT NULL;

CREATE INDEX fa_reference_type_idx ON file_associations(reference_type);
CREATE INDEX fa_reference_id_idx ON file_associations(reference_id);
CREATE INDEX fa_purpose_idx ON file_associations(purpose);