CREATE TABLE nominations (
  id UUID PRIMARY KEY
, created_datetime TIMESTAMPTZ
, reference TEXT UNIQUE
);

CREATE TABLE nomination_details (
  id UUID PRIMARY KEY
, nomination_id UUID NOT NULL
, created_datetime TIMESTAMPTZ NOT NULL
, version INT
, status TEXT NOT NULL
, submitted_datetime TIMESTAMPTZ
, CONSTRAINT nomination_details_nomination_id_fk FOREIGN KEY (nomination_id) REFERENCES nominations (id)
, CONSTRAINT nomination_details_nomination_id_version_unique UNIQUE (nomination_id, version)
);

CREATE INDEX nomination_details_nomination_id_idx ON nomination_details (nomination_id);