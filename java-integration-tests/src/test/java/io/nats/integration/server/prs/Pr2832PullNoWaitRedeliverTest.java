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

package io.nats.integration.server.prs;

import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.impl.NatsMessage;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.nats.integration.utils.JetStreamTestBase.runInJsServer;
import static io.nats.integration.utils.TestBase.sleep;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Pr2832PullNoWaitRedeliverTest {

    // https://github.com/nats-io/nats-server/pull/2832
    // pull with no wait was not handling redelivers
    @Test
    public void testNoWaitRedeliver() throws Exception {
        runInJsServer(nc -> {

            StreamConfiguration sc = StreamConfiguration.builder()
                .name("nwr-strm")
                .storageType(StorageType.Memory)
                .subjects("nwr-sub").build();

            nc.jetStreamManagement().addStream(sc);

            // Create our JetStream context.
            JetStream js = nc.jetStream();
            js.publish(new NatsMessage("nwr-sub", null, null));

            ConsumerConfiguration cc = ConsumerConfiguration.builder()
                .ackWait(Duration.ofMillis(1000))
                .build();

            PullSubscribeOptions options = PullSubscribeOptions.builder()
                .durable("nwr-dur") // required
                .configuration(cc)
                .build();

            JetStreamSubscription sub = js.subscribe("nwr-sub", options);

            sub.pullNoWait(1);
            Message msg = sub.nextMessage(500);
            msg.nak();
            sleep(2000); // nak

            sub.pullNoWait(1);
            msg = sub.nextMessage(1000);
            assertNotNull(msg);
            sleep(2000); // don't ack

            sub.pullNoWait(1);
            msg = sub.nextMessage(1000);
            assertNotNull(msg);
            msg.ack();
        });
    }
}
