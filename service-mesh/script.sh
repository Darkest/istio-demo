BASEDIR=$(dirname "$0")
oc new-project istio-system
oc apply -f $BASEDIR/istio-cp.yml
