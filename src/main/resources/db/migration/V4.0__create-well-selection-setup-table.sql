CREATE TABLE well_selection_setup (
  id UUID PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, selection_type VARCHAR(255)
, CONSTRAINT well_selection_setup_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX well_selection_setup_nomination_detail_id_idx ON well_selection_setup (nomination_detail_id);