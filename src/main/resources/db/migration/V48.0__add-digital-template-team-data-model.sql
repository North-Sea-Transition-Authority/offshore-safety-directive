CREATE TABLE teams (
  id UUID PRIMARY KEY,
  type TEXT NOT NULL,
  name TEXT NOT NULL,
  scope_type TEXT,
  scope_id TEXT
);

CREATE UNIQUE INDEX teams_scoped_unique
ON teams (type, scope_type, scope_id)
WHERE (scope_type IS NOT NULL);

CREATE UNIQUE INDEX teams_static_unique
ON teams (type)
WHERE (scope_type IS NULL);

CREATE TABLE teams_aud (
  rev SERIAL,
  revtype NUMERIC,
  id UUID,
  type TEXT,
  name TEXT,
  scope_type TEXT,
  scope_id TEXT,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE TABLE team_roles (
  id UUID PRIMARY KEY,
  team_id UUID NOT NULL REFERENCES teams(id),
  role TEXT NOT NULL,
  wua_id BIGINT NOT NULL
);

CREATE INDEX team_roles_team_id_idx ON team_roles(team_id);

CREATE TABLE team_roles_aud (
  rev SERIAL,
  revtype NUMERIC,
  id UUID,
  team_id UUID,
  role TEXT,
  wua_id BIGINT,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);