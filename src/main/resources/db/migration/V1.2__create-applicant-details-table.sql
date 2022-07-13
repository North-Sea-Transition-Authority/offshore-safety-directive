CREATE TABLE osd.applicant_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, portal_organisation_id INT NOT NULL
, applicant_reference TEXT
, CONSTRAINT ad_nomination_detail_fk FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_ad_nomination_detail_idx ON osd.applicant_details (nomination_detail);