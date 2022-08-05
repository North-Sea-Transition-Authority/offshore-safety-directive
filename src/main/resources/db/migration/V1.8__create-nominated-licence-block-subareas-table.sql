CREATE TABLE osd.nominated_licence_block_subareas (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, block_subarea_id INT
, CONSTRAINT nlbs_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_nlbs_nomination_detail_idx ON osd.nominated_licence_block_subareas (nomination_detail);