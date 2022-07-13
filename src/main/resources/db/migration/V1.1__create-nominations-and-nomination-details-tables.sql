CREATE TABLE osd.nominations (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, created_datetime TIMESTAMP
);

CREATE TABLE osd.nomination_details (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_id INT NOT NULL
, created_datetime TIMESTAMP NOT NULL
, version INT NOT NULL
, status TEXT NOT NULL
, CONSTRAINT nd_nomination_fk FOREIGN KEY (nomination_id) REFERENCES osd.nominations (id)
);

CREATE INDEX osd_nd_nomination_idx ON osd.nomination_details (nomination_id);