package io.nats.compatibility;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.support.JsonParser;
import io.nats.client.support.JsonValue;
import io.nats.client.support.JsonValueUtils;
import io.nats.utils.Debug;

public class Request {
    public final Connection nc;

    // info from the message
    public final String subject;
    public final String replyTo;

    // info from the subject
    public final Suite suite;
    public final Test test;
    public final String something;
    public final Kind kind;

    // info from the message data
    public final JsonValue dataValue;
    public final JsonValue config;
    public final String result;

    public Request(Connection nc, Message m) {
        this.nc = nc;
        this.replyTo = m.getReplyTo();
        this.subject = m.getSubject(); // tests.<suite>.<test>.<something>.[command|result]

        String[] split = subject.split("\\.");
        this.suite = Suite.instance(split[1]);
        if (suite == Suite.DONE) {
            this.test = null;
            this.something = null;
            this.kind = null;
            this.dataValue = null;
            this.config = null;
            this.result = null;
        }
        else {
            this.test = Test.instance(split[2]);
            this.something = split[3];
            this.kind = Kind.instance(split[4]);

            byte[] payload = m.getData();
            if (payload == null || payload.length == 0) {
                dataValue = null;
                config = null;
                result = "";
            }
            else {
                String data = new String(payload).trim();
                if (data.startsWith("{")) {
                    dataValue = JsonParser.parseUnchecked(m.getData());
                    config = JsonValueUtils.readObject(dataValue, "config");
                    result = null;
                }
                else {
                    dataValue = null;
                    config = null;
                    result = data;
                }
            }
        }
    }

    public Request(Request r) {
        this.nc = r.nc;
        this.subject = r.subject;
        this.replyTo = r.replyTo;
        this.suite = r.suite;
        this.test = r.test;
        this.something = r.something;
        this.kind = r.kind;
        this.dataValue = r.dataValue;
        this.config = r.config;
        this.result = r.result;
    }

    public boolean isCommand() {
        return dataValue != null;
    }

    public boolean isResult() {
        return dataValue == null;
    }

    protected void respond() {
        Debug.dbg("REPLY " + subject);
        nc.publish(replyTo, null);
    }

    protected void respond(String payload) {
        Debug.dbg("REPLY " + subject + " with " + payload);
        nc.publish(replyTo, payload.getBytes());
    }

    @Override
    public String toString() {
        if (isCommand()) {
            return dataValue.toJson();
        }

        if (result == null || result.isEmpty()) {
            return "";
        }

        if (result.contains("Config {")) {
            return "Result: " + result + "\n";
        }

        return "Result: " + result;
    }

    protected void handleException(Exception e) {
        Debug.err(subject, e);
//        System.exit(-3);
    }
}
