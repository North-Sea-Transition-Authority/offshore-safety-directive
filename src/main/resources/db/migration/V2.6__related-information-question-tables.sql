CREATE TABLE osd.related_information (
  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY
, nomination_detail_id INT NOT NULL
, related_to_fields BOOLEAN
, CONSTRAINT ri_nomination_detail_fk FOREIGN KEY (nomination_detail_id) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_ri_nomination_detail_id_idx ON osd.related_information (nomination_detail_id);

CREATE TABLE osd.related_information_fields (
  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY
, related_information_id INT NOT NULL
, field_id INT NOT NULL
, field_name TEXT NOT NULL
, CONSTRAINT rif_related_information_fk FOREIGN KEY (related_information_id) REFERENCES osd.related_information (id)
);

CREATE INDEX osd_rif_related_information_idx ON osd.related_information_fields (related_information_id);
