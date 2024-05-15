GRANT SELECT ON decmgr.xview_organisation_names TO wios_migration;
GRANT SELECT ON decmgr.xview_organisation_units TO wios_migration;

CREATE TABLE wios_migration.unmatched_organisation_units (
  operator_name CLOB
, possible_matches CLOB
) TABLESPACE tbsdata;

CREATE TABLE wios_migration.historical_company_name_mappings (
  name_in_spreadsheet VARCHAR2(4000)
, name_in_sor VARCHAR2(4000)
) TABLESPACE tbsdata;