// Copyright 2020 The NATS Authors
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

package io.nats.utils;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Message;
import io.nats.client.api.ConsumerInfo;
import io.nats.client.api.StreamInfo;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import io.nats.client.impl.NatsMessage;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("SameParameterValue")
public abstract class Debug {
    public static final String SEP = " | ";
    public static boolean DO_NOT_TRUNCATE = true;
    public static boolean PRINT_THREAD_ID = true;

    private Debug() {}  /* ensures cannot be constructed */

    public static void msg(Message msg) {
        msg(null, msg, null);
    }

    public static void msg(String label, Message msg) {
        _dbg(label, msg, null, true);
    }

    public static void msg(Message msg, Object extra) {
        _dbg(null, msg, extra, true);
    }

    public static void msg(String label, Message msg, Object extra) {
        _dbg(label, msg, extra, true);
    }

    public static void dbg(String label) {
        _dbg(label, null, null, false);
    }

    public static void dbg(String label, Object extra) {
        if (extra instanceof NatsMessage) {
            _dbg(label, (NatsMessage)extra, null, false);
        }
        else {
            _dbg(label, null, extra, false);
        }
    }

    private static void _dbg(String label, Message msg, Object extra, boolean forMsg) {
        String start;
        if (PRINT_THREAD_ID) {
            start = "[" + Thread.currentThread().getName() + "@" + time() + "] ";
        }
        else {
            start = "[" + time() + "] ";
        }

        if (label != null) {
            label = label.trim();
        }
        if (label == null || label.isEmpty()) {
            label = start; //  + "--->" + SEP;
        }
        else {
            label = start + label + SEP;
        }

        extra = extra == null ? "" : extra + SEP;

        if (msg == null) {
            if (forMsg) {
                System.out.println(label + "<null>" + SEP + extra);
            }
            else {
                System.out.println(label + extra);
            }
            return;
        }

        if (msg.isStatusMessage()) {
            System.out.println(label + sidString(msg) + subjString(msg) + msg.getStatus() + extra);
        }
        else if (msg.isJetStream()) {
            System.out.println(label + sidString(msg) + subjString(msg) + dataString(msg) + replyToString(msg) + extra);
        }
        else if (msg.getSubject() == null) {
            System.out.println(label + sidString(msg) + msg + extra);
        }
        else {
            System.out.println(label + sidString(msg) + subjString(msg) + dataString(msg) + replyToString(msg) + extra);
        }
        debugHdr(label.length() + 1, msg);
    }

    private static String sidString(Message msg) {
        return ""; // ""sid:" + msg.getSID() + SEP;
    }

    private static String subjString(Message msg) {
        return msg.getSubject() + SEP;
    }

    private static String replyToString(Message msg) {
        if (msg.isJetStream()) {
            NatsJetStreamMetaData meta = msg.metaData();
            return "ss:" + meta.streamSequence() + ' '
                + "cc:" + meta.consumerSequence() + ' '
                + "dlvr:" + meta.deliveredCount() + ' '
                + "pnd:" + meta.pendingCount()
                + SEP;
        }
        return msg.getReplyTo();
    }

    private static String time() {
        String t = "" + System.currentTimeMillis();
        return t.substring(t.length() - 9);
    }

    private static String dataString(Message msg) {
        byte[] data = msg.getData();
        if (data == null || data.length == 0) {
            return "<no data>" + SEP;
        }
        String s = new String(data, UTF_8);
        if (DO_NOT_TRUNCATE) {
            return s + SEP;
        }

        int at = s.indexOf("io.nats.jetstream.api");
        if (at == -1) {
            return s.length() > 27 ? s.substring(0, 27) + "..." : s;
        }
        int at2 = s.indexOf('"', at);
        return s.substring(at, at2) + SEP;
    }

    private final static String PAD = "                                                            ";
    public static void debugHdr(int indent, Message msg) {
        Headers h = msg.getHeaders();
        if (h != null && !h.isEmpty()) {
            String pad = PAD.substring(0, indent);
            for (String key : h.keySet()) {
                System.out.println(pad + key + "=" + h.get(key));
            }
        }
    }

    public static void streamAndConsumer(Connection nc, String stream, String durable) throws IOException, JetStreamApiException {
        streamAndConsumer(nc.jetStreamManagement(), stream, durable);
    }

    public static void streamAndConsumer(JetStreamManagement jsm, String stream, String durable) throws IOException, JetStreamApiException {
        printStreamInfo(jsm.getStreamInfo(stream));
        printConsumerInfo(jsm.getConsumerInfo(stream, durable));
    }

    public static void consumer(Connection nc, String stream, String durable) throws IOException, JetStreamApiException {
        consumer(nc.jetStreamManagement(), stream, durable);
    }

    public static void consumer(JetStreamManagement jsm, String stream, String durable) throws IOException, JetStreamApiException {
        ConsumerInfo ci = jsm.getConsumerInfo(stream, durable);
        System.out.println("Consumer numPending=" + ci.getNumPending() + " numWaiting=" + ci.getNumWaiting() + " numAckPending=" + ci.getNumAckPending());
    }

    public static void printStreamInfo(StreamInfo si) {
        printObject(si, "StreamConfiguration", "StreamState", "ClusterInfo", "Mirror", "subjects", "sources");
    }

    public static void printStreamInfoList(List<StreamInfo> list) {
        printObject(list, "!StreamInfo", "StreamConfiguration", "StreamState");
    }

    public static void printConsumerInfo(ConsumerInfo ci) {
        printObject(ci, "ConsumerConfiguration", "Delivered", "AckFloor");
    }

    public static void printConsumerInfoList(List<ConsumerInfo> list) {
        printObject(list, "!ConsumerInfo", "ConsumerConfiguration", "Delivered", "AckFloor");
    }

    public static void printObject(Object o, String... subObjectNames) {
        String s = o.toString();
        for (String sub : subObjectNames) {
            boolean noIndent = sub.startsWith("!");
            String sb = noIndent ? sub.substring(1) : sub;
            String rx1 = ", " + sb;
            String repl1 = (noIndent ? ",\n": ",\n    ") + sb;
            s = s.replace(rx1, repl1);
        }
        System.out.println(s);
    }

    public static String pad2(int n) {
        return n < 10 ? " " + n : "" + n;
    }

    public static String pad3(int n) {
        return n < 10 ? "  " + n : (n < 100 ? "  " + n : "" + n);
    }

    public static String yn(boolean b) {
        return b ? "Yes" : "No ";
    }
}
