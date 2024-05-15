ALTER TABLE assets
    ADD COLUMN status TEXT,
    ADD COLUMN portal_event_id TEXT,
    ADD COLUMN portal_event_type VARCHAR(255);

UPDATE assets
SET status = 'EXTANT';

ALTER TABLE assets
    ALTER COLUMN status SET NOT NULL;