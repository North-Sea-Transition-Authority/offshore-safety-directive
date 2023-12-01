DO $$
  DECLARE

    l_regulator_team_id uuid;
    l_consultee_team_id uuid;
    l_shell_industry_team uuid;
    l_bp_industry_team uuid;
    l_chevron_industry_team uuid;

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

INSERT INTO osd.teams (uuid, type, display_name)
VALUES (gen_random_uuid(), 'INDUSTRY', 'BP EXPLORATION')
    RETURNING uuid INTO l_bp_industry_team;

INSERT INTO osd.team_scopes (id, team_id, portal_id, portal_team_type)
VALUES (gen_random_uuid(), l_bp_industry_team, '50', 'ORGANISATION_GROUP');

/* access.manager.bp@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
    gen_random_uuid(),
    53632,
    l_bp_industry_team,
    'ACCESS_MANAGER'
 );

/* nomination.editor.bp@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53634,
     l_bp_industry_team,
     'NOMINATION_EDITOR'
 );

/* nomination.submitter.bp@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53633,
     l_bp_industry_team,
     'NOMINATION_SUBMITTER'
 );

/* nomination.viewer.bp@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53635,
     l_bp_industry_team,
     'NOMINATION_VIEWER'
 );

INSERT INTO osd.teams (uuid, type, display_name)
VALUES (gen_random_uuid(), 'INDUSTRY', 'CHEVRON CORPORATION')
    RETURNING uuid INTO l_chevron_industry_team;

INSERT INTO osd.team_scopes (id, team_id, portal_id, portal_team_type)
VALUES (gen_random_uuid(), l_chevron_industry_team, '56', 'ORGANISATION_GROUP');

/* access.manager.chevron@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53636,
     l_chevron_industry_team,
     'ACCESS_MANAGER'
 );

/* nomination.editor.chevron@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53638,
     l_chevron_industry_team,
     'NOMINATION_EDITOR'
 );

/* nomination.submitter.chevron@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53637,
     l_chevron_industry_team,
     'NOMINATION_SUBMITTER'
 );

/* nomination.viewer.chevron@wios.co.uk */
INSERT INTO osd.team_member_roles VALUES (
     gen_random_uuid(),
     53639,
     l_chevron_industry_team,
     'NOMINATION_VIEWER'
 );

  END;
$$