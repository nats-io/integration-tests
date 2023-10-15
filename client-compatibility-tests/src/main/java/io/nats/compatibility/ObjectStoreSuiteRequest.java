package io.nats.compatibility;

import io.nats.client.JetStreamApiException;
import io.nats.client.ObjectStore;
import io.nats.client.ObjectStoreManagement;
import io.nats.client.api.ObjectInfo;
import io.nats.client.api.ObjectMeta;
import io.nats.client.api.ObjectStoreConfiguration;
import io.nats.client.api.StorageType;
import io.nats.client.support.ApiConstants;
import io.nats.client.support.JsonValueUtils;
import io.nats.utils.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;

public class ObjectStoreSuiteRequest extends Request {

    final String bucket;
    final String object;

    public ObjectStoreSuiteRequest(Request r) {
        super(r);
        bucket = JsonValueUtils.readString(dataValue, "bucket");
        object = JsonValueUtils.readString(dataValue, "object");
    }

    public void execute() {
        switch (test) {
            case DEFAULT_BUCKET:  doCreateBucket(); break;
            case CUSTOM_BUCKET:   doCreateBucket(); break;
            case PUT_OBJECT:      doPutObject(); break;
            case GET_OBJECT:      doGetObject(); break;
            case UPDATE_METADATA: doUpdateMetadata(); break;
            default:
                respond();
                break;
        }
    }

    private void doCreateBucket() {
        try {
            _createBucket();
            respond();
        }
        catch (Exception e) {
            handleException(e);
        }
    }

    private void doPutObject() {
        try {
            _createBucket();
            String objectName = JsonValueUtils.readString(config, "name");
            String description = JsonValueUtils.readString(config, "description");
            _putObject(objectName, description);
            respond();
        }
        catch (Exception e) {
            handleException(e);
        }
    }

    private void doGetObject() {
        try {
            _createBucket();
            _putObject(object, null);
            ObjectStore os = nc.objectStore(bucket);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectInfo oi = os.get(object, baos);
            respond(oi.getDigest());
        }
        catch (Exception e) {
            handleException(e);
        }
    }

    private void doUpdateMetadata() {
        try {
            _createBucket();
            _putObject(object, null);
            ObjectStore os = nc.objectStore(bucket);
            String objectName = JsonValueUtils.readString(config, "name");
            String description = JsonValueUtils.readString(config, "description");
            ObjectMeta meta = ObjectMeta.builder(objectName).description(description).build();
            os.updateMeta(object, meta);
            respond();
        }
        catch (Exception e) {
            handleException(e);
        }
    }

    private void _putObject(String objectName, String description) {
        try {
            ObjectStore os = nc.objectStore(bucket);
            ObjectMeta meta = ObjectMeta.builder(objectName).description(description).build();
            InputStream inputStream = ResourceUtils.resourceAsInputStream("nats-server.zip");
            os.put(meta, inputStream);
        }
        catch (Exception e) {
            handleException(e);
        }
    }

    private void _createBucket() {
        try {
            ObjectStoreManagement osm = nc.objectStoreManagement();
            osm.create(extractObjectStoreConfiguration());
        }
        catch (JetStreamApiException jsae) {
            if (!jsae.getMessage().contains("10058")) {
                handleException(jsae);
            }
        }
        catch (Exception e) {
            handleException(e);
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
