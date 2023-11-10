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

package io.nats.integration.utils;

import io.nats.client.Connection;
import io.nats.client.NUID;
import io.nats.client.Nats;
import io.nats.client.Options;
import nats.io.ConsoleOutput;
import nats.io.NatsServerRunner;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class TestBase {

    public static final String PLAIN      = "plain";
    public static final String HAS_SPACE  = "has space";
    public static final String HAS_DASH   = "has-dash";
    public static final String HAS_DOT    = "has.dot";
    public static final String HAS_STAR   = "has*star";
    public static final String HAS_GT     = "has>gt";
    public static final String HAS_DOLLAR = "has$dollar";
    public static final String HAS_TAB    = "has\tgt";

    public static final long STANDARD_CONNECTION_WAIT_MS = 5000;
    public static final long LONG_CONNECTION_WAIT_MS = 10000;
    public static final long STANDARD_FLUSH_TIMEOUT_MS = 2000;
    public static final long MEDIUM_FLUSH_TIMEOUT_MS = 5000;
    public static final long LONG_FLUSH_TIMEOUT_MS = 15000;

    static {
        NatsServerRunner.setDefaultOutputSupplier(ConsoleOutput::new);
        NatsServerRunner.setDefaultOutputLevel(Level.WARNING);
    }

    // ----------------------------------------------------------------------------------------------------
    // runners
    // ----------------------------------------------------------------------------------------------------
    public interface InConnectionTest {
        void test(Connection nc) throws Exception;
    }

    public interface InClusterTest {
        void test(List<Connection> connections) throws Exception;
    }

    public static void runInServer(InConnectionTest inConnectionTest) throws Exception {
        runInServer(false, false, inConnectionTest);
    }

    public static void runInServer(Options.Builder builder, InConnectionTest inConnectionTest) throws Exception {
        runInServer(false, false, builder, inConnectionTest);
    }

    public static void runInServer(boolean debug, InConnectionTest inConnectionTest) throws Exception {
        runInServer(debug, false, inConnectionTest);
    }

    public static void runInJsServer(InConnectionTest inConnectionTest) throws Exception {
        runInServer(false, true, inConnectionTest);
    }

//    public static void runInJsSimpleCluster(InClusterTest inClusterTest) throws Exception {
//        List<ClusterInsert> clusterInserts = createClusterInserts();
//        ClusterInsert ci1 = clusterInserts.get(0);
//        ClusterInsert ci2 = clusterInserts.get(1);
//        ClusterInsert ci3 = clusterInserts.get(2);
//
//        try (NatsServerRunner runner1 = new NatsServerRunner(ci1.node.port, false, true, null, ci1.configInserts, null);
//             NatsServerRunner runner2 = new NatsServerRunner(ci2.node.port, false, true, null, ci2.configInserts, null);
//             NatsServerRunner runner3 = new NatsServerRunner(ci3.node.port, false, true, null, ci3.configInserts, null);
//             Connection nc1 = connectionWait(runner1.getURI(), LONG_CONNECTION_WAIT_MS);
//             Connection nc2 = connectionWait(runner2.getURI(), LONG_CONNECTION_WAIT_MS);
//             Connection nc3 = connectionWait(runner3.getURI(), LONG_CONNECTION_WAIT_MS)
//        ) {
//            System.out.println(nc1.getServerInfo());
//            System.out.println(nc2.getServerInfo());
//            System.out.println(nc3.getServerInfo());
//            sleep(30000); // just making sure the cluster is ready
//            inClusterTest.test(Arrays.asList(nc1, nc2, nc3));
//        }
//    }

    public static void runInJsServer(boolean debug, InConnectionTest inConnectionTest) throws Exception {
        runInServer(debug, true, inConnectionTest);
    }

    public static void runInServer(boolean debug, boolean jetstream, InConnectionTest inConnectionTest) throws Exception {
        try (NatsServerRunner runner = new NatsServerRunner(debug, jetstream);
             Connection nc = standardConnection(runner.getURI()))
        {
            inConnectionTest.test(nc);
        }
    }

    public static void runInServer(boolean debug, boolean jetstream, Options.Builder builder, InConnectionTest inConnectionTest) throws Exception {
        try (NatsServerRunner runner = new NatsServerRunner(debug, jetstream);
             Connection nc = standardConnection(builder.server(runner.getURI()).build()))
        {
            inConnectionTest.test(nc);
        }
    }

    public static void runInExternalServer(InConnectionTest inConnectionTest) throws Exception {
        runInExternalServer(Options.DEFAULT_URL, inConnectionTest);
    }

    public static void runInExternalServer(String url, InConnectionTest inConnectionTest) throws Exception {
        try (Connection nc = Nats.connect(url)) {
            inConnectionTest.test(nc);
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // assertions
    // ----------------------------------------------------------------------------------------------------
    public static void assertConnected(Connection conn) {
        assertSame(Connection.Status.CONNECTED, conn.getStatus(),
                () -> expectingMessage(conn, Connection.Status.CONNECTED));
    }

    public static void assertNotConnected(Connection conn) {
        assertNotSame(Connection.Status.CONNECTED, conn.getStatus(),
                () -> "Failed not expecting Connection Status " + Connection.Status.CONNECTED.name());
    }

    public static void assertClosed(Connection conn) {
        assertSame(Connection.Status.CLOSED, conn.getStatus(),
                () -> expectingMessage(conn, Connection.Status.CLOSED));
    }

    public static void assertCanConnect() throws IOException, InterruptedException {
        standardCloseConnection( standardConnection() );
    }

    public static void assertCanConnect(String serverURL) throws IOException, InterruptedException {
        standardCloseConnection( standardConnection(serverURL) );
    }

    public static void assertCanConnect(Options options) throws IOException, InterruptedException {
        standardCloseConnection( standardConnection(options) );
    }

    public static void assertByteArraysEqual(byte[] data1, byte[] data2) {
        if (data1 == null) {
            assertNull(data2);
            return;
        }
        assertNotNull(data2);
        assertEquals(data1.length, data2.length);
        for (int x = 0; x < data1.length; x++) {
            assertEquals(data1[x], data2[x]);
        }
    }

    public static void assertBytesArraysNotEqual(byte[] data1, byte[] data2) {
        if (data1 == null) {
            assertNotNull(data2);
            return;
        }
        if (data2 == null) {
            return;
        }

        if (data1.length != data2.length) {
            return;
        }

        for (int x = 0; x < data1.length; x++) {
            if (data1[x] != data2[x]) {
                return;
            }
        }

        fail("byte arrays are equals");
    }

    // ----------------------------------------------------------------------------------------------------
    // utils / macro utils
    // ----------------------------------------------------------------------------------------------------
    public static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { /* ignored */ }
    }

    // ----------------------------------------------------------------------------------------------------
    // flush
    // ----------------------------------------------------------------------------------------------------
    public static void flushConnection(Connection conn) {
        flushConnection(conn, Duration.ofMillis(STANDARD_FLUSH_TIMEOUT_MS));
    }

    public static void flushConnection(Connection conn, long timeoutMillis) {
        flushConnection(conn, Duration.ofMillis(timeoutMillis));
    }

    public static void flushConnection(Connection conn, Duration timeout) {
        try { conn.flush(timeout); } catch (Exception exp) { /* ignored */ }
    }

    // ----------------------------------------------------------------------------------------------------
    // connect or wait for a connection
    // ----------------------------------------------------------------------------------------------------

    public static Connection standardConnection() throws IOException, InterruptedException {
        return standardConnectionWait( Nats.connect() );
    }

    public static Connection standardConnection(String serverURL) throws IOException, InterruptedException {
        return standardConnectionWait( Nats.connect(serverURL) );
    }

    public static Connection standardConnection(Options options) throws IOException, InterruptedException {
        return standardConnectionWait( Nats.connect(options) );
    }

    public static Connection standardConnectionWait(Connection conn) {
        return connectionWait(conn, STANDARD_CONNECTION_WAIT_MS);
    }

    public static Connection connectionWait(String serverURL, long millis) throws IOException, InterruptedException {
        return connectionWait( Nats.connect(serverURL), millis );
    }

    public static Connection connectionWait(Options options, long millis) throws IOException, InterruptedException {
        return connectionWait( Nats.connect(options), millis );
    }

    public static Connection connectionWait(Connection conn, long millis) {
        return waitUntilStatus(conn, millis, Connection.Status.CONNECTED);
    }

    // ----------------------------------------------------------------------------------------------------
    // close
    // ----------------------------------------------------------------------------------------------------
    public static void standardCloseConnection(Connection conn) {
        closeConnection(conn, STANDARD_CONNECTION_WAIT_MS);
    }

    public static void closeConnection(Connection conn, long millis) {
        if (conn != null) {
            close(conn);
            waitUntilStatus(conn, millis, Connection.Status.CLOSED);
            assertClosed(conn);
        }
    }

    public static void close(Connection conn) {
        try { conn.close(); } catch (InterruptedException e) { /* ignored */ }
    }

    // ----------------------------------------------------------------------------------------------------
    // connection waiting
    // ----------------------------------------------------------------------------------------------------
    public static Connection waitUntilStatus(Connection conn, long millis, Connection.Status waitUntilStatus) {
        long times = (millis + 99) / 100;
        for (long x = 0; x < times; x++) {
            sleep(100);
            if (conn.getStatus() == waitUntilStatus) {
                return conn;
            }
        }

        throw new AssertionFailedError(expectingMessage(conn, waitUntilStatus));
    }

    private static String expectingMessage(Connection conn, Connection.Status expecting) {
        return "Failed expecting Connection Status " + expecting.name() + " but was " + conn.getStatus();
    }

    // ----------------------------------------------------------------------------------------------------
    // misc
    // ----------------------------------------------------------------------------------------------------
    public static String uniqueEnough() {
        return new NUID().next();
    }

    public static String uniqueEnough(String prefix) {
        return prefix + "-" + uniqueEnough();
    }

    public static String uniqueEnough(String prefix, String dfltPrefix) {
        return (prefix == null ? dfltPrefix : prefix) + "-" + uniqueEnough();
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(new NUID().next());
        }
        return sb.substring(0, length);
    }

    static final byte[] RANDOM_BYTES;
    static final int RANDOM_BYTES_LEN;

    static {
        RANDOM_BYTES = new byte[1000];
        ThreadLocalRandom.current().nextBytes(RANDOM_BYTES);
        RANDOM_BYTES_LEN = RANDOM_BYTES.length;
    }

    public static byte randomByte() {
        return randomBytes(1)[0];
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    // ----------------------------------------------------------------------------------------------------
    // printing
    // ----------------------------------------------------------------------------------------------------
    static final String INDENT = "                        ";
    private static String indent(int level) {
        return level == 0 ? "" : INDENT.substring(0, level * 4);
    }

    public static void printFormatted(Object o) {
        int level = 0;
        boolean indentNext = true;
        String s = o.toString();
        for (int x = 0; x < s.length(); x++) {
            char c = s.charAt(x);
            if (c == '{') {
                System.out.print(c + "\n");
                ++level;
                indentNext = true;
            }
            else if (c == '}') {
                System.out.print("\n" + indent(--level) + c);
            }
            else if (c == ',') {
                System.out.print("\n");
                indentNext = true;
            }
            else {
                if (indentNext) {
                    if (c != ' ') {
                        System.out.print(indent(level) + c);
                        indentNext = false;
                    }
                }
                else {
                    System.out.print(c);
                }
            }
        }

        System.out.println();
    }
}
