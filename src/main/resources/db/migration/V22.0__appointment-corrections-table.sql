CREATE TABLE appointment_corrections (
  id UUID PRIMARY KEY
, appointment_id UUID NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, corrected_by_wua_id INT NOT NULL
, reason_for_correction TEXT NOT NULL
, CONSTRAINT appointment_corrections_appointment_id FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX appointment_corrections_appointment_id_appointments_idx ON appointment_corrections(appointment_id);