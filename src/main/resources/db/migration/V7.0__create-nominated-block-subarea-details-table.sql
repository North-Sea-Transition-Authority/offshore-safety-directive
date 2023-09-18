CREATE TABLE nominated_block_subarea_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, valid_for_future_wells_in_subarea BOOLEAN
, for_all_well_phases BOOLEAN
, exploration_and_appraisal_phase BOOLEAN
, development_phase BOOLEAN
, decommissioning_phase BOOLEAN
, CONSTRAINT nominated_block_subarea_details_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominated_block_subarea_details_nomination_detail_id_idx
ON nominated_block_subarea_details (nomination_detail_id);