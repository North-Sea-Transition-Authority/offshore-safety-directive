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
- Add an ID column in column A with a series of unique integer IDs

### Installation appointments prerequisites
- Add an ID column in column A with a series of unique integer IDs

### Forward approval appointments prerequisites
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

Prior to cleansing the wellbore data the following patches need to be run on the Energy Portal database:

- `/energyportal/V03_create_wellbore_migration_table.sql`
- `/energyportal/V04_create_wellbore_cleanse_package.sql`
- `/energyportal/V07_create_operator_migration_table.sql`
- `/energyportal/V08_create_operator_mapping_package.sql`

To cleanse the raw wellbore appointment data you can execute the following code:

```oraclesqlplus
EXEC wios_migration.wellbore_appointment_migration.cleanse_wellbore_appointments;
```

For each row in the `wios_migration.raw_wellbore_appointments_data` table, a cleansed row will be inserted into
`wios_migration.wellbore_appointments` table.

Any rows in the `wios_migration.raw_wellbore_appointments_data` which don't map to a valid WONS wellbore will be 
written to `wios_migration.unmatched_wellbores` as well as an error row written to `wios_migration.wellbore_migration_errors`.

Any errors in the migration process will be written to `wios_migration.wellbore_migration_errors`.

## 4. Cleanse the installation data

Prior to cleansing the installation data the following patches need to be run on the Energy Portal database:

- `/energyportal/V05_create_installation_migration_table.sql`
- `/energyportal/V06_create_installation_cleanse_package.sql`
- `/energyportal/V07_create_operator_migration_table.sql`
- `/energyportal/V08_create_operator_mapping_package.sql`

To cleanse the raw installation appointment data you can execute the following code:

```oraclesqlplus
EXEC wios_migration.installation_appointment_migration.cleanse_installation_appointments;
```

For each row in the `wios_migration.raw_installation_appointments_data` table, a cleansed row will be inserted into
`wios_migration.installation_appointments` table.

Any rows in the `wios_migration.raw_installation_appointments_data` which don't map to a valid DEVUK installation
will be written to `wios_migration.unmatched_installations` as well as an error row written to 
`wios_migration.installation_migration_errors`.

Any errors in the migration process will be written to `wios_migration.installation_migration_errors`.

## 5. Cleanse the subarea forward approval data

Prior to cleansing the subarea data the following patches need to be run on the Energy Portal database:

- `/energyportal/V07_create_operator_migration_table.sql`
- `/energyportal/V08_create_operator_mapping_package.sql`
- `/energyportal/V12_create_subarea_migration_table.sql`
- `/energyportal/V13_create_subarea_cleanse_package.sql`

To cleanse the raw subarea appointment data you can execute the following code:

```oraclesqlplus
EXEC wios_migration.subarea_appointment_migration.cleanse_subarea_appointments;
```

For each row in the `wios_migration.raw_subarea_appointments_data` table, a cleansed row will be inserted into
`wios_migration.subarea_appointments` table.

Any rows in the `wios_migration.raw_subarea_appointments_data` which don't map to a valid PEARS subarea
will be written to `wios_migration.unmatched_subareas` as well as an error row written to
`wios_migration.subarea_migration_errors`.

## 6. Export the cleansed wellbore data

On the Oracle database run the following SQL which will return all the wellbore appointments in the format that the
WIOS migration table is expecting.

```oraclesqlplus
SELECT
  wa.migratable_appointment_id
, wa.wellbore_id
, wa.wellbore_registration_number
, wa.appointed_operator_id
, wa.appointed_operator_name
, TO_CHAR(wa.responsible_from_date, 'YYYY-MM-DD') responsible_from_date
, TO_CHAR(wa.responsible_to_date, 'YYYY-MM-DD') responsible_to_date
, wa.is_exploration_phase
, wa.is_development_phase
, wa.is_decommissioning_phase
, wa.appointment_source
, wa.legacy_nomination_reference
FROM wios_migration.wellbore_appointments wa
WHERE wa.migratable_appointment_id NOT IN(
  SELECT DISTINCT wme.migratable_appointment_id
  FROM wios_migration.wellbore_migration_errors wme
)
```
Export the results the above SQL to a CSV. This will be the CSV that we migrate into WIOS.

