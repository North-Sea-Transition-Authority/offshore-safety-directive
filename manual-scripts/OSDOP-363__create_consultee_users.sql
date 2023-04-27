DECLARE

  l_user decmgr.contact_test_harness.t_user;
  l_resource_id NUMBER;
  l_user_wua_id NUMBER;

BEGIN

  l_user := decmgr.contact_test_harness.construct_user_type(
    p_login_id => 'consultee@wios.co.uk'
  , p_user_title => 'Dr'
  , p_user_forename => 'WIOS'
  , p_user_surname => 'Consultee'
  , p_organisation_name => 'Offshore Major Accident Regulator'
  );

  l_user_wua_id := decmgr.contact_test_harness.get_or_create_user_in_role(
    p_resource_type => 'WIOS_ACCESS_TEAM'
  , p_resource_roles => bpmmgr.varchar2_list_type('SERVICE_ACCESS')
  , p_primary_uref => NULL
  , p_secondary_uref => NULL
  , p_tertiary_uref => NULL
  , p_user => l_user
  , po_resource_id => l_resource_id
  );

  l_user := decmgr.contact_test_harness.construct_user_type(
    p_login_id => 'consultee.manager@wios.co.uk'
  , p_user_title => 'Dr'
  , p_user_forename => 'Consultee'
  , p_user_surname => 'Access Manager'
  , p_organisation_name => 'Offshore Major Accident Regulator'
  );

  l_user_wua_id := decmgr.contact_test_harness.get_or_create_user_in_role(
    p_resource_type => 'WIOS_ACCESS_TEAM'
  , p_resource_roles => bpmmgr.varchar2_list_type('SERVICE_ACCESS')
  , p_primary_uref => NULL
  , p_secondary_uref => NULL
  , p_tertiary_uref => NULL
  , p_user => l_user
  , po_resource_id => l_resource_id
  );

  l_user := decmgr.contact_test_harness.construct_user_type(
    p_login_id => 'consultee.coordinator@wios.co.uk'
  , p_user_title => 'Dr'
  , p_user_forename => 'Consultee'
  , p_user_surname => 'Coordinator'
  , p_organisation_name => 'Offshore Major Accident Regulator'
  );

  l_user_wua_id := decmgr.contact_test_harness.get_or_create_user_in_role(
    p_resource_type => 'WIOS_ACCESS_TEAM'
  , p_resource_roles => bpmmgr.varchar2_list_type('SERVICE_ACCESS')
  , p_primary_uref => NULL
  , p_secondary_uref => NULL
  , p_tertiary_uref => NULL
  , p_user => l_user
  , po_resource_id => l_resource_id
  );

END;
/

COMMIT
/