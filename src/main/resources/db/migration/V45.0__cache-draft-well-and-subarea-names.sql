-- nominated_wells
ALTER TABLE nominated_wells
ADD COLUMN name TEXT;

UPDATE nominated_wells
SET name = 'UNKNOWN';

ALTER TABLE nominated_wells
ALTER COLUMN name SET NOT NULL;

ALTER TABLE nominated_wells_aud
ADD COLUMN name TEXT;

-- nominated_licence_block_subareas
ALTER TABLE nominated_licence_block_subareas
ADD COLUMN name TEXT;

UPDATE nominated_licence_block_subareas
SET name = 'UNKNOWN';

ALTER TABLE nominated_licence_block_subareas
ALTER COLUMN name SET NOT NULL;

ALTER TABLE nominated_licence_block_subareas_aud
ADD COLUMN name TEXT;

-- nominated_subarea_wells
ALTER TABLE nominated_subarea_wells
ADD COLUMN name TEXT;

UPDATE nominated_subarea_wells
SET name = 'UNKNOWN';

ALTER TABLE nominated_subarea_wells
ALTER COLUMN name SET NOT NULL;

ALTER TABLE nominated_subarea_wells_aud
ADD COLUMN name TEXT;