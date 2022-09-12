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

package io.nats.integration.client;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.ServerInfo;
import io.nats.client.api.*;
import io.nats.integration.utils.JetStreamTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ServerBehaviorByVersion extends JetStreamTestBase {

    // gradle test --tests io.nats.integration.client.ServerBehaviorByVersion

    @Test
    public void test272() throws Exception {
        runInJsServer(nc -> {
            ServerInfo serverInfo = nc.getServerInfo();

            KeyValueStatus status =
                nc.keyValueManagement().create(
                    KeyValueConfiguration.builder().name("test272").build());

            if (serverInfo.isOlderThanVersion("2.7.2")) {
                assertEquals(DiscardPolicy.Old,
                    status.getConfiguration().getBackingConfig().getDiscardPolicy());
            }
            else {
                assertEquals(DiscardPolicy.New,
                    status.getConfiguration().getBackingConfig().getDiscardPolicy());
            }
        });
    }

    @Test
    public void test284() throws Exception {
        runInJsServer(nc -> {
            ServerInfo serverInfo = nc.getServerInfo();

            JetStreamManagement jsm = nc.jetStreamManagement();
            StreamInfo si = jsm.addStream(
                StreamConfiguration.builder()
                .name("test284")
                .storageType(StorageType.Memory)
                .subjects("subject.>")
                .build()
            );

            ConsumerConfiguration cc = ConsumerConfiguration.builder()
                .durable("dur284")
                .deliverSubject("del284")
                .filterSubject("subject.284")
                .build();

            jsm.addOrUpdateConsumer("test284", cc);

            ConsumerConfiguration change = ConsumerConfiguration.builder(cc).filterSubject("subject.>").build();

            if (serverInfo.isNewerVersionThan("2.8.4")) {
                jsm.addOrUpdateConsumer("test284", change);
            }
            else {
                JetStreamApiException e = assertThrows(JetStreamApiException.class,
                    () -> jsm.addOrUpdateConsumer("test284", change));
                assertTrue(e.getMessage().contains("10012"));
            }
        });
    }
}
