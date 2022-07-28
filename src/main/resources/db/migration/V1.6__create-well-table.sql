CREATE TABLE osd.well (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, well_id INT
, CONSTRAINT well_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_well_nomination_detail_idx ON osd.well (nomination_detail);