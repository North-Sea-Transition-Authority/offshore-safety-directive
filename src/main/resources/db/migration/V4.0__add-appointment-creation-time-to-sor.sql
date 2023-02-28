ALTER TABLE osd.appointments
ADD COLUMN created_datetime TIMESTAMP;

UPDATE osd.appointments
SET created_datetime = responsible_from_date::timestamp
WHERE created_datetime IS NULL;

ALTER TABLE osd.appointments
ALTER COLUMN created_datetime SET NOT NULL;