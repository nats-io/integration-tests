package io.nats.compatibility;

import io.nats.client.*;
import io.nats.utils.Debug;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        Options options = new Options.Builder()
            .server("nats://localhost:4222")
            .errorListener(new ErrorListener() {})
            .build();

        try (Connection nc = Nats.connect(options)) {
            Dispatcher d = nc.createDispatcher();
            d.subscribe("tests.>", m -> {
                String subject = m.getSubject();
                Suite suite = Suite.instance(subject);
                Request request = new Request(nc, m, suite, subject);
                if (request.isResult()) {
                    Debug.msg("RESULT", m);
                }
                else {
                    Debug.dbg("REQUEST", request);
                    switch (suite) {
                        case OBJECT_STORE:
                            ObjectStore.execute(request);
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported suite: " + suite);
                    }
                }
            });
            Debug.dbg("Ready");
            Thread.sleep(600000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
