CREATE TABLE osd.nomination_portal_references (
  uuid UUID PRIMARY KEY
, nomination_id INTEGER NOT NULL
, portal_reference_type TEXT NOT NULL
, portal_references TEXT
, CONSTRAINT nsr_nominations_fk FOREIGN KEY (nomination_id) REFERENCES nominations (id)
);