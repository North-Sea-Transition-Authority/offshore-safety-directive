ALTER TABLE osd.case_events
RENAME COLUMN created_timestamp TO event_timestamp;

ALTER TABLE osd.case_events
ADD COLUMN created_timestamp TIMESTAMP DEFAULT NULL;

UPDATE osd.case_events ce
SET created_timestamp = ce.event_timestamp
WHERE created_timestamp IS NULL;

ALTER TABLE osd.case_events
ALTER COLUMN created_timestamp SET NOT NULL;