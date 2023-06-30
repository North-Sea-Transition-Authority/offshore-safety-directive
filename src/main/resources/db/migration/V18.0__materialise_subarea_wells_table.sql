CREATE TABLE nominated_subarea_wells (
  uuid UUID PRIMARY KEY
, nomination_detail_id INT NOT NULL
, wellbore_id INT NOT NULL
, CONSTRAINT nominated_subarea_wells_nomination_detail_fk
    FOREIGN KEY (nomination_detail_id) REFERENCES nomination_details (id)
);