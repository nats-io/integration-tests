// Copyright 2022 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.integration.server.behavior;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.nats.integration.utils.JetStreamTestBase;
import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DefaultConsumerConfiguration extends JetStreamTestBase {
    // default json from server for reference
    //    "durable_name": "durable",
    //    "deliver_policy": "all",
    //    "ack_policy": "explicit",
    //    "ack_wait": 30000000000,
    //    "max_deliver": -1,
    //    "replay_policy": "instant",
    //    "max_waiting": 512,
    //    "max_ack_pending": 20000

    static class ConsumerConfigurationChecker extends ConsumerConfiguration {
        public ConsumerConfigurationChecker(ConsumerConfiguration cc) {
            super(cc);
        }

        public DeliverPolicy deliverPolicy() { return deliverPolicy; };
        public AckPolicy ackPolicy() { return ackPolicy; };
        public ReplayPolicy replayPolicy() { return replayPolicy; };

        public String description() { return description; };
        public String durable() { return durable; };
        public String deliverSubject() { return deliverSubject; };
        public String deliverGroup() { return deliverGroup; };
        public String filterSubject() { return filterSubject; };
        public String sampleFrequency() { return sampleFrequency; };

        public ZonedDateTime startTime() { return startTime; };

        public Duration ackWait() { return ackWait; }
        public Duration idleHeartbeat() { return idleHeartbeat; };
//        public Duration maxExpires() { return maxExpires; };
//        public Duration inactiveThreshold() { return inactiveThreshold; };

        public Long startSeq() { return startSeq; };
        public Long maxDeliver() { return maxDeliver; };
        public Long rateLimit() { return rateLimit; };
        public Long maxAckPending() { return maxAckPending; };
        public Long maxPullWaiting() { return maxPullWaiting; };
//        public Long maxBatch() { return maxBatch; };

        public Boolean flowControl() { return flowControl; };
        public Boolean headersOnly() { return headersOnly; };
    }

    @Test
    public void testDefaultConsumerConfiguration() throws Exception {
        runInJsServer(nc -> {
            JetStreamTestHelper h = new JetStreamTestHelper(nc);
            createTestMemoryStream(h);

            ConsumerConfiguration cc =
                ConsumerConfiguration.builder()
                    .durable("dccdur").build();
            cc = h.jsm.addOrUpdateConsumer(h.streamName, cc).getConsumerConfiguration();

            ConsumerConfigurationChecker ccc = new ConsumerConfigurationChecker(cc);

            assertEquals(DeliverPolicy.All, ccc.deliverPolicy());
            assertEquals(AckPolicy.Explicit, ccc.ackPolicy());
            assertEquals(ReplayPolicy.Instant, ccc.replayPolicy());

            assertNull(ccc.description());
            assertEquals("dccdur", ccc.durable());
            assertNull(ccc.deliverSubject());
            assertNull(ccc.deliverGroup());
            assertNull(ccc.filterSubject());
            assertNull(ccc.sampleFrequency());

            assertNull(ccc.startTime());

            assertEquals(Duration.ofSeconds(30), ccc.ackWait());
            assertNull(ccc.idleHeartbeat());
//            assertNull(ccc.maxExpires());
//            assertNull(ccc.inactiveThreshold());

            assertNull(ccc.startSeq());
            assertEquals(-1, ccc.maxDeliver());
            assertNull(ccc.rateLimit());
            if (nc.getServerInfo().isNewerVersionThan("2.7.4")) {
                assertEquals(1000, ccc.maxAckPending());
            }
            else {
                assertEquals(20000, ccc.maxAckPending());
            }
            assertEquals(512, ccc.maxPullWaiting());
//            assertNull(ccc.maxBatch());

            assertFalse(ccc.flowControl());
            assertFalse(ccc.headersOnly());
        });
    }
}