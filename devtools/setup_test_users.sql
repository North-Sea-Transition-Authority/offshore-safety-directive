DO $$
  DECLARE

    l_regulator_team_id uuid;
    l_consultee_team_id uuid;

  BEGIN

    SELECT uuid
    INTO l_regulator_team_id
    FROM osd.teams
    WHERE type = 'REGULATOR';

    /* access.manager@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53174,
      l_regulator_team_id,
      'ACCESS_MANAGER'
    );

    /* nomination.manager@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53173,
      l_regulator_team_id,
      'MANAGE_NOMINATION'
    );

    /* third.party.access@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53312,
      l_regulator_team_id,
      'THIRD_PARTY_ACCESS_MANAGER'
    );

    /* nomination.viewer@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53172,
      l_regulator_team_id,
      'VIEW_NOMINATION'
    );

    /* appointment.manager@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53454,
      l_regulator_team_id,
      'MANAGE_ASSET_APPOINTMENTS'
    );

    SELECT uuid
    INTO l_consultee_team_id
    FROM osd.teams
    WHERE type = 'CONSULTEE';

    /* consultee.manager@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53293,
      l_consultee_team_id,
      'ACCESS_MANAGER'
    );

    /* consultee.coordinator@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53294,
      l_consultee_team_id,
      'CONSULTATION_COORDINATOR'
    );

    /* consultee@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
      gen_random_uuid(),
      53292,
      l_consultee_team_id,
      'CONSULTEE'
    );

  END;
$$