## 7. Export the cleansed installation data

On the Oracle database run the following SQL which will return all the installation appointments in the format that the
WIOS migration table is expecting.

```oraclesqlplus
SELECT
  ia.migratable_appointment_id
, ia.installation_id
, ia.installation_name
, ia.appointed_operator_id
, ia.appointed_operator_name
, TO_CHAR(ia.responsible_from_date, 'YYYY-MM-DD') responsible_from_date
, TO_CHAR(ia.responsible_to_date, 'YYYY-MM-DD') responsible_to_date
, ia.is_development_phase
, ia.is_decommissioning_phase
, ia.appointment_source
, ia.legacy_nomination_reference
FROM wios_migration.installation_appointments ia
WHERE ia.migratable_appointment_id NOT IN(
  SELECT DISTINCT ime.migratable_appointment_id
  FROM wios_migration.installation_migration_errors ime
)
```
Export the results the above SQL to a CSV. This will be the CSV that we migrate into WIOS.

## 8. Export the cleansed subarea forward approval data

On the Oracle database run the following SQL which will return all the subarea appointments in the format that the
WIOS migration table is expecting.

```oraclesqlplus
SELECT
  sa.migratable_appointment_id
, sa.subarea_id
, sa.subarea_reference
, sa.appointed_operator_id
, sa.appointed_operator_name
, TO_CHAR(sa.responsible_from_date, 'YYYY-MM-DD') responsible_from_date
, TO_CHAR(sa.responsible_to_date, 'YYYY-MM-DD') responsible_to_date
, sa.is_exploration_phase
, sa.is_development_phase
, sa.is_decommissioning_phase
, sa.appointment_source
, sa.legacy_nomination_reference
FROM wios_migration.subarea_appointments sa
WHERE sa.migratable_appointment_id NOT IN(
  SELECT DISTINCT sme.migratable_appointment_id
  FROM wios_migration.subarea_migration_errors sme
)
```
Export the results the above SQL to a CSV. This will be the CSV that we migrate into WIOS.

## 9. Load the appointment data into WIOS

Once we have the cleansed appointment data in CSV format we need to load this data in the WIOS postgres database. 
Before this can be done you need to execute the following script to create a migration schema and the required data 
model tables:

`energyportal/V09_create_wios_migration_tables.sql`

Once the schema and migration tables have been created, the CSVs from steps 6, 7 and 8 can be loaded into these 
tables using pgAdmin. The tables are as follows:

- wellbores: osd_migration.migratable_wellbore_appointments
- installations: osd_migration.migratable_installation_appointments
- forward approvals: osd_migration.migratable_subarea_appointments

## 10. Migrate the wellbore appointments

Execute the anonymous block in `energyportal/V10_create_wellbore_appointments.sql` which will take all the data in the
`osd_migration.migratable_wellbore_appointments` table and create rows in the following tables:

- `osd.assets`
- `osd.appointments`
- `osd.asset_phases`

## 11. Migrate the installation appointments

Execute the anonymous block in `energyportal/V11_create_installation_appointments.sql` which will take all the data in 
the `osd_migration.migratable_installation_appointments` table and create rows in the following tables:

- `osd.assets`
- `osd.appointments`
- `osd.asset_phases`

## 12. Migrate the subarea forward approval appointments

Execute the anonymous block in `energyportal/V13_create_subarea_appointments.sql` which will take all the data in
the `osd_migration.migratable_subarea_appointments` table and create rows in the following tables:

- `osd.assets`
- `osd.appointments`
- `osd.asset_phases`

## 13. Report migration errors to NSTA

Any migration errors owing to issues in the migration spreadsheet data need to be reported back to the NSTA. They will
need to fix up any data so the rows can be migrated in a subsequent migration run.