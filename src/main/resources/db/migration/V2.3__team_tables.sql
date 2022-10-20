CREATE TABLE osd.teams (
  uuid UUID PRIMARY KEY
, type VARCHAR NOT NULL
);

CREATE TABLE osd.team_member_roles (
  uuid UUID PRIMARY KEY
, wua_id INT NOT NULL
, team_id UUID NOT NULL REFERENCES osd.teams(uuid)
, role TEXT NOT NULL
);

CREATE INDEX osd_t_team_member_roles_idx ON osd.team_member_roles(team_id);