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

package io.nats.integration.server.issues;

import io.nats.client.JetStreamApiException;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.integration.utils.JetStreamTestBase;
import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue2423MaxWaitingPlusDeliverSubjectTest extends JetStreamTestBase {

    // https://github.com/nats-io/nats-server/issues/2423
    @Test
    void server_issue_2423_MaxWaitingPlusDeliverSubjectTest() throws Exception {
        runInJsServer(true, nc -> {
            // create consumer with both
            // after fixing create subscription and default in ConsumerConfiguration
            JetStreamTestHelper h = new JetStreamTestHelper(nc, "strm2423", "sub2423.*");
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
