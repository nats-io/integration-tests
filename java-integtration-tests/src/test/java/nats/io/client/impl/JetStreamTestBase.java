package nats.io.client.impl;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import nats.io.client.utils.TestBase;

import java.io.IOException;

public class JetStreamTestBase extends TestBase {

    // ----------------------------------------------------------------------------------------------------
    // Management
    // ----------------------------------------------------------------------------------------------------
    public static StreamInfo createMemoryStream(JetStreamManagement jsm, String streamName, String... subjects)
            throws IOException, JetStreamApiException {

        StreamConfiguration sc = StreamConfiguration.builder().name(streamName).storageType(StorageType.Memory)
                .subjects(subjects).build();

        return jsm.addStream(sc);
    }

    public static StreamInfo createMemoryStream(Connection nc, String streamName, String... subjects)
            throws IOException, JetStreamApiException {
        return createMemoryStream(nc.jetStreamManagement(), streamName, subjects);
    }
}
