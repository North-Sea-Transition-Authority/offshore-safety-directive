/**
 Use this script as a base to insert example installation appointment rows into the SoR dataset
*/

INSERT INTO osd.assets(id, portal_asset_id, portal_asset_type)
VALUES ('12117df8-10ac-4534-a6cd-380d53be9a5f', '826', 'INSTALLATION');

INSERT INTO osd.appointments (
  id
, asset_id
, appointed_portal_operator_id
, responsible_from_date
, responsible_to_date
, type
)
VALUES(
  '22117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
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
  '32117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
, '22117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DEVELOPMENT_DESIGN'
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
  '42117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
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
  '52117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
, '42117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DEVELOPMENT_CONSTRUCTION'
);

INSERT INTO osd.asset_phases (
  id
, asset_id
, appointment_id
, phase
)
VALUES(
  '62117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
, '42117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DEVELOPMENT_INSTALLATION'
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
  '72117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
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
  '82117df8-10ac-4534-a6cd-380d53be9a5f'
, '12117df8-10ac-4534-a6cd-380d53be9a5f'
, '72117df8-10ac-4534-a6cd-380d53be9a5f'
, 'DECOMMISSIONING'
);