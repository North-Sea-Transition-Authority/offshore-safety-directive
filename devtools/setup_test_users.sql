DO $$
  DECLARE

    l_regulator_team_id uuid;
    l_consultee_team_id uuid;
    l_shell_industry_team uuid;

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

    INSERT INTO osd.teams (uuid, type, display_name)
    VALUES (gen_random_uuid(), 'INDUSTRY', 'ROYAL DUTCH SHELL')
    RETURNING uuid INTO l_shell_industry_team;

    INSERT INTO osd.team_scopes (id, team_id, portal_id, portal_team_type)
    VALUES (gen_random_uuid(), l_shell_industry_team, '116', 'ORGANISATION_GROUP');

    /* access.manager.shell@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
         gen_random_uuid(),
         53640,
         l_shell_industry_team,
         'ACCESS_MANAGER'
     );

    /* nomination.editor.shell@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
         gen_random_uuid(),
         53642,
         l_shell_industry_team,
         'NOMINATION_EDITOR'
     );

    /* nomination.submitter.shell@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
         gen_random_uuid(),
         53641,
         l_shell_industry_team,
         'NOMINATION_SUBMITTER'
     );

    /* nomination.viewer.shell@wios.co.uk */
    INSERT INTO osd.team_member_roles VALUES (
         gen_random_uuid(),
         53643,
         l_shell_industry_team,
         'NOMINATION_VIEWER'
     );

  END;
$$