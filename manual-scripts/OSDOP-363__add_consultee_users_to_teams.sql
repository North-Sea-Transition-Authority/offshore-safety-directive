DO $$
DECLARE
    l_consultee_wua_id INTEGER := 53292;
    l_consultee_coordinator_wua_id INTEGER := 53294;
    l_consultee_access_manager_wua_id INTEGER := 53293;

    l_consultee_team_uuid UUID := :consultee_team_uuid;
BEGIN

    INSERT INTO osd.team_member_roles (uuid, wua_id, team_id, role)
    VALUES (gen_random_uuid(), l_consultee_wua_id, l_consultee_team_uuid, 'CONSULTEE');

    INSERT INTO osd.team_member_roles (uuid, wua_id, team_id, role)
    VALUES (gen_random_uuid(), l_consultee_coordinator_wua_id, l_consultee_team_uuid, 'CONSULTATION_COORDINATOR');

    INSERT INTO osd.team_member_roles (uuid, wua_id, team_id, role)
    VALUES (gen_random_uuid(), l_consultee_access_manager_wua_id, l_consultee_team_uuid, 'ACCESS_MANAGER');

    COMMIT;
END;
$$