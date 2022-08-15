CREATE TABLE osd.nominated_installations (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail INT NOT NULL
, installation_id INT
, CONSTRAINT nominated_installations_fk1 FOREIGN KEY (nomination_detail) REFERENCES osd.nomination_details (id)
);

CREATE INDEX osd_ni_nomination_detail_idx ON osd.nominated_installations (nomination_detail);