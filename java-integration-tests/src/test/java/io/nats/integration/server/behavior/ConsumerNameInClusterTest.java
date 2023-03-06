/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.nats.integration.server.behavior;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.integration.utils.JetStreamTestBase;
import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumerNameInClusterTest extends JetStreamTestBase {

    @Test
    public void testSingleServer() throws Exception {
        runInJsServer(nc -> _testCluster(Collections.singletonList(nc)));
    }

//    @Test
//    public void testCluster() throws Exception {
//        runInJsSimpleCluster(ConsumerNameInClusterTest::_testCluster);
//    }

    public static void _testCluster(List<Connection> ncs) throws IOException, JetStreamApiException {
        JetStreamTestHelper h = new JetStreamTestHelper(ncs.get(0));
        createTestMemoryStream(h);

        String[] conNames = new String[ncs.size()];
        JetStreamSubscription[] subs = new JetStreamSubscription[ncs.size()];
        for (int x = 0; x < ncs.size(); x++) {
            conNames[x] = uniqueEnough();
            PullSubscribeOptions subOpts = ConsumerConfiguration.builder()
                .name(conNames[x])
                .inactiveThreshold(100000)
                .buildPullSubscribeOptions();
            subs[x] = ncs.get(x).jetStream().subscribe(h.subject, subOpts);
        }

        List<String> consumerNames = h.jsm.getConsumerNames(h.streamName);
        assertEquals(ncs.size(), consumerNames.size());
        for (int x = 0; x < ncs.size(); x++) {
            assertTrue(consumerNames.contains(conNames[x]));
            subs[x].unsubscribe();
        }
    }
}
