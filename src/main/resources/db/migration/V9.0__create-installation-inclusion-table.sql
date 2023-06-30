CREATE TABLE installation_inclusion (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id INT NOT NULL
, include_installations_in_nomination BOOLEAN
, CONSTRAINT installation_inclusion_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX installation_inclusion_nomination_detail_id_idx ON installation_inclusion (nomination_detail_id);