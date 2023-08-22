#!/bin/sh

cp softwareag-ums.jar /var/db/newrelic-infra/custom-integrations

cp softwareag-ums-definition.yml /var/db/newrelic-infra/custom-integrations/

cp softwareag-ums-config.yml /etc/newrelic-infra/integrations.d/

cp softwareag-ums-server-config.json /etc/newrelic-infra/integrations.d/
