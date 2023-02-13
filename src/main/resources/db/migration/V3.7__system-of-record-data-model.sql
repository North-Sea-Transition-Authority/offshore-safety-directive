CREATE TABLE osd.assets (
  id UUID PRIMARY KEY
, portal_asset_id TEXT NOT NULL
, portal_asset_type VARCHAR(255) NOT NULL
);

CREATE INDEX portal_asset_id_index
  ON osd.assets (portal_asset_id);

CREATE INDEX portal_asset_type_index
  ON osd.assets (portal_asset_type);

CREATE TABLE osd.appointments (
  id UUID PRIMARY KEY
, asset_id UUID NOT NULL
, appointed_portal_operator_id VARCHAR(255) NOT NULL
, responsible_from_date DATE NOT NULL
, responsible_to_date DATE
, type VARCHAR(255) NOT NULL
, created_by_nomination_id INT
, created_by_legacy_nomination_reference TEXT
, created_by_appointment_id UUID
, CONSTRAINT appointment_asset_id_fk
    FOREIGN KEY (asset_id) REFERENCES osd.assets (id)
);

CREATE INDEX appointed_portal_operator_id_index
  ON osd.appointments (appointed_portal_operator_id);

CREATE INDEX appointment_type_index
  ON osd.appointments (type);

CREATE TABLE osd.asset_phases (
  id UUID PRIMARY KEY
, asset_id UUID NOT NULL
, appointment_id UUID NOT NULL
, phase VARCHAR(255) NOT NULL
, CONSTRAINT asset_phase_asset_id_fk
    FOREIGN KEY (asset_id) REFERENCES osd.assets (id)
, CONSTRAINT asset_phase_appointment_id_fk
    FOREIGN KEY (appointment_id) REFERENCES osd.appointments (id)
);