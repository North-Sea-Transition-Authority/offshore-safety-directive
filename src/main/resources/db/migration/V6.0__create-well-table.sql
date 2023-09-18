CREATE TABLE nominated_wells (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, well_id INT
, CONSTRAINT nominated_wells_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominated_wells_nomination_nomination_detail_id_idx ON nominated_wells (nomination_detail_id);