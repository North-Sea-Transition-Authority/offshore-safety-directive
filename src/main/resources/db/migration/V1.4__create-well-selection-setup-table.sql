CREATE TABLE osd.well_selection_setup (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, selection_type VARCHAR(255)
, CONSTRAINT ws_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_ws_nomination_detail_idx ON osd.well_selection_setup (nomination_detail);