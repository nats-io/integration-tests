package io.nats.integration.utils;

import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Message;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StreamInfo;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import java.io.IOException;

import static io.nats.integration.utils.TestBase.randomBytes;

public class JetStreamTestHelper {
    public static final Headers TEST_HEADERS = new Headers().add("foo", "bar").add("foo", "baz").add("abcd", "wxyz");

    public int dataLen = 1;

    public String streamName;
    public String subject;
    public JetStreamManagement jsm;
    public JetStream js;
    public StreamInfo si;
    public byte[] dataBytes;
    public Message msgWithHeaders;

    public JetStreamTestHelper streamName(final String streamName) {
        this.streamName = streamName;
        return this;
    }

    public JetStreamTestHelper subject(final String subject) {
        this.subject = subject;
        return this;
    }

    public JetStreamTestHelper jsm(final JetStreamManagement jsm) {
        this.jsm = jsm;
        return this;
    }

    public JetStreamTestHelper js(final JetStream js) {
        this.js = js;
        return this;
    }

    public JetStreamTestHelper si(final StreamInfo si) {
        this.si = si;
        return this;
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
