CREATE TABLE teams (
  uuid UUID PRIMARY KEY
, type VARCHAR NOT NULL
, display_name TEXT
);

CREATE TABLE team_member_roles (
  uuid UUID PRIMARY KEY
, wua_id INT NOT NULL
, team_id UUID NOT NULL REFERENCES teams(uuid)
, role TEXT NOT NULL
);

CREATE INDEX team_member_roles_team_id_idx ON team_member_roles(team_id);

INSERT INTO teams (uuid, type)
VALUES (gen_random_uuid(), 'REGULATOR');

INSERT INTO teams (uuid, type, display_name)
VALUES (gen_random_uuid(), 'CONSULTEE', 'Offshore Major Accident Regulator (OMAR)');