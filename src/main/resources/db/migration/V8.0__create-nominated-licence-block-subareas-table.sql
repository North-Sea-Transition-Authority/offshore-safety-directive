CREATE TABLE nominated_licence_block_subareas (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, nomination_detail_id INT NOT NULL
, block_subarea_id TEXT
, CONSTRAINT nominated_licence_block_subareas_nomination_detail_id_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);

CREATE INDEX nominated_licence_block_subareas_nomination_detail_id_idx
ON nominated_licence_block_subareas (nomination_detail_id);