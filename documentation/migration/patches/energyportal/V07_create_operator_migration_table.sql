GRANT SELECT ON decmgr.organisation_units TO wios_migration;

CREATE TABLE wios_migration.unmatched_organisation_units (
  operator_name CLOB
, possible_matches CLOB
) TABLESPACE tbsdata;