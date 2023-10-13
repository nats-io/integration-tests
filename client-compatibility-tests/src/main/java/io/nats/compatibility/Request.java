package io.nats.compatibility;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.support.JsonParser;
import io.nats.client.support.JsonValue;
import io.nats.client.support.JsonValueUtils;

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
    public final Command command;
    public final JsonValue config;
    public final String result;

    public Request(Connection nc, Message m, Suite suite, String subject) {
        this.nc = nc;
        this.subject = subject;
        this.replyTo = m.getReplyTo();

        this.suite = Suite.instance(subject);
        String temp = subject.substring(this.suite.prefixLen);
        int at = temp.indexOf(".");
        this.test = Test.instance(temp.substring(0, at));
        temp = temp.substring(at + 1);
        at = temp.lastIndexOf(".");
        this.something = temp.substring(0, at);
        this.kind = Kind.instance(temp.substring(at + 1));

        String data = new String(m.getData());
        if (data.startsWith("{")) {
            dataValue = JsonParser.parseUnchecked(m.getData());

            // validate the suite, should never be an issue
            String suiteId = JsonValueUtils.readString(dataValue, "suite");
            if (!suite.id.equals(suiteId)) {
                throw new IllegalArgumentException("Invalid Suite Id");
            }

            command = Command.instance(JsonValueUtils.readString(dataValue, "command"));
            config = JsonValueUtils.readObject(dataValue, "config");
            result = null;
        }
        else {
            dataValue = null;
            command = null;
            config = null;
            result = data;
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
        this.command = r.command;
        this.config = r.config;
        this.result = r.result;
    }

    public boolean isCommand() {
        return command != null;
    }

    public boolean isResult() {
        return result != null;
    }

    protected void respond() {
        nc.publish(replyTo, null);
    }

    @Override
    public String toString() {
        return "Request{" +
            "suite=" + suite +
            ", test=" + test +
            ", something='" + something + '\'' +
            ", kind=" + kind +
            ", dataValue=" + dataValue +
            ", command=" + command +
            ", config=" + config +
            ", result='" + result + '\'' +
            '}';
    }
}
