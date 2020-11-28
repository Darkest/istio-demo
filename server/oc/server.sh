#!/bin/sh
oc login -u kubeadmin -p Ddbvk-odz4g-NghN8-TiFC8 https://api.crc.testing:6443
BASEDIR=$(dirname "$0")
oc apply -f $BASEDIR/deployment.yml
oc apply -f $BASEDIR/ingress.yml
oc apply -f $BASEDIR/egress.yml
oc apply -f $BASEDIR/server-ingress-gateway.yml
oc apply -f $BASEDIR/server-ingress-service.yml
oc apply -f $BASEDIR/server-route.yml
oc apply -f $BASEDIR/sm.yml