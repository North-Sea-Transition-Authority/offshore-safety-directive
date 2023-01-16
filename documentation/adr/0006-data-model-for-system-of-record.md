## OSDOP-254: Data model for phase 1 system of record

* Status: DRAFT
* Approved by:

### Problem statement

Once a nomination takes effect we need to add a record for all the assets (wellbores, installations and subareas) 
included in the nomination to the System or Record (SoR). We need a data model that will allow effective and efficient
searching while including all the required information in the 
[SoR data dictionary](https://docs.google.com/spreadsheets/d/1ZuWqmWp5KXquemD3jwxulEOwnEe-oZLRCs5n8z8AjXo/edit#gid=0).

Note: The data check and supplementary documents data capture requirements have not been included in this ADR as
these are part of phase 2 and hence will not be built out until after phase 1 has gone live.

### Solution

_Assets_

For each asset that you can nominate (currently WONS wellbores, DEVUK installations, PEARS subareas) a row will be
inserted into the below table. This allows easy on-boarding of other asset types in the future without a data model
change. 

```sql
|_______________ASSETS________________|
| id: UUID PRIMARY KEY NOT NULL       |
| portal_asset_id: VARCHAR NOT NULL   |
| portal_asset_type: VARCHAR NOT NULL |
```

The `portal_asset_id` will be the ID of the wellbore, installation or subarea. This is a string column to cater
for the string IDs in the subareas API. 

The `portal_asset_type` will simply be either `WELLBORE`, `INSTALLATION` or `SUBAREA` so we know 
what the asset is and hence where to get the information from when interacting with EPA.

_Appointments_

This table will include the information about the appointment for a specific asset. As part of phase 2 of the project,
we will be implementing corrections and terminations of appointments. These actions will be done on a per asset level. 
As a result we will need to be able to change appointment information post nomination. There were some concerns about 
having the appointment information duplicated when a nomination becomes an appointment, but due to a single appointment
for an asset needing to possibly change later this is the easiest model to work with. The alternative was to have a
mapping table of appointments to assets, but we would have to copy forward data and then update if one of the assets 
was to change. Equally, there is minimal data which would be duplicate so hence not too much of a concern.

```sql
|________________APPOINTMENTS________________|
| id: UUID PRIMARY KEY NOT NULL              |
| asset_id: UUID NOT NULL                    |
| appointed_portal_operator_id: INT NOT NULL |
| responsible_from_date: DATE NOT NULL       |
| responsible_to_date: DATE                  |
| type: VARCHAR NOT NULL                     |
| created_by_nomination_id: INT              |
| created_by_legacy_nomination: VARCHAR      |          
| created_by_appointment_id: UUID            |          
```

The `type` will be either `DEEMED` (NSTA inferred appointment when regulations came in), `NOMINATED` (either a legacy
nomination or a WIOS nomination) or `FORWARD_APPROVED` (for wellbores drilled in a subarea which has a forward approval
and no new operator is required).

The `created_by_nomination_id` column will hold a link to the WIOS nomination that resulted in this appointment. This
will be `NULL` for all migrated appointments.

The `created_by_legacy_nomination` column will contain the legacy NSTA reference for the nomination. This will be 
`NULL` for `DEEMED` appointments and for appointments from a WIOS nomination.

The `created_by_appointment_id` is used in scenarios where we have an existing appointment for a forward approval
subarea and a new wellbore is drilled in that subarea. In WONS, the applicant will be shown the existing appointed 
operator from the subarea approval and if this is not going to change an appointment for that wellbore will be written
into the WIOS system of record. The appointment type would be `FORWARD_APPROVED` and we will set the 
`created_by_appointment_id` value to the appointment ID for the forward approved subarea. We don't need to show this
on screen currently but could be useful for auditing, debug and reporting purposes.

_Asset phases_

When an asset is included as part of a nomination the applicant is required to select the lifecycle phases the
nomination covers. The phases are different per asset type, and you can select more than one phase per asset
per nomination.

```sql
|__________ASSET_PHASES__________|
| id: UUID PRIMARY KEY NOT NULL  |
| asset_id: UUID NOT NULL        |
| appointment_id UUID NOT NULL   |  
| phase: VARCHAR NOT NULL        |
```
#### Worked example

_Scenario:_

Installation `Alba Northern Platform` (Energy Portal ID: 1234) had a deemed nomination from 2015, a subsequent
nomination in the legacy service and a subsequent new nomination through WIOS.

_Assets table:_

There will be one row for the installation in the `ASSETS` table.

```sql
|___________________ASSETS___________________|
| id | portal_assert_id | portal_asset_type  |
| 10 | 1234             | INSTALLATION       |
```

_Appointments table:_

There will be three rows in the `APPOINTMENTS` table. Two migrated appointments and one WIOS appointment.

```sql
|___________________________________________________________________________________APPOINTMENTS____________________________________________________________________________________|
| id | asset_id | appointed_portal_operator_id | responsible_from_date | responsible_to_date | type      | created_by_nomination_id | created_by_legacy_nomination | created_by_appointment_id |
| 20 | 10       | 5678                         | 2015-07-19            | 2018-01-20          | DEEMED    | NULL                     | NULL                         | NULL                      |
| 21 | 10       | 9012                         | 2018-01-20            | 2020-11-01          | NOMINATED | NULL                     | OSD/2020/1                   | NULL                      |
| 22 | 10       | 3456                         | 2020-11-01            |                     | NOMINATED | 17                       | NULL                         | NULL                      |
```

_Asset phases table:_

For each appointment (migrated and new) there will be one or more asset lifecycle phases the appointment relates to.
In this case it assumes that the first deemed appointment includes `EXPLORATION` and `DEVELOPMENT` phases. The following
legacy nomination is only for `DEVELOPMENT` (as the asset is no longer in the `EXPLORATION` phase). The following WIOS
nomination covers both the `DEVELOPMENT` and `DECOMMISSIONING` phases.

```sql
|__________________ASSET_PHASES____________________|
| id | asset_id | appointment_id | phase           |  
| 1  | 10       | 20             | EXPLORATION     |  
| 2  | 10       | 20             | DEVELOPMENT     |  
| 3  | 10       | 21             | DEVELOPMENT     |  
| 4  | 10       | 22             | DEVELOPMENT     |  
| 5  | 10       | 22             | DECOMMISSIONING |  
```