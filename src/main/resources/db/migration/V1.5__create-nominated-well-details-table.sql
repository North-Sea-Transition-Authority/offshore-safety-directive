CREATE TABLE osd.nominated_well_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, for_all_well_phases BOOLEAN
, exploration_and_appraisal_phase BOOLEAN
, development_phase BOOLEAN
, decommissioning_phase BOOLEAN
, CONSTRAINT sws_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_sws_nomination_detail_idx ON osd.nominated_well_details (nomination_detail);