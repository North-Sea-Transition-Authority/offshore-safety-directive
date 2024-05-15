CREATE TABLE nomination_submission_information (
  id UUID PRIMARY KEY
, nomination_detail_id UUID NOT NULL
, authority_confirmed BOOLEAN NOT NULL
, fast_track_reason TEXT
, CONSTRAINT nomination_submission_information_nomination_detail_id_fk FOREIGN KEY(nomination_detail_id) REFERENCES nomination_details(id)
);

CREATE INDEX nomination_submission_information_nomination_detail_idx ON nomination_submission_information(nomination_detail_id);