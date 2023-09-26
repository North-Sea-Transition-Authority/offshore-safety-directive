ALTER TABLE appointments
ADD COLUMN status TEXT;

UPDATE appointments
SET status = 'EXTANT'
WHERE id NOT IN (
  SELECT at.appointment_id
  FROM appointment_terminations at
  WHERE at.appointment_id = appointments.id
);

UPDATE appointments
SET status = 'TERMINATED'
WHERE id IN (
    SELECT at.appointment_id
    FROM appointment_terminations at
    WHERE at.appointment_id = appointments.id
);

ALTER TABLE appointments
ALTER COLUMN status SET NOT NULL;