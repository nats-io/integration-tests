package io.nats.integration.utils;

import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import java.io.IOException;

import static io.nats.integration.utils.TestBase.randomBytes;
import static io.nats.integration.utils.TestBase.uniqueEnough;

public class JetStreamTestHelper {
    public static final Headers TEST_HEADERS = new Headers().add("foo", "bar").add("foo", "baz").add("abcd", "wxyz");

    public int dataLen = 1;

    public String streamName;
    public String subject;
    public JetStreamManagement jsm;
    public JetStream js;
    public byte[] dataBytes;
    public Message msgWithHeaders;

    public JetStreamTestHelper(Connection nc) throws IOException {
        this(nc, null, null);
    }

    public JetStreamTestHelper(Connection nc, String streamName, String subject) throws IOException {
        jsm = nc.jetStreamManagement();
        js = nc.jetStream();
        this.streamName = uniqueEnough(streamName, "stream");
        this.subject = uniqueEnough(subject, "subject");
    }

    public byte[] getDataBytes() {
        if (dataBytes == null || dataLen != dataBytes.length) {
            dataBytes = randomBytes(dataLen);
        }
        return dataBytes;
    }

    public Message getMsgWithHeaders() {
        if (msgWithHeaders == null || dataBytes == null || dataLen != dataBytes.length) {
            msgWithHeaders = NatsMessage.builder()
                    .subject(subject)
                    .data(getDataBytes())
                    .headers(TEST_HEADERS)
                    .build();
        }
        return msgWithHeaders;
    }

    public PublishAck publish() throws IOException, JetStreamApiException {
        return js.publish(subject, getDataBytes());
    }

    public PublishAck publishHeaders() throws IOException, JetStreamApiException {
        return js.publish(getMsgWithHeaders());
    }

    public void publish(int count) throws IOException, JetStreamApiException {
        for (int x = 0; x < count; x++) {
            js.publish(subject, getDataBytes());
        }
    }

    public void publishHeaders(int count) throws IOException, JetStreamApiException {
        for (int x = 0; x < count; x++) {
            js.publish(getMsgWithHeaders());
        }
    }
}
