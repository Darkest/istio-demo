BASEDIR=$(dirname "$0")
oc new-project bookinfo
oc apply -n bookinfo -f https://raw.githubusercontent.com/Maistra/istio/maistra-2.0/samples/bookinfo/platform/kube/bookinfo.yaml
oc apply -n bookinfo -f $BASEDIR/ingress.yml
oc apply -n bookinfo -f $BASEDIR/egress.yml
oc apply -n bookinfo -f $BASEDIR/book-ingress-service.yml
oc apply -n bookinfo -f $BASEDIR/book-ingress-gateway.yml
oc apply -n bookinfo -f $BASEDIR/book-route.yml
oc apply -n bookinfo -f $BASEDIR/vs.yml