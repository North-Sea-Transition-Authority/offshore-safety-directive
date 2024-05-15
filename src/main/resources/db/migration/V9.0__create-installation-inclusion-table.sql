CREATE TABLE installation_inclusion (
  id UUID PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, include_installations_in_nomination BOOLEAN
, CONSTRAINT installation_inclusion_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX installation_inclusion_nomination_detail_id_idx ON installation_inclusion (nomination_detail_id);