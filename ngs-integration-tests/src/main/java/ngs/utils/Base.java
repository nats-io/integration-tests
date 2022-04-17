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

package ngs.utils;

import io.nats.client.*;
import io.nats.client.api.ServerInfo;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.io.IOException;
import java.util.List;

public class Base {

    public long CONNECTION_WAIT_MS = 5000;
    public int PAYLOAD_SIZE = 128;

    protected final Options options;
    protected final JetStreamOptions jetStreamOptions;

    public Base(Options options) {
        this(options, null);
    }

    public Base(Options options, JetStreamOptions jetStreamOptions) {
        this.options = options;
        this.jetStreamOptions = jetStreamOptions;
    }

    public Connection connect() throws IOException, InterruptedException {
        Connection conn = Nats.connect(options);
        waitUntilStatus(conn, CONNECTION_WAIT_MS, Connection.Status.CONNECTED);
        return conn;
    }

    // ----------------------------------------------------------------------------------------------------
    // Util
    // ----------------------------------------------------------------------------------------------------
    public void sleep(long millis) {
        try {
            if (millis > 0) {
                Thread.sleep(millis);
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public String name(String prefix) {
        return prefix + "-" + NUID.nextGlobal();
    }

    // ----------------------------------------------------------------------------------------------------
    // Stream
    // ----------------------------------------------------------------------------------------------------
    public StreamInfo createStream(Connection nc, StreamConfiguration sc) throws IOException, JetStreamApiException {
        JetStreamManagement jsm = nc.jetStreamManagement(jetStreamOptions);
        deleteStream(jsm, sc.getName());
        return jsm.addStream(sc);
    }

    public void deleteStream(JetStreamManagement jsm, String stream) throws IOException {
        try {
            jsm.deleteStream(stream);
        }
        catch (JetStreamApiException j) {
            // this is just a 404, so the stream does not exist
        }
    }

    public void deleteStream(Connection nc, String stream) throws IOException {
        deleteStream(nc.jetStreamManagement(jetStreamOptions), stream);
    }

    // ----------------------------------------------------------------------------------------------------
    // run / connection
    // ----------------------------------------------------------------------------------------------------

    public interface ConnectionTest {
        void test(Connection nc) throws Exception;
    }

    public interface StreamSubjectTest {
        void test(Connection nc, String stream, String subject) throws Exception;
    }

    public void run(ConnectionTest connectionTest) throws Exception {
        ServerInfo si = null;
        try (Connection nc = connect()) {
            try {
                si = nc.getServerInfo();
                connectionTest.test(nc);
            }
            finally {
                if (si != null && si.isJetStreamAvailable()) {
                    List<String> streams = nc.jetStreamManagement(jetStreamOptions).getStreamNames();
                    for (String stream : streams) {
                        try {
                            deleteStream(connect(), stream);
                        }
                        catch (Exception ignore) {}
                    }
                }
            }
        }
    }

    public void runStream(StreamSubjectTest streamSubjectTest) throws Exception {
        run(nc -> {
            String stream = name("strm");
            String subject = name("sub");
            createMemoryStream(nc, stream, subject);
            streamSubjectTest.test(nc, stream, subject);
        });
    }

    public void createMemoryStream(Connection nc, String stream, String subject) throws IOException, JetStreamApiException {
        StreamConfiguration sc = StreamConfiguration.builder()
            .name(stream)
            .subjects(subject)
            .storageType(StorageType.Memory)
            .build();
        createStream(nc, sc);
    }

    public void createFileStream(Connection nc, String stream, String subject) throws IOException, JetStreamApiException {
        StreamConfiguration sc = StreamConfiguration.builder()
            .name(stream)
            .subjects(subject)
            .storageType(StorageType.File)
            .build();
        createStream(nc, sc);
    }

    public class ConnectionWaitException extends IOException {
        public ConnectionWaitException() {
            super("Too too long to connect.");
        }
    }

    public void waitUntilStatus(Connection conn, long millis, Connection.Status waitUntilStatus) throws ConnectionWaitException {
        long times = (millis + 99) / 100;
        for (long x = 0; x < times; x++) {
            sleep(100);
            if (conn.getStatus() == waitUntilStatus) {
                return;
            }
        }

        throw new ConnectionWaitException();
    }
}
