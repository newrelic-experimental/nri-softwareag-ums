@echo off

copy softwareag-ums.jar "C:\Program Files\New Relic\newrelic-infra\custom-integrations\"

copy softwareag-ums-definition.yml "C:\Program Files\New Relic\newrelic-infra\custom-integrations\"

copy softwareag-ums-config.yml "C:\Program Files\New Relic\newrelic-infra\integrations.d"

copy softwareag-ums-server-config.json "C:\Program Files\New Relic\newrelic-infra\integrations.d"
