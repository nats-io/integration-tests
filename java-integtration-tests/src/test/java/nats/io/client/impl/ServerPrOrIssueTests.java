/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package nats.io.client.impl;

import nats.io.client.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import static nats.io.client.utils.JetStreamTestBase.createTestFileStream;
import static nats.io.client.utils.JetStreamTestBase.runInJsServer;

class ServerPrOrIssueTests {

    // https://github.com/nats-io/nats-server/pull/2152
    // On windows this failed to delete the stream b/c temporary files were left open.
    @Test
    void pr_2152_file_stream_delete() throws Exception {
        runInJsServer(nc -> {
            JetStreamTestHelper h = createTestFileStream(nc);
            nc.jetStream().publish(h.subject, new byte[1]);
            h.jsm.deleteStream(h.streamName);
        });
    }
}