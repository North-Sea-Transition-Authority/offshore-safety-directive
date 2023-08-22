CREATE TABLE nomination_licences (
  id UUID PRIMARY KEY
, nomination_detail_id INT NOT NULL
, licence_id INT NOT NULL
, CONSTRAINT nominated_installations_nomination_detail_id_fk
  FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nomination_licences_nomination_detail_id_idx ON nomination_licences (nomination_detail_id);