# Offshore Safety Directive (OSD)

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
  (
  See [Docker setup](https://confluence.fivium.co.uk/display/JAVA/Java+development+environment+setup#Javadevelopmentenvironmentsetup-Docker)
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

### Development

- In your IntelliJ run configuration for the Spring app, include `development` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable           | Description                                                                                             |
|--------------------------------|---------------------------------------------------------------------------------------------------------|
| OSD_SNS_SQS_ACCESS_KEY_ID      | SNS/SQS access key id                                                                                   |
| OSD_SNS_SQS_SECRET_ACCESS_KEY  | SNS/SQS secret access key                                                                               |
| OSD_SNS_SQS_ENVIRONMENT_SUFFIX | SNS/SQS environment suffix. This should be set to something unique per developer, such as your initials |

### Production

- In your IntelliJ run configuration for the Spring app, include `production` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable                       | Description                                                                                          |
|--------------------------------------------|------------------------------------------------------------------------------------------------------|
| OSD_DATABASE_URL                           | The URL to the database the service connect to                                                       |
| OSD_DATABASE_PASSWORD                      | Database schema password for the `osd` user                                                          |
| OSD_SAML_ENTITY_ID                         | Fox instance URL (dev: https://itportal.dev.fivium.local/engedudev1/fox)                             |
| OSD_SAML_CERTIFICATE                       | The x509 certificate string                                                                          |
| OSD_SAML_LOGIN_URL                         | The URL to hit the `login` entry theme of the SAML login module                                      |
| OSD_SAML_BASE_URL                          | The URL prior to the `/${serverContext}` part of the url  (e.g: https://itportal.dev.fivium.local)   |
| OSD_ENABLE_FLYWAY_OUT_OF_ORDER             | Set to `true` to allow flyway to run out of order, defaults to `false`                               | 
| OSD_ENERGY_PORTAL_API_URL                  | The URL to the Energy Portal API (ending in `/graphql`)                                              |
| OSD_ENERGY_PORTAL_API_TOKEN                | The pre-shared key to authenticate with the Energy Portal API                                        |
| OSD_ENERGY_PORTAL_TEAM_ACCESS_API_BASE_URL | The Energy Portal team access API base url. (ending in the fox 5 context for the environment)        |
| OSD_ENERGY_PORTAL_TEAM_ACCESS_API_TOKEN    | The Energy Portal team access API token                                                              |
| OSD_ENERGY_PORTAL_REGISTRATION_URL         | The Energy Portal registration url for the environment                                               |
| OSD_ENERGY_PORTAL_LOGOUT_URL               | The URL to the log out entry theme of the Energy Portal                                              |
| OSD_S3_ACCESS_KEY                          | S3 username for document uploads / downloads                                                         |
| OSD_S3_SECRET_KEY                          | S3 secret for document uploads / downloads                                                           |
| OSD_S3_BUCKET_NAME                         | S3 bucket for document uploads / downloads                                                           |
| OSD_S3_ENDPOINT                            | S3 endpoint for document uploads / downloads                                                         |
| OSD_S3_REGION_NAME                         | S3 region for document uploads / downloads                                                           |
| OSD_S3_DISABLE_SSL                         | Disable S3 SSL (default `false`)                                                                     |
| OSD_S3_PROXY_HOST                          | The hostname to access the S3 proxy (Optional)                                                       |
| OSD_S3_PROXY_PORT                          | The port number to access the S3 proxy (Optional)                                                    |
| OSD_CLAMAV_HOST                            | The host URL for ClamAV                                                                              |
| OSD_CLAMAV_PORT                            | The port ClamAV is hosted on                                                                         |
| OSD_CLAMAV_TIMEOUT                         | ClamAV request timeout                                                                               |
| OSD_SESSION_TIMEOUT_MINUTES                | The time in minutes before a session timeouts. This should be the same as the Energy Portal, e.g 180 |
| OSD_SNS_SQS_ACCESS_KEY_ID                  | SNS/SQS access key id                                                                                |
| OSD_SNS_SQS_SECRET_ACCESS_KEY              | SNS/SQS secret access key                                                                            |
| OSD_SNS_SQS_ENVIRONMENT_SUFFIX             | SNS/SQS environment suffix. This should be set to the environment name, e.g. st or prod              |

### 3. Initialise the Fivium Design System

- `git submodule update --init --recursive`
- `cd fivium-design-system-core && npm install && npx gulp build && cd ..`

### 4. Build frontend components

- `npm install`
- `npx gulp buildAll`

### 5. Run the app

Create a run configuration for the Spring app and start the application.

The application will be running on `localhost:8080/wios/<endpoint>`

## Development setup

### Checkstyle

1. In Intellij install the Checkstyle-IDEA plugin (from third-party repositories)
2. Go to File > Settings > Checkstyle
3. Click the plus icon under "Configuration File"
4. Select "Use a local Checkstyle file"
5. Select `devtools/checkstyle.xml`
6. Check the "Active" box next to the new profile

Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.

