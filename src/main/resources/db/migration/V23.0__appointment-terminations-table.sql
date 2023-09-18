CREATE TABLE appointment_terminations (
  id UUID PRIMARY KEY
, appointment_id UUID NOT NULL
, reason_for_termination TEXT NOT NULL
, termination_date DATE NOT NULL
, corrected_by_wua_id INT NOT NULL
, created_timestamp TIMESTAMPTZ NOT NULL
, CONSTRAINT appointment_terminations_appointment_id FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX appointment_terminations_appointment_id_idx ON appointment_terminations(appointment_id);