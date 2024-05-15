CREATE TABLE nomination_portal_references (
  uuid UUID PRIMARY KEY
, nomination_id UUID NOT NULL
, portal_reference_type TEXT NOT NULL
, portal_references TEXT
, CONSTRAINT nomination_portal_references_nomination_id_fk FOREIGN KEY (nomination_id) REFERENCES nominations (id)
);