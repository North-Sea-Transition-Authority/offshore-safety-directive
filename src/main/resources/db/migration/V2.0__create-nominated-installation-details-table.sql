CREATE TABLE osd.nominated_installation_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, for_all_installation_phases BOOLEAN
, development_design_phase BOOLEAN
, development_construction_phase BOOLEAN
, development_installation_phase BOOLEAN
, development_commissioning_phase BOOLEAN
, development_production_phase BOOLEAN
, decommissioning_phase BOOLEAN
, CONSTRAINT nominated_installation_details_fk1 FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_nid_nomination_detail_idx ON osd.nominated_installation_details (nomination_detail);