protoc -I=/mnt/c/job/data-plane-api/envoy/service/ratelimit/v3 \
        -I=/mnt/c/job/data-plane-api \
  --descriptor_set_out=bundle.protoset *.proto
