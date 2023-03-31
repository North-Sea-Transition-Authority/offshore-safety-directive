# Migration steps

This guide details the steps to migrate the NSTA appointment spreadsheet into the WIOS
database.

The NSTA will be providing a spreadsheet of the current operator appointments for wellbore, installation and subarea
forward approvals. The migration spreadsheet can be found in [Confluence](https://confluence.fivium.co.uk/display/BESPOKE/NSTA).

This migration guide will make use of both the Energy Portal Oracle database and the WIOS Postgres database.

## 1. Create the migration schema on the Energy Portal database

Run the following patches to construct the required tables for the migration. Note, this will need to be in a schema
such as `XVIEWMGR` which has permission to make tables in other schemas.

- `/energyportal/V01_create_migration_schema.sql`
- `/energyportal/V02_create_raw_mirgation_data_tables.sql`

## 2. Write the raw appointment data to the Energy Portal database

### Wellbore appointments prerequisites
- Remove columns C-L and columns S-U in the well migration template sheet
- Add an ID column in column A with a series of unique integer IDs

### Installation appointments prerequisites
- Remove columns I and J in the installation migration template sheet
- Add an ID column in column A with a series of unique integer IDs

### Forward approval appointments prerequisites
- Remove columns N in the forward approval migration template sheet
- Add an ID column in column A with a series of unique integer IDs

### Migrate the raw data into the database
- Export a CSV of the appointments. Note you will need to change the default comma delimiter as organisation names
  can have commas in. Using the pipe character (|) works well in this case. To change this see [Regional settings for list separator
  ](https://exceljet.net/glossary/list-separator) guidance
- Once you have generated a CSV of the appointments copy the contents and insert a row into the `wios_migration.migration_csv_files`
  table in the Energy Portal Oracle database. The `type` column should be set to something describing the content for
  example `WELLBORE_APPOINTMENTS`.
- Run the code below in `XVIEWMGR` to insert the CSV into the raw data table in the Energy Portal database. Note,
  depending on the source being migrated the tables will be one of the following and the type needs to be set to the
  value from the previous step (e.g `WELLBORE_APPOINTMENTS`).
    - `wios_migration.raw_wellbore_appointments_data`
    - `wios_migration.raw_installation_appointments_data`
    - `wios_migration.raw_subarea_appointments_data`

```oraclesqlplus
DECLARE

  l_csv_data CLOB;

BEGIN

  SELECT mcf.migration_csv
  INTO l_csv_data
  FROM wios_migration.migration_csv_files mcf
  WHERE mcf.type = 'WELLBORE_APPOINTMENTS'; 

  xviewmgr.load_csv_to_table(
    p_table_name => 'wios_migration.raw_wellbore_appointments_data'
  , p_csv_data => l_csv_data
  , p_delimeter => '|'
  );
  
  COMMIT;  
  
END;
```

## 3. Cleanse the wellbore data

Prior to cleansing the wellbore data the following two patches need to be run on the Energy Portal database:

- `/energyportal/V03_create_wellbore_migration_table.sql`
- `/energyportal/V04_create_wellbore_clense_package.sql`

To cleanse the raw wellbore appointment data you can execute the following code:

```oraclesqlplus
EXEC wios_migration.wellbore_appointment_migration.cleanse_wellbore_appointments;
```

For each row in the `wios_migration.raw_wellbore_appointments_data` table, a cleansed row will be inserted into
`wios_migration.wellbore_appointments` table.

Any rows in the `wios_migration.raw_wellbore_appointments_data` which don't map to a valid WONS wellbore will be 
written to `wios_migration.unmatched_wellbores` as well as an error row written to `wios_migration.wellbore_migration_errors`.

Any errors in the migration process will be written to `wios_migration.wellbore_migration_errors`.
