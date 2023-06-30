CREATE TABLE nominated_installation_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id INT NOT NULL
, for_all_installation_phases BOOLEAN
, development_design_phase BOOLEAN
, development_construction_phase BOOLEAN
, development_installation_phase BOOLEAN
, development_commissioning_phase BOOLEAN
, development_production_phase BOOLEAN
, decommissioning_phase BOOLEAN
, CONSTRAINT nominated_installation_details_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominated_installation_details_nomination_detail_id_idx
ON nominated_installation_details (nomination_detail_id);