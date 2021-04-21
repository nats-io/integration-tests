/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package nats.io;

import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.support.ByteArrayBuilder;
import org.junit.jupiter.api.Test;

import static nats.io.client.impl.JetStreamTestBase.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreamConfigurationTests {

    @Test
    void testMaxMessageSize() throws Exception {
        runInJsServer(nc -> {

            JetStreamManagement jsm = nc.jetStreamManagement();
            JetStream js = nc.jetStream();
            ByteArrayBuilder bab = new ByteArrayBuilder(1000);

            StreamConfiguration.Builder builder = StreamConfiguration.builder()
                    .name(STREAM)
                    .storageType(StorageType.Memory)
                    .subjects(SUBJECT);

            jsm.addStream(builder.build());

            bab.append((byte)0);
            for (int x = 1; x < 10000; x++) {

                jsm.updateStream(builder.maxMsgSize(x).build());

                byte[] bytes1 = bab.toByteArray();
                js.publish(SUBJECT, bytes1);

                bab.append((byte)0);
                byte[] bytes2 = bab.toByteArray();
                assertThrows(JetStreamApiException.class, () -> js.publish(SUBJECT, bytes2));
            }
        });
    }
}
