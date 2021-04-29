package io.nats.integration.utils;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.impl.Headers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JetStreamTestBase extends TestBase {

    public static final int STORAGE_OVERHEAD_MEMORY = 16;
    public static final int STORAGE_OVERHEAD_FILE = 30;
    public static final int STORAGE_OVERHEAD_HEADERS_MEMORY = 12;
    public static final int STORAGE_OVERHEAD_HEADERS_FILE = 16;
    public static final int STORAGE_OVERHEAD_PER_HEADERS_TUPLE = 3;

    public static JetStreamTestHelper api(Connection nc) throws IOException {
        return new JetStreamTestHelper().jsm(nc.jetStreamManagement()).js(nc.jetStream());
    }

    public static JetStreamTestHelper namer(Connection nc) throws IOException {
        return api(nc).streamName(uniqueEnough("stream")).subject(uniqueEnough("subject"));
    }

    public static JetStreamTestHelper helper(Connection nc, String streamName, String subject) throws IOException {
        return api(nc).streamName(streamName).subject(subject);
    }

    // ----------------------------------------------------------------------------------------------------
    // Stream
    // ----------------------------------------------------------------------------------------------------
    public static JetStreamTestHelper createStream(Connection nc, StorageType storageType, String streamName, String subject)
            throws IOException, JetStreamApiException {

        JetStreamTestHelper h = helper(nc, streamName, subject);

        StreamConfiguration sc = StreamConfiguration.builder()
                .name(h.streamName)
                .storageType(storageType)
                .subjects(h.subject)
                .build();

        try {
            h.jsm.deleteStream(h.streamName);
        }
        catch (JetStreamApiException j) {
            // this is just a 404, so the stream does not exist
        }

        return h.si(h.jsm.addStream(sc));
    }

    public static JetStreamTestHelper createTestMemoryStream(Connection nc) throws IOException, JetStreamApiException {
        return createStream(nc, StorageType.Memory, uniqueEnough("stream"), uniqueEnough("subject"));
    }

    public static JetStreamTestHelper createTestFileStream(Connection nc) throws IOException, JetStreamApiException {
        return createStream(nc, StorageType.File, uniqueEnough("stream"), uniqueEnough("subject"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Storage
    // ----------------------------------------------------------------------------------------------------
    public static int storageLength(StorageType storageType, String subject, int dataLen, Headers headers) {
        int len = subject.length() + dataLen + (storageType == StorageType.Memory ? STORAGE_OVERHEAD_MEMORY : STORAGE_OVERHEAD_FILE);
        if (headers != null && headers.size() > 0) {
            len += (storageType == StorageType.Memory ? STORAGE_OVERHEAD_HEADERS_MEMORY : STORAGE_OVERHEAD_HEADERS_FILE);
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String s : entry.getValue()) {
                    len += (entry.getKey().length() + s.length() + STORAGE_OVERHEAD_PER_HEADERS_TUPLE);
                }
            }
        }

        return len;
    }

    public static int storageLength(StorageType storageType, String subject, int dataLen) {
        return storageLength(storageType, subject, dataLen, null);
    }

    public static int storageLength(StorageType storageType, String subject, String data, Headers headers) {
        return storageLength(storageType, subject, (data == null ? 0 : data.length()), headers);
    }

    public static int storageLength(StorageType storageType, String subject, byte[] data, Headers headers) {
        return storageLength(storageType, subject, (data == null ? 0 : data.length), headers);
    }

    public static int storageLength(StorageType storageType, Message m) {
        return storageLength(storageType, m.getSubject(), m.getData(), m.getHeaders());
    }
}
