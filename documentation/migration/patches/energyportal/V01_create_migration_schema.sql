CREATE USER wios_migration IDENTIFIED BY "<password>"
DEFAULT TABLESPACE tbsdata
TEMPORARY TABLESPACE TEMP
PROFILE mgr_user
QUOTA UNLIMITED ON TBSBLOB
QUOTA UNLIMITED ON TBSCLOB
QUOTA UNLIMITED ON TBSDATA
QUOTA UNLIMITED ON TBSIDX;

GRANT CREATE SESSION TO wios_migration;