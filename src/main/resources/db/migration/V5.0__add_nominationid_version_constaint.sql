ALTER TABLE osd.nomination_details
ADD CONSTRAINT nomination_id_detail_version_unique UNIQUE (nomination_id, version);