# Offshore Safety Directive (OSD) [![Build Status](https://drone-github.fivium.co.uk/api/badges/Fivium/offshore-safety-directive/status.svg?ref=refs/heads/develop)](https://drone-github.fivium.co.uk/Fivium/offshore-safety-directive)

## Background

In April 2010 an explosion on the Deepwater Horizon drilling rig in the Gulf of Mexico resulted in the largest marine
oil spill in the history of the petroleum industry. Millions of barrels of oil was released with widespread marine,
coastal and economic impact.

In response to the Deepwater Horizon accident the EU issued a Directive on the on safety of offshore oil and gas
operations. The EU Directive was implemented in UK law by the Offshore Petroleum Licensing
(Offshore Safety Directive) Regulations 2015.

## Pre-requisites

- Java 17
- Node LTS + NPM
- [Docker for Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
  (See [Docker setup](https://confluence.fivium.co.uk/display/JAVA/Java+development+environment+setup#Javadevelopmentenvironmentsetup-Docker)
  for further information about adding your account to the `docker-users` group)

## Setup

### 1. Run the backend services

- Ensure that you have [Docker for Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
  installed and running (or an alternative way of running docker).
- Run the backing services defined in the `local-dev-compose.yml`. This can be done by clicking the run icon next
  to `services` when in the file.
    - If IntelliJ doesn't detect the file as a docker compose file automatically you may need to
      [Associate docker-compose as file type](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360009394620-Associate-docker-compose-as-file-type)
      manually.

### 2. Add the required profile

### Mandatory regardless of profile

| Environment Variable                   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| OSD_GOV_NOTIFY_API_KEY                 | The GOV.UK Notify API key - find key for your environment in<br/> the [WIOS project](https://tpm.fivium.co.uk/index.php/prj/view/103). <br/><ul><li>Local dev<ul><li>[to mock sending emails](https://tpm.fivium.co.uk/index.php/pwd/view/2212)</li><li>[to actually send emails](https://tpm.fivium.co.uk/index.php/pwd/view/2124)</li></ul><li>[Dev](https://tpm.fivium.co.uk/index.php/pwd/view/2124)</li><li>[ST](https://tpm.fivium.co.uk/index.php/pwd/view/2125)</li><li>[Pre-prod](https://tpm.fivium.co.uk/index.php/pwd/view/2126)</li></ul> | 
| OSD_EPMQ_SNS_SQS_AWS_ACCESS_KEY_ID     | AWS access key id for SNS/SQS. Can be optional if run in the `disable-epmq` profile <ul><li>[Local dev](https://tpm.fivium.co.uk/index.php/prj/view/147)</li><li>[Dev](https://tpm.fivium.co.uk/index.php/prj/view/147)</li><li>[ST](https://tpm.fivium.co.uk/index.php/prj/view/147)</li><li>[Pre-prod](https://tpm.fivium.co.uk/index.php/pwd/view/2217)</li><li>[Prod](https://tpm.fivium.co.uk/index.php/pwd/view/2217)</li></ul>                                                                                                                  |
| OSD_EPMQ_SNS_SQS_AWS_SECRET_ACCESS_KEY | AWS secret access key for SNS/SQS. Can be optional if run in the `disable-epmq` profile <ul><li>[Local dev](https://tpm.fivium.co.uk/index.php/prj/view/147)</li><li>[Dev](https://tpm.fivium.co.uk/index.php/prj/view/147)</li><li>[ST](https://tpm.fivium.co.uk/index.php/prj/view/147)</li><li>[Pre-prod](https://tpm.fivium.co.uk/index.php/pwd/view/2217)</li><li>[Prod](https://tpm.fivium.co.uk/index.php/pwd/view/2217)</li></ul>                                                                                                              |
| OSD_EPMQ_ENVIRONMENT_SUFFIX            | Something unique per environment, e.g. `dev`. For local dev this can be your initials. Can be optional if run in the `disable-epmq` profile                                                                                                                                                                                                                                                                                                                                                                                                            |

### Development profile specific

- In your IntelliJ run configuration for the Spring app, include `development` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable                   | Description                                                                     |
|----------------------------------------|---------------------------------------------------------------------------------|
| OSD_EMAIL_TEST_RECIPIENTS              | If email is `test` mode, who to send emails to instead. Value can be a CSV list |

### Production profile specific

- In your IntelliJ run configuration for the Spring app, include `production` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable                       | Description                                                                                                                 |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| OSD_DATABASE_URL                           | The URL to the database the service connect to                                                                              |
| OSD_DATABASE_PASSWORD                      | Database schema password for the `osd` user                                                                                 |
| OSD_SAML_ENTITY_ID                         | Fox instance URL (dev: https://itportal.dev.fivium.co.uk/engedudev1/fox)                                                    |
| OSD_SAML_CERTIFICATE                       | The x509 certificate string                                                                                                 |
| OSD_SAML_LOGIN_URL                         | The URL to hit the `login` entry theme of the SAML login module                                                             |
| OSD_SERVICE_BASE_URL                       | The URL prior to the `/${serverContext}` part of the url  (e.g: https://itportal.dev.fivium.co.uk)                          |
| OSD_ENABLE_FLYWAY_OUT_OF_ORDER             | Set to `true` to allow flyway to run out of order, defaults to `false`                                                      | 
| OSD_ENERGY_PORTAL_API_URL                  | The URL to the Energy Portal API (ending in `/graphql`)                                                                     |
| OSD_ENERGY_PORTAL_API_TOKEN                | The pre-shared key to authenticate with the Energy Portal API                                                               |
| OSD_ENERGY_PORTAL_TEAM_ACCESS_API_BASE_URL | The Energy Portal team access API base url. (ending in the fox 5 context for the environment)                               |
| OSD_ENERGY_PORTAL_TEAM_ACCESS_API_TOKEN    | The Energy Portal team access API token                                                                                     |
| OSD_ENERGY_PORTAL_REGISTRATION_URL         | The Energy Portal registration url for the environment                                                                      |
| OSD_ENERGY_PORTAL_LOGOUT_URL               | The URL to the log out entry theme of the Energy Portal                                                                     |
| OSD_ENERGY_PORTAL_LOGOUT_KEY               | The pre-shared logout key for the energy portal (https://tpm.fivium.co.uk/index.php/pwd/view/2098)                          |
| OSD_S3_ACCESS_KEY                          | S3 username for document uploads / downloads                                                                                |
| OSD_S3_SECRET_KEY                          | S3 secret for document uploads / downloads                                                                                  |
| OSD_S3_BUCKET_NAME                         | S3 bucket for document uploads / downloads                                                                                  |
| OSD_S3_ENDPOINT                            | S3 endpoint for document uploads / downloads                                                                                |
| OSD_S3_REGION_NAME                         | S3 region for document uploads / downloads                                                                                  |
| OSD_S3_DISABLE_SSL                         | Disable S3 SSL (default `false`)                                                                                            |
| OSD_S3_PROXY_HOST                          | The hostname to access the S3 proxy (Optional)                                                                              |
| OSD_S3_PROXY_PORT                          | The port number to access the S3 proxy (Optional)                                                                           |
| OSD_CLAMAV_HOST                            | The host URL for ClamAV                                                                                                     |
| OSD_CLAMAV_PORT                            | The port ClamAV is hosted on                                                                                                |
| OSD_CLAMAV_TIMEOUT                         | ClamAV request timeout                                                                                                      |
| OSD_SESSION_TIMEOUT                        | The time before a session timeouts. This should be the same as the Energy Portal, e.g 180m. Note, needs the unit afterwards |
| OSD_NOTIFICATION_MODE                      | Can be `test` or `production`. `test` mode will redirect all outbound emails to the test recipient(s)                       |
| OSD_EMAIL_TEST_RECIPIENTS                  | If email is test mode, who to send emails to. Value can be a CSV list                                                       |
| OSD_ACTUATOR_ADMIN_USER_PASSWORD           | Password for the actuator admin user                                                                                        |
| OSD_CAN_SHOW_STACK_TRACE                   | Boolean flag to control if the stack trace shows up when a 500 response is received (Optional)                              |
| OSD_DATAWAREHOUSE_USER_PASSWORD            | The database password for the `datawarehouse` database user                                                                 |
| OSD_STATEMENT_PREPARED_DATE                | The date the accessibility statement was prepared. E.g. 05 May 2023                                                         |
| OSD_STATEMENT_LAST_REVIEW_DATE             | The date the accessibility statement was last reviewed. E.g. 05 May 2023                                                    |
| OSD_SERVICE_LAST_TEST_DATE                 | The date the service was last tested. E.g. 05 May 2023                                                                      |
| OSD_SERVICE_LAST_TESTED_BY                 | The company who carried out the last service E.g. Fivium Ltd                                                                |
| OSD_DESIGN_SYSTEM_LAST_TEST_DATE           | The date the design system was last tested. E.g. 05 May 2023                                                                |
| OSD_OFFLINE_DOCUMENT_LOCATION_URL          | The link to the location of offline appointments documents                                                                  |   
| OSD_EPMQ_MESSAGE_POLL_FREQUENCY_SECONDS    | The delay for epmq messages annotated with @Scheduled (Optional) defaults to 30                                             |
| OSD_ENABLE_STATSD                          | To enable or disable the statsd export. Should be true in prod                                                              |
| OSD_STATSD_HOST                            | The url that the metrics run on                                                                                             |
| OSD_STATSD_PORT                            | The port that the metrics run on                                                                                            |
| OSD_METRICS_INSTANCE_TAG                   | The hostname which will be applied to all metrics                                                                           |
| OSD_METRICS_ENVIRONMENT_NAME               | The app name and environment e.g. osd-prod                                                                                  |
| OSD_FMS_URL_BASE                           | The URL for the FMS instance on your environment                                                                            |
| OSD_FMS_SUBMIT_ENDPOINT                    | The FMS endpoint where feedback will be sent. Defaults to /api/v1/save-feedback                                             |
| OSD_FMS_PRESHARED_KEY                      | This is the api key used when making requests                                                                               |
| OSD_FMS_CONNECTION_TIMEOUT_SECONDS         | Connection timeout in seconds. Defaults to 20                                                                               |
| OSD_ANALYTICS_ENERGY_PORTAL_IDENTIFIER     | The google analytics tag used for all apps                                                                                  |
| OSD_ANALYTICS_SERVICE_IDENTIFIER           | The service specific google analytics tag                                                                                   |

### 3. Initialise the Fivium Design System

- `git submodule update --init --recursive`
- `cd fivium-design-system-core && npm install && npx gulp build && cd ..`

### 4. Build frontend components

- `npm install`
- `npx gulp buildAll`

### 5. Generate Jooq classes

Execute the gradle task `generateJooq`. You will need to re-generate when you change the database.
(This requires docker to be running on your machine)

### 6. Run the app

Create a run configuration for the Spring app and start the application.

The application will be running on `localhost:8080/wios/<endpoint>` e.g. `localhost:8080/wios/work-area`

If you receive a `publishEpmqMessagesIntegrationTestPublicationToSnapshotsRepository` error when hot reloading then
run the `publishing > publishEpmqMessagesIntegrationTestPublicationToMavenLocal` gradle task.

## Development setup

### Setup local development users

A SQL script exists in `/devtools/setup_test_users/sql` which will create users in the relevant teams
to use for local development. The script contains the email addresses and
[this page](https://confluence.fivium.co.uk/display/BESPOKE/WIOS+Testing+users) contains a
summary of the roles for the users.

### Checkstyle

1. In Intellij install the Checkstyle-IDEA plugin (from third-party repositories)
2. Go to File > Settings > Checkstyle
3. Click the plus icon under "Configuration File"
4. Select "Use a local Checkstyle file"
5. Select `devtools/checkstyle.xml`
6. Check the "Active" box next to the new profile

Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.
