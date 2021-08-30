/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.nats.integration;

import io.nats.client.JetStreamApiException;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.integration.utils.JetStreamTestBase;
import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class Issue2423_MaxWaitingPlusDeilverSubject_Test extends JetStreamTestBase {

    @Test
    void issue2423() throws Exception {
        runInJsServer(true, nc -> {
            // create consumer with both
            // after fixing create subscription and default in ConsumerConfiguration
            JetStreamTestHelper h = manager(nc).streamName("strm2423").subject("sub2423.*");
            createTestMemoryStream(h);


            // can't have max pull waiting and deliver subject
            assertThrows(JetStreamApiException.class, () ->
                    h.jsm.addOrUpdateConsumer(h.streamName, new ConsumerConfiguration.Builder()
                            .durable("dur242301")
                            .maxPullWaiting(2423)
                            .deliverSubject("delisub242301")
                            .build()));

            // only deliver subject
            h.jsm.addOrUpdateConsumer(h.streamName, new ConsumerConfiguration.Builder()
                    .durable("dur242302")
                    .maxPullWaiting(0)
                    .deliverSubject("delisub242302")
                    .build());

            // only max pull waiting
            h.jsm.addOrUpdateConsumer(h.streamName, new ConsumerConfiguration.Builder()
                    .durable("dur242303")
                    .maxPullWaiting(2423)
                    .build());
        });
    }
}
