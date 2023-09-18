CREATE TABLE applicant_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, portal_organisation_id INT NOT NULL
, applicant_reference TEXT
, CONSTRAINT applicant_details_nomination_detail_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX  applicant_details_nomination_detail_id_idx ON applicant_details (nomination_detail_id);