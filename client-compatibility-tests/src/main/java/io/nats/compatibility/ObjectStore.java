package io.nats.compatibility;

import io.nats.client.ObjectStoreManagement;
import io.nats.client.api.ObjectStoreConfiguration;
import io.nats.client.api.StorageType;
import io.nats.client.support.ApiConstants;
import io.nats.client.support.JsonValueUtils;

import java.time.Duration;

public class ObjectStore extends Request {

    // tests.object-store.put-object.create.command
    // {"url":
    // "https://github.com/nats-io/nats-server/releases/download/v2.9.19/nats-server-v2.9.19-linux-arm64.zip",
    // "bucket":"test","suite":"object_store","test":"object","command":"put",
    // "config":{"name":"nats-server.zip","description":"a nats server"}}
    final String bucket;

    public static void execute(Request r) {
        new ObjectStore(r).execute();
    }

    private ObjectStore(Request r) {
        super(r);
        bucket = JsonValueUtils.readString(dataValue, "bucket");
    }

    private void execute() {
        switch (command) {
            case CREATE:
                makeBucketThenRespond();
                break;
        }
    }

    private void makeBucketThenRespond() {
        try {
            ObjectStoreManagement osm = nc.objectStoreManagement();
            osm.create(extractObjectStoreConfiguration());
            respond();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectStoreConfiguration extractObjectStoreConfiguration() {
        ObjectStoreConfiguration.Builder builder = ObjectStoreConfiguration.builder();
        if (bucket != null) {
            builder.name(bucket);
        }
        else {
            String s = JsonValueUtils.readString(config, ApiConstants.BUCKET);
            if (s != null) {
                builder.name(s);
            }
            s = JsonValueUtils.readString(config, ApiConstants.DESCRIPTION);
            if (s != null) {
                builder.description(s);
            }
            Long l = JsonValueUtils.readLong(config, ApiConstants.MAX_BYTES);
            if (l != null) {
                builder.maxBucketSize(l);
            }
            Duration d = JsonValueUtils.readNanos(config, ApiConstants.MAX_AGE);
            if (d != null) {
                builder.ttl(d);
            }
            s = JsonValueUtils.readString(config, ApiConstants.STORAGE);
            if (s != null) {
                builder.storageType(StorageType.get(s));
            }
            Integer i = JsonValueUtils.readInteger(config, ApiConstants.NUM_REPLICAS);
            if (i != null) {
                builder.replicas(i);
            }
        }
        return builder.build();
    }
}
