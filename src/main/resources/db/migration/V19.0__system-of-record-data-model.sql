CREATE TABLE assets (
  id UUID PRIMARY KEY
, portal_asset_id TEXT NOT NULL
, portal_asset_type VARCHAR(255) NOT NULL
, asset_name TEXT NOT NULL
);

CREATE INDEX assets_portal_asset_id_idx
  ON assets (portal_asset_id);

CREATE INDEX assets_portal_asset_type_idx
  ON assets (portal_asset_type);

CREATE TABLE appointments (
  id UUID PRIMARY KEY
, asset_id UUID NOT NULL
, appointed_portal_operator_id VARCHAR(255) NOT NULL
, responsible_from_date DATE NOT NULL
, responsible_to_date DATE
, type VARCHAR(255) NOT NULL
, created_by_nomination_id UUID
, created_by_legacy_nomination_reference TEXT
, created_by_appointment_id UUID
, created_datetime TIMESTAMP NOT NULL
, CONSTRAINT appointment_asset_id_fk
    FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE INDEX appointments_appointed_portal_operator_id_idx
  ON appointments (appointed_portal_operator_id);

CREATE INDEX appointments_appointment_type_idx
  ON appointments (type);

CREATE TABLE asset_phases (
  id UUID PRIMARY KEY
, asset_id UUID NOT NULL
, appointment_id UUID NOT NULL
, phase VARCHAR(255) NOT NULL
, CONSTRAINT asset_phase_asset_id_fk
    FOREIGN KEY (asset_id) REFERENCES assets (id)
, CONSTRAINT asset_phase_appointment_id_fk
    FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);