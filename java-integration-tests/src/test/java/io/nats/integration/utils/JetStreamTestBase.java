package io.nats.integration.utils;

import io.nats.client.*;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JetStreamTestBase extends TestBase {

    public static final int STORAGE_OVERHEAD_MEMORY = 16;
    public static final int STORAGE_OVERHEAD_FILE = 30;
    public static final int STORAGE_OVERHEAD_HEADERS_MEMORY = 12;
    public static final int STORAGE_OVERHEAD_HEADERS_FILE = 16;
    public static final int STORAGE_OVERHEAD_PER_HEADERS_TUPLE = 3;

    // ----------------------------------------------------------------------------------------------------
    // Stream
    // ----------------------------------------------------------------------------------------------------
    public static StreamInfo createStream(JetStreamTestHelper h, StorageType storageType) throws IOException, JetStreamApiException {
        return createStream(h,
            StreamConfiguration.builder()
                .name(h.streamName)
                .storageType(storageType)
                .subjects(h.subject)
                .build()
        );
    }

    public static StreamInfo createStream(JetStreamTestHelper h, StreamConfiguration sc) throws IOException, JetStreamApiException {
        try {
            h.jsm.deleteStream(h.streamName);
        }
        catch (JetStreamApiException j) {
            // this is just a 404, so the stream does not exist
        }

        return h.jsm.addStream(sc);
    }

    public static void createTestMemoryStream(JetStreamTestHelper h) throws IOException, JetStreamApiException {
        createStream(h, StorageType.Memory);
    }

    public static StreamInfo createTestFileStream(JetStreamTestHelper h) throws IOException, JetStreamApiException {
        return createStream(h, StorageType.File);
    }

    // ----------------------------------------------------------------------------------------------------
    // Storage
    // ----------------------------------------------------------------------------------------------------
    public static int storageLength(StorageType storageType, String subject, int dataLen, Headers headers) {
        int len = subject.length() + dataLen + (storageType == StorageType.Memory ? STORAGE_OVERHEAD_MEMORY : STORAGE_OVERHEAD_FILE);
        if (headers != null && headers.size() > 0) {
            len += (storageType == StorageType.Memory ? STORAGE_OVERHEAD_HEADERS_MEMORY : STORAGE_OVERHEAD_HEADERS_FILE);
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String s : entry.getValue()) {
                    len += (entry.getKey().length() + s.length() + STORAGE_OVERHEAD_PER_HEADERS_TUPLE);
                }
            }
        }

        return len;
    }

    public static int storageLength(StorageType storageType, String subject, int dataLen) {
        return storageLength(storageType, subject, dataLen, null);
    }

    public static int storageLength(StorageType storageType, String subject, String data, Headers headers) {
        return storageLength(storageType, subject, (data == null ? 0 : data.length()), headers);
    }

    public static int storageLength(StorageType storageType, String subject, byte[] data, Headers headers) {
        return storageLength(storageType, subject, (data == null ? 0 : data.length), headers);
    }

    public static int storageLength(StorageType storageType, Message m) {
        return storageLength(storageType, m.getSubject(), m.getData(), m.getHeaders());
    }

    // ----------------------------------------------------------------------------------------------------
    // Data
    // ----------------------------------------------------------------------------------------------------
    public static final String DATA = "data";

    public static String data(int seq) {
        return DATA + "-" + seq;
    }

    public static byte[] dataBytes(int seq) {
        return data(seq).getBytes(StandardCharsets.US_ASCII);
    }

    // ----------------------------------------------------------------------------------------------------
    // Publish / Read
    // ----------------------------------------------------------------------------------------------------
    public static void jsPublish(JetStream js, String subject, String prefix, int count) throws IOException, JetStreamApiException {
        for (int x = 1; x <= count; x++) {
            String data = prefix + x;
            js.publish(NatsMessage.builder()
                .subject(subject)
                .data(data.getBytes(StandardCharsets.US_ASCII))
                .build()
            );
        }
    }

    public static void jsPublish(JetStream js, String subject, int startId, int count) throws IOException, JetStreamApiException {
        for (int x = 0; x < count; x++) {
            js.publish(NatsMessage.builder().subject(subject).data((dataBytes(startId++))).build());
        }
    }

    public static void jsPublish(JetStream js, String subject, int count) throws IOException, JetStreamApiException {
        jsPublish(js, subject, 1, count);
    }

    public static void jsPublish(Connection nc, String subject, int count) throws IOException, JetStreamApiException {
        jsPublish(nc.jetStream(), subject, 1, count);
    }

    public static void jsPublish(Connection nc, String subject, int startId, int count) throws IOException, JetStreamApiException {
        jsPublish(nc.jetStream(), subject, startId, count);
    }

    public static List<Message> readMessagesAck(JetStreamSubscription sub) throws InterruptedException {
        return readMessages(sub, AckPolicy.Explicit, false);
    }

    public static List<Message> readMessagesAck(JetStreamSubscription sub, boolean noisy) throws InterruptedException {
        return readMessages(sub, AckPolicy.Explicit, noisy);
    }

    public static List<Message> readMessages(JetStreamSubscription sub, AckPolicy ackPolicy) throws InterruptedException {
        return readMessages(sub, ackPolicy, false);
    }

    public static List<Message> readMessages(JetStreamSubscription sub, AckPolicy ackPolicy, boolean noisy) throws InterruptedException {
        List<Message> messages = new ArrayList<>();
        Message last = null;
        Message msg = sub.nextMessage(Duration.ofSeconds(1));
        while (msg != null) {
            last = msg;
            messages.add(msg);
            if (msg.isJetStream()) {
                if (noisy) {
                    System.out.println("ACK " + new String(msg.getData()));
                }
                if (ackPolicy == AckPolicy.Explicit) {
                    msg.ack();
                }
            }
            else if (msg.isStatusMessage()) {
                if (noisy) {
                    System.out.println("STATUS " + msg.getStatus());
                }
            }
            else if (noisy) {
                System.out.println("? " + new String(msg.getData()) + "?");
            }
            msg = sub.nextMessage(Duration.ofSeconds(1));
        }
        if (last != null && ackPolicy == AckPolicy.All) {
            last.ack();
        }
        return messages;
    }

    public static List<Message> readMessages(Iterator<Message> list) {
        List<Message> messages = new ArrayList<>();
        while (list.hasNext()) {
            messages.add(list.next());
        }
        return messages;
    }
}
