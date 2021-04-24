/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package nats.io.client.impl;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamState;
import io.nats.client.support.ByteArrayBuilder;
import nats.io.client.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static nats.io.client.utils.JetStreamTestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreamConfigurationTests {
    private static final int TEST_ROUNDS = 100;

    @Test
    void max_message_size() throws Exception {
        runInJsServer(nc -> {

            JetStreamTestHelper h = createTestMemoryStream(nc);

            StreamConfiguration.Builder updateBuilder = StreamConfiguration.builder()
                    .name(h.streamName)
                    .storageType(StorageType.Memory)
                    .subjects(h.subject);

            ByteArrayBuilder bab = new ByteArrayBuilder(TEST_ROUNDS + 1).append(randomByte());
            for (int x = 1; x <= TEST_ROUNDS; x++) {
                h.jsm.updateStream(updateBuilder.maxMsgSize(x).build());

                h.js.publish(h.subject, bab.toByteArray());

                assertThrows(JetStreamApiException.class,
                        () -> h.js.publish(h.subject, bab.append(randomByte()).toByteArray()));
            }
        });
    }

    @Test
    void max_messages_discard_old() throws Exception {
        runInJsServer(nc -> {

            JetStreamTestHelper h = namer(nc);

            h.jsm.addStream(StreamConfiguration.builder()
                    .name(h.streamName)
                    .subjects(h.subject)
                    .storageType(StorageType.Memory)
                    .discardPolicy(DiscardPolicy.Old)
                    .maxMessages(2)
                    .build()
            );

            _discard_old(h);
        });
    }

    @Test
    void max_messages_discard_new() throws Exception {
        runInExternalServer(nc -> {
            JetStreamTestHelper h = namer(nc);

            h.jsm.addStream(StreamConfiguration.builder()
                    .name(h.streamName)
                    .subjects(h.subject)
                    .storageType(StorageType.Memory)
                    .discardPolicy(DiscardPolicy.New)
                    .maxMessages(2)
                    .build()
            );

            _discard_new(h);
        });
    }

    @Test
    void byte_count_file_storage() throws Exception {
        runInJsServer(nc -> _byte_count(createTestFileStream(nc), StorageType.File));
    }

    @Test
    void byte_count_memory_storage() throws Exception {
        runInJsServer(nc -> _byte_count(createTestMemoryStream(nc), StorageType.Memory));
    }

    @Test
    void max_bytes_discard_old_file_storage() throws Exception {
        runInJsServer(nc -> _max_bytes_old(nc, StorageType.File));
    }

    @Test
    void max_bytes_discard_old_memory_storage() throws Exception {
        runInJsServer(nc -> _max_bytes_old(nc, StorageType.Memory));
    }

    @Test
    void max_bytes_discard_new_file_storage() throws Exception {
        runInJsServer(nc -> _max_bytes_new(nc, StorageType.File));
    }

    @Test
    void max_bytes_discard_new_memory_storage() throws Exception {
        runInJsServer(nc -> _max_bytes_new(nc, StorageType.Memory));
    }

    private void _discard_old(JetStreamTestHelper h) throws IOException, JetStreamApiException {
        h.publish(2);

        StreamState ss = h.jsm.getStreamInfo(h.streamName).getStreamState();
        assertEquals(2, ss.getMsgCount());
        assertEquals(1, ss.getFirstSequence());
        assertEquals(2, ss.getLastSequence());

        for (int x = 2; x < 10; x++) {
            h.publish();
            ss = h.jsm.getStreamInfo(h.streamName).getStreamState();
            assertEquals(2, ss.getMsgCount());
            assertEquals(x, ss.getFirstSequence());
            assertEquals(x + 1, ss.getLastSequence());
        }
    }

    private void _discard_new(JetStreamTestHelper h) throws IOException, JetStreamApiException {
        h.publish(2);
        assertThrows(JetStreamApiException.class, h::publish);

        StreamState ss = h.jsm.getStreamInfo(h.streamName).getStreamState();
        assertEquals(2, ss.getMsgCount());
        assertEquals(1, ss.getFirstSequence());
        assertEquals(2, ss.getLastSequence());

        h.jsm.deleteMessage(h.streamName, 1);

        h.publish();
        assertThrows(JetStreamApiException.class, h::publish);
        ss = h.jsm.getStreamInfo(h.streamName).getStreamState();
        assertEquals(2, ss.getMsgCount());
        assertEquals(2, ss.getFirstSequence());
        assertEquals(3, ss.getLastSequence());

        h.jsm.deleteMessage(h.streamName, 3);
        h.publish();
        assertThrows(JetStreamApiException.class, h::publish);
        ss = h.jsm.getStreamInfo(h.streamName).getStreamState();
        assertEquals(2, ss.getMsgCount());
        assertEquals(2, ss.getFirstSequence());
        assertEquals(4, ss.getLastSequence());
    }

    private void _max_bytes_old(Connection nc, StorageType storageType) throws Exception {

        JetStreamTestHelper h = namer(nc);
        long size = storageLength(storageType, h.subject, h.dataLen);

        h.jsm.addStream(StreamConfiguration.builder()
                .name(h.streamName)
                .subjects(h.subject)
                .storageType(storageType)
                .discardPolicy(DiscardPolicy.Old)
                .maxBytes(size * 2 + 1)
                .build()
        );

        _discard_old(h);
    }

    private void _max_bytes_new(Connection nc, StorageType storageType) throws Exception {

        JetStreamTestHelper h = namer(nc);
        long size = storageLength(storageType, h.subject, h.dataLen);

        h.jsm.addStream(StreamConfiguration.builder()
                .name(h.streamName)
                .subjects(h.subject)
                .storageType(storageType)
                .discardPolicy(DiscardPolicy.New)
                .maxBytes(size * 2 + 1)
                .build()
        );

        _discard_new(h);
    }

    private void _byte_count(JetStreamTestHelper h, StorageType storageType) throws Exception {
        int expected = 0;

        for (int x = 0; x < 10; x++) {
            h.publish();
            expected += storageLength(storageType, h.subject, h.dataLen);
            assertEquals(expected, h.jsm.getStreamInfo(h.streamName).getStreamState().getByteCount());
        }

        for (int x = 0; x < 10; x++) {
            h.publishHeaders();
            expected += storageLength(storageType, h.getMsgWithHeaders());
            assertEquals(expected, h.jsm.getStreamInfo(h.streamName).getStreamState().getByteCount());
        }
    }
}