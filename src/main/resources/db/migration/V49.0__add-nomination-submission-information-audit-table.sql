CREATE TABLE nomination_submission_information_aud (
  rev SERIAL,
  revtype NUMERIC,
  id UUID,
  nomination_detail_id UUID,
  authority_confirmed BOOLEAN,
  fast_track_reason TEXT,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);