ALTER TABLE osd.related_information
ADD COLUMN related_to_licence_applications BOOLEAN,
ADD COLUMN related_licence_applications TEXT,
ADD COLUMN related_to_well_applications BOOLEAN,
ADD COLUMN related_well_applications TEXT;