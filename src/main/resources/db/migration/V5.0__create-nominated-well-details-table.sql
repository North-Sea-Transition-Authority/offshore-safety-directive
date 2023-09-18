CREATE TABLE nominated_well_details (
  id UUID PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, for_all_well_phases BOOLEAN
, exploration_and_appraisal_phase BOOLEAN
, development_phase BOOLEAN
, decommissioning_phase BOOLEAN
, CONSTRAINT nominated_well_details_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominated_well_details_nomination_detail_id_idx ON nominated_well_details (nomination_detail_id);