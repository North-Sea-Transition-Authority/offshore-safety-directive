-- Create stub user
INSERT INTO osd.team_member_roles(uuid, team_id, wua_id, role)
VALUES (
  gen_random_uuid()
  , (
      WITH iterable_teams AS (
          SELECT
            row_number() OVER () row
          , t.*
          FROM osd.teams t
      ), max_reg_team AS (
          SELECT MAX(it.row), it.uuid
          FROM iterable_teams it
          WHERE it.type = 'REGULATOR'
          GROUP BY it.uuid
      )
    SELECT mrt.uuid
    FROM max_reg_team mrt
  )
  , :user_id
  , :role
);