/**
 Use this script as a base to insert example subarea appointment rows into the SoR dataset
*/

INSERT INTO osd.assets(id, portal_asset_id, portal_asset_type)
VALUES ('13117df8-10ac-4534-a6cd-380d53be9a5f', 'REST-2/5-387', 'SUBAREA');

INSERT INTO osd.appointments (
  id
, asset_id
, appointed_portal_operator_id
, responsible_from_date
, responsible_to_date
, type
)
VALUES(
  '23117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '20'
, date '2015-07-19'
, date '2018-10-01'
, 'DEEMED'
);

INSERT INTO osd.asset_phases (
  id
, asset_id
, appointment_id
, phase
)
VALUES(
  '33117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '23117df8-10ac-4534-a6cd-380d53be9a5f'
, 'EXPLORATION_AND_APPRAISAL'
);

INSERT INTO osd.appointments (
  id
, asset_id
, appointed_portal_operator_id
, responsible_from_date
, responsible_to_date
, type
, created_by_legacy_nomination_reference
)
VALUES(
  '43117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '12'
, date '2018-10-01'
, date '2019-06-15'
, 'NOMINATED'
, 'OSD/2019/1'
);

INSERT INTO osd.asset_phases (
  id
, asset_id
, appointment_id
, phase
)
VALUES(
  '53117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '43117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DEVELOPMENT'
);

INSERT INTO osd.asset_phases (
  id
, asset_id
, appointment_id
, phase
)
VALUES(
  '63117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '43117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DECOMMISSIONING'
);

INSERT INTO osd.appointments (
  id
, asset_id
, appointed_portal_operator_id
, responsible_from_date
, type
, created_by_legacy_nomination_reference
)
VALUES(
  '73117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '10507'
, date '2019-06-15'
, 'NOMINATED'
, 'OSD/2019/2'
);

INSERT INTO osd.asset_phases (
  id
, asset_id
, appointment_id
, phase
)
VALUES(
  '83117df8-10ac-4534-a6cd-380d53be9a5f'
, '13117df8-10ac-4534-a6cd-380d53be9a5f'
, '73117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DECOMMISSIONING'
);