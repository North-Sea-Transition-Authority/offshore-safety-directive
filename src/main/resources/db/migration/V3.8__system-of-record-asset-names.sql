ALTER TABLE osd.assets
ADD COLUMN asset_name TEXT;

UPDATE osd.assets
SET asset_name = ''
WHERE assets.asset_name IS NULL;

ALTER TABLE osd.assets
ALTER COLUMN asset_name SET NOT NULL;