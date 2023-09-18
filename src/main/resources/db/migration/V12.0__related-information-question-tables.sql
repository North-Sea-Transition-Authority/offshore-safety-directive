CREATE TABLE related_information (
  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY
, nomination_detail_id UUID NOT NULL
, related_to_fields BOOLEAN
, related_to_licence_applications BOOLEAN
, related_licence_applications TEXT
, related_to_well_applications BOOLEAN
, related_well_applications TEXT
, CONSTRAINT related_information_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX related_information_nomination_detail_id_idx ON related_information (nomination_detail_id);

CREATE TABLE related_information_fields (
  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY
, related_information_id INT NOT NULL
, field_id INT NOT NULL
, field_name TEXT NOT NULL
, CONSTRAINT related_information_fields_related_information_fk
    FOREIGN KEY (related_information_id) REFERENCES related_information (id)
);

CREATE INDEX related_information_fields_related_information_idx
ON related_information_fields (related_information_id);
