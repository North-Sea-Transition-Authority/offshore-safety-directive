CREATE TABLE excluded_well_details (
  uuid UUID PRIMARY KEY
, nomination_detail_id INT NOT NULL
, has_wells_to_exclude BOOLEAN
, CONSTRAINT excluded_well_detail_nomination_detail_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX excluded_well_details_nomination_detail_id_idx ON excluded_well_details (nomination_detail_id);

CREATE TABLE excluded_wells (
  uuid UUID PRIMARY KEY
, nomination_detail_id INT NOT NULL
, wellbore_id INT NOT NULL
, CONSTRAINT excluded_wells_nomination_detail_fk FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX excluded_wells_nomination_detail_id_idx ON excluded_wells (nomination_detail_id);
