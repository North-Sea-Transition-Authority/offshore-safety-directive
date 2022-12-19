CREATE TABLE osd.case_events (
  uuid UUID PRIMARY KEY
, type TEXT NOT NULL
, nomination_id INT NOT NULL
, nomination_version INT NOT NULL
, created_by INT NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, comment TEXT
, CONSTRAINT ce_nominations_fk FOREIGN KEY(nomination_id) REFERENCES osd.nominations(id)
);

CREATE INDEX osd_ce_nomination_id_idx ON osd.case_events (nomination_id);