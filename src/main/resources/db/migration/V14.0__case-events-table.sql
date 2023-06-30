CREATE TABLE case_events (
  uuid UUID PRIMARY KEY
, type TEXT NOT NULL
, nomination_id INT NOT NULL
, nomination_version INT NOT NULL
, created_by INT NOT NULL
, event_timestamp TIMESTAMP NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, comment TEXT
, title TEXT
, CONSTRAINT case_events_nomination_id_fk FOREIGN KEY(nomination_id) REFERENCES nominations(id)
);

CREATE INDEX case_events_nomination_id_idx ON case_events (nomination_id);