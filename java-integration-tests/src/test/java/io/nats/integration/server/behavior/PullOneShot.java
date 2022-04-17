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

import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PullSubscribeOptions;
import io.nats.integration.utils.JetStreamTestBase;
import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PullOneShot extends JetStreamTestBase {

    @Test
    public void testFlowControl() throws Exception {
        runInJsServer(nc -> {
            JetStreamTestHelper h = new JetStreamTestHelper(nc);
            createTestMemoryStream(h);

            // Build 3 subscriptions against the same subject so they can
            // each have their own pull against the same exact data
            PullSubscribeOptions optNoWait = PullSubscribeOptions.builder().durable("nowait").build();
            JetStreamSubscription subNoWait = h.js.subscribe(h.subject, optNoWait);

            PullSubscribeOptions optExpires = PullSubscribeOptions.builder().durable("expires").build();
            JetStreamSubscription subExpires = h.js.subscribe(h.subject, optExpires);

            PullSubscribeOptions optOneShot = PullSubscribeOptions.builder().durable("oneshot").build();
            JetStreamSubscription subOneShot = h.js.subscribe(h.subject, optOneShot);

            // no wait, batch size 10, there are 10 messages, we will read them all and not trip nowait
            subNoWait.pullNoWait(10);
            subExpires.pullExpiresIn(10, Duration.ofSeconds(5));
            subOneShot.pullNoWait(10, Duration.ofSeconds(5));

            sleep(1000); // give plenty of time for nowait to be handled

            jsPublish(h.js, h.subject, 5);
            sleep(5000); // all the expirations will happen

            // read and ack the messages and check the results
            List<Message> listNoWait = readMessagesAck(subNoWait);
            assertEquals(0, listNoWait.size());

            List<Message> listExpires = readMessagesAck(subExpires);
            assertEquals(5, listExpires.size());

            List<Message> listOneShot = readMessagesAck(subOneShot);
            if (nc.getServerInfo().isOlderThanVersion("2.7.1")) {
                assertEquals(0, listOneShot.size());
            }
            else {
                assertTrue(listOneShot.size() > 0);
            }

        });
    }
}
