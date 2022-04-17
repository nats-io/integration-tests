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

import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import static io.nats.integration.utils.JetStreamTestBase.createTestFileStream;
import static io.nats.integration.utils.JetStreamTestBase.runInJsServer;

class Pr2152FileStreamDeleteTest {

    // https://github.com/nats-io/nats-server/pull/2152
    // On windows this failed to delete the stream b/c temporary files were left open.
    @Test
    void server_pr_2152_FileStreamDeleteTest() throws Exception {
        runInJsServer(nc -> {
            JetStreamTestHelper h = new JetStreamTestHelper(nc);
            createTestFileStream(h);
            nc.jetStream().publish(h.subject, new byte[1]);
            h.jsm.deleteStream(h.streamName);
        });
    }
}
