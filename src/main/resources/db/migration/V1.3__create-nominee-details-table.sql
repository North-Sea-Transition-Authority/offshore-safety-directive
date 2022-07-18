CREATE TABLE osd.nominee_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, nominated_organisation_id INT
, reason_for_nomination TEXT
, planned_start_date DATE
, operator_has_authority BOOLEAN
, operator_has_capacity BOOLEAN
, licensee_acknowledge_operator_requirements BOOLEAN
, CONSTRAINT nd_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_nd_nomination_detail_idx ON osd.nominee_details (nomination_detail);