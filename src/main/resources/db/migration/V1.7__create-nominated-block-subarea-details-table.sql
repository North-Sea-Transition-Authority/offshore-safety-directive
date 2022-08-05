CREATE TABLE osd.nominated_block_subarea_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, valid_for_future_wells_in_subarea BOOLEAN
, for_all_well_phases BOOLEAN
, exploration_and_appraisal_phase BOOLEAN
, development_phase BOOLEAN
, decommissioning_phase BOOLEAN
, CONSTRAINT nbsd_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_nbsd_nomination_detail_idx ON osd.nominated_block_subarea_details (nomination_detail);