CREATE TABLE team_scopes (
  id UUID PRIMARY KEY
, team_id UUID NOT NULL
, portal_id TEXT NOT NULL
, portal_team_type TEXT NOT NULL
, CONSTRAINT team_scopes_teams FOREIGN KEY (team_id) REFERENCES teams(uuid)
);

CREATE INDEX team_scopes_team_id_idx ON team_scopes(team_id);