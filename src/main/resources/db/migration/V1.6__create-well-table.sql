CREATE TABLE osd.nominated_wells (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, well_id INT
, CONSTRAINT nw_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_nw_nomination_detail_idx ON osd.nominated_wells (nomination_detail);