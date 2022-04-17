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

package ngs;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import ngs.objects.NscAccount;
import ngs.objects.NscConfig;
import ngs.utils.Base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ngs.utils.NscUtils.getDefaultAccount;
import static ngs.utils.NscUtils.getNscConfig;

public class Limits extends Base {

    public static void main(String[] args) throws Exception {
        NscConfig nscConfig = getNscConfig();
        NscAccount nscAccount = getDefaultAccount(nscConfig);

        Options options = new Options.Builder()
            .server(nscAccount.getUrl())
            .authHandler(Nats.credentials(nscAccount.getCreds()))
            .build();

        Limits l = new Limits(options);
        l.conns(1);

        System.exit(0); // not sure why it's not exiting so force it, figure out later
    }

    public Limits(Options options) {
        super(options, null);
    }

    public Limits(Options options, JetStreamOptions jetStreamOptions) {
        super(options, jetStreamOptions);
    }

    public void conns(int count) throws IOException, InterruptedException {
        List<Connection> conns = new ArrayList<>();
        try {
            for (int x = 0; x < count; x++) {
                Connection conn = connect();
                conns.add(conn);
            }
        }
        finally {
            for (Connection conn : conns) {
                try {
                    conn.close();
                }
                catch (Exception ignore) {}
            }
        }
    }

    public void payload(int bytes) throws Exception {
        run(nc -> {
            byte[] payload = new byte[bytes];
            nc.publish(name("pyld"), payload);
        });
    }

    public void subs(int count) throws Exception {
        run(nc -> {
            String subject = name("subs");
            List<Subscription> list = new ArrayList<>();
            for (int x = 0; x < count; x++) {
                list.add(nc.subscribe(subject));
            }
        });
    }

    public void stream(int count) throws Exception {
        run(nc -> {
            for (int x = 0; x < count; x++) {
                String stream = name("strm");
                String subject = name("sub");
                createMemoryStream(nc, stream, subject);
            }
        });
    }

    public void consumers(int count) throws Exception {
        runStream( (nc, stream, subject) -> {
            JetStreamManagement jsm = nc.jetStreamManagement(jetStreamOptions);
            for (int x = 0; x < count; x++) {
                ConsumerConfiguration cc = ConsumerConfiguration.builder()
                    .durable(name("dur"))
                    .build();
                jsm.addOrUpdateConsumer(stream, cc);
            }
        });
    }

    public void memoryStorage(int bytes) throws Exception {
        run(nc -> {
            String stream = name("strm");
            String subject = name("sub");
            createMemoryStream(nc, stream, subject);
            publish(nc, subject, bytes);
        });
    }

    public void diskStorage(int bytes) throws Exception {
        run(nc -> {
            String stream = name("strm");
            String subject = name("sub");
            createFileStream(nc, stream, subject);
            publish(nc, subject, bytes);
        });
    }

    private void publish(Connection nc, String subject, int bytes) throws Exception {
        JetStream js = nc.jetStream(jetStreamOptions);
        byte[] buf = new byte[PAYLOAD_SIZE];
        while (bytes > 0) {
            if (buf.length > bytes) {
                buf = new byte[bytes];
            }
            js.publish(subject, buf);
            bytes -= buf.length;
        }
        sleep(10000);
    }

    public void maxAckPending(int bytes) throws Exception {
        run(nc -> {
        });
    }

    public void memoryMaxStreamBytes(int bytes) throws Exception {
        run(nc -> {
        });
    }

    public void diskMaxStreamBytes(int bytes) throws Exception {
        run(nc -> {
        });
    }
}
