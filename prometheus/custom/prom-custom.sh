#!/bin/bash
namespace=custom-prom
sa=custom-prom-sa
oc project $namespace
oc create serviceaccount $sa
oc adm policy add-scc-to-user anyuid -z $sa --as system:admin
oc apply -f cluster-role.yml
oc apply -f cluster-role-binding.yml
oc delete cm custom-prometheus
oc create cm custom-prometheus --from-file=prom-config.yml=prom-conf.yml
oc apply -f prometheus-deploy.yml
