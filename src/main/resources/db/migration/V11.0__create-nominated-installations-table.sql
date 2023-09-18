CREATE TABLE nominated_installations (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, installation_id INT
, CONSTRAINT nominated_installations_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominated_installations_nomination_detail_id_idx ON nominated_installations (nomination_detail_id);