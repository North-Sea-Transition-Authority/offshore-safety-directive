CREATE TABLE osd.installation_advice (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, include_installations_in_nomination BOOLEAN
, CONSTRAINT installation_advice_fk1 FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_ia_nomination_detail_idx ON osd.installation_advice (nomination_detail);