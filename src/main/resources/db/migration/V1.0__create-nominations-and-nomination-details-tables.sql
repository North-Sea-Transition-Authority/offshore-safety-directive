CREATE TABLE nominations (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, created_datetime TIMESTAMP
, reference TEXT UNIQUE
);

CREATE TABLE nomination_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_id INT NOT NULL
, created_datetime TIMESTAMP NOT NULL
, version INT
, status TEXT NOT NULL
, submitted_datetime TIMESTAMP
, CONSTRAINT nomination_details_nomination_id_fk FOREIGN KEY (nomination_id) REFERENCES nominations (id)
, CONSTRAINT nomination_details_nomination_id_version_unique UNIQUE (nomination_id, version)
);

CREATE INDEX nomination_details_nomination_id_idx ON nomination_details (nomination_id);