# Offshore Safety Directive (OSD)

## Background

In April 2010 an explosion on the Deepwater Horizon drilling rig in the Gulf of Mexico resulted in the 
largest marine oil spill in the history of the petroleum industry. Millions of barrels of oil was released 
with widespread marine, coastal and economic impact.

In response to the Deepwater Horizon accident the EU issued a Directive on the on safety of offshore oil 
and gas operations. The EU Directive was implemented in UK law by the Offshore Petroleum Licensing 
(Offshore Safety Directive) Regulations 2015.

## Pre-requisites
- Java 17
- Node LTS + NPM

## Backend setup

### 1. Initialise the Fivium Design System
- `git submodule update --init --recursive`
- `cd fivium-design-system-core && npm install && npx gulp build && cd ..`

### 2. Build frontend components
- `npm install`
- `npx gulp buildAll`

### 3. Run the app
Create a run configuration for the Spring app and start the application.

The application will be running on `localhost:8080/osd/<endpoint>`

## Development setup

### Checkstyle
1. In Intellij install the Checkstyle-IDEA plugin (from third-party repositories)
2. Go to File > Settings > Checkstyle 
3. Click the plus icon under "Configuration File"
4. Select "Use a local Checkstyle file"
5. Select `devtools/checkstyle.xml`
6. Check the "Active" box next to the new profile

Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.

