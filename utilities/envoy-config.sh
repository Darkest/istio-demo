oc exec $1 c istio-proxy -- curl -kv localhost:15000/config_dump> $1_Envoy_config.json