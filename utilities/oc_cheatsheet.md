curl -X POST http://localhost:15000/logging?level=trace
oc exec $1 c istio-proxy -- curl -kv localhost:15000/config_dump> _Envoy_config.json
