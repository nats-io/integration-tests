package ngs;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Limits extends Base {

    public static void main(String[] args) throws Exception {
        Options options = new Options.Builder()
            .server(Options.DEFAULT_URL)
            .build();

        Limits l = new Limits(options);
//        l.conns(10);
//        l.subs(10);
//        l.stream(10);
//        l.consumers(10);
//        l.memoryStorage(1024 * 1024);
//        l.diskStorage(1024 * 1024);
//        System.exit(0); // not sure why it's not exiting so force it, figure out later
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
