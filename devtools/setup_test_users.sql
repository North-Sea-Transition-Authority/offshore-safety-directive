DO $$
  DECLARE

    l_regulator_team_id uuid;
    l_consultee_team_id uuid;
    l_shell_industry_team uuid;
    l_bp_industry_team uuid;
    l_chevron_industry_team uuid;

  BEGIN

    SELECT id
    INTO l_regulator_team_id
    FROM osd.teams
    WHERE type = 'REGULATOR';

    /* access.manager@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_regulator_team_id,
      'TEAM_MANAGER',
      53174
    );

    /* nomination.manager@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_regulator_team_id,
      'NOMINATION_MANAGER',
      53173
    );

    /* third.party.access@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_regulator_team_id,
      'THIRD_PARTY_TEAM_MANAGER',
      53312
    );

    /* nomination.viewer@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_regulator_team_id,
      'VIEW_ANY_NOMINATION',
      53172
    );

    /* appointment.manager@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_regulator_team_id,
      'APPOINTMENT_MANAGER',
      53454
    );

    SELECT id
    INTO l_consultee_team_id
    FROM osd.teams
    WHERE type = 'CONSULTEE';

    /* consultee.manager@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_consultee_team_id,
      'TEAM_MANAGER',
      53293
    );

    /* consultee.coordinator@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_consultee_team_id,
      'CONSULTATION_MANAGER',
      53294
    );

    /* consultee@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
      gen_random_uuid(),
      l_consultee_team_id,
      'CONSULTATION_PARTICIPANT',
      53292
    );

    INSERT INTO osd.teams (id, name, type, scope_id, scope_type)
    VALUES (gen_random_uuid(), 'ROYAL DUTCH SHELL', 'ORGANISATION_GROUP', '116', 'ORGANISATION_GROUP')
    RETURNING id INTO l_shell_industry_team;

    /* access.manager.shell@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_shell_industry_team,
         'TEAM_MANAGER',
         53640
     );

    /* nomination.editor.shell@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_shell_industry_team,
         'NOMINATION_EDITOR',
         53642
     );

    /* nomination.submitter.shell@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_shell_industry_team,
         'NOMINATION_SUBMITTER',
         53641
     );

    /* nomination.viewer.shell@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_shell_industry_team,
         'NOMINATION_VIEWER',
         53643
     );

    INSERT INTO osd.teams (id, type, name, scope_id, scope_type)
    VALUES (gen_random_uuid(), 'ORGANISATION_GROUP', 'BP EXPLORATION',  '50', 'ORGANISATION_GROUP')
        RETURNING id INTO l_bp_industry_team;

    /* access.manager.bp@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
        gen_random_uuid(),
        l_bp_industry_team,
        'TEAM_MANAGER',
        53632
     );

    /* nomination.editor.bp@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_bp_industry_team,
         'NOMINATION_EDITOR',
         53634
     );

    /* nomination.submitter.bp@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_bp_industry_team,
         'NOMINATION_SUBMITTER',
         53633
     );

    /* nomination.viewer.bp@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_bp_industry_team,
         'NOMINATION_VIEWER',
         53635
     );

    INSERT INTO osd.teams (id, type, name, scope_id, scope_type)
    VALUES (gen_random_uuid(), 'ORGANISATION_GROUP', 'CHEVRON CORPORATION',  '56', 'ORGANISATION_GROUP')
        RETURNING id INTO l_chevron_industry_team;

    /* access.manager.chevron@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_chevron_industry_team,
         'TEAM_MANAGER',
         53636
     );

    /* nomination.editor.chevron@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_chevron_industry_team,
         'NOMINATION_EDITOR',
         53638
     );

    /* nomination.submitter.chevron@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_chevron_industry_team,
         'NOMINATION_SUBMITTER',
         53637
     );

    /* nomination.viewer.chevron@wios.co.uk */
    INSERT INTO osd.team_roles VALUES (
         gen_random_uuid(),
         l_chevron_industry_team,
         'NOMINATION_VIEWER',
         53639
     );

  END;
$$