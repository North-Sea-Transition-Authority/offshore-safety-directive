CREATE TABLE nominee_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, nominated_organisation_id INT
, reason_for_nomination TEXT
, planned_start_date DATE
, operator_has_authority BOOLEAN
, operator_has_capacity BOOLEAN
, licensee_acknowledge_operator_requirements BOOLEAN
, CONSTRAINT nominee_details_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominee_details_nomination_detail_idx ON nominee_details (nomination_detail_id);