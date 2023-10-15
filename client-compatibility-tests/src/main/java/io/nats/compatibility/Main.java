package io.nats.compatibility;

import io.nats.client.*;
import io.nats.utils.Debug;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("CallToPrintStackTrace")
public class Main {
    public static ExecutorService EXEC_SERVICE = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {

        Options options = new Options.Builder()
            .server("nats://localhost:4222")
            .errorListener(new ErrorListener() {})
            .build();

        try (Connection nc = Nats.connect(options)) {
            Dispatcher d = nc.createDispatcher();
            d.subscribe("tests.>", m-> {
                try {
                    Request request = new Request(nc, m);
                    if (request.suite == Suite.DONE) {
                        System.exit(0);
                    }
                    else if (request.isCommand()) {
                        EXEC_SERVICE.submit(() -> {
                            try {
                                Debug.dbg("CMD", request.subject, request);
                                //noinspection SwitchStatementWithTooFewBranches
                                switch (request.suite) {
                                    case OBJECT_STORE:
                                        new ObjectStoreSuiteRequest(request).execute();
                                        break;
                                    default:
                                        throw new UnsupportedOperationException("Unsupported suite: " + request.suite);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                System.exit(-2);
                            }
                        });
                    }
                    else if (request.subject.contains(".pass")) {
                        Debug.dbg("PASS", request.subject);
                    }
                    else if (request.subject.contains(".fail")) {
                        Debug.err("FAIL", request.subject);
                    }
                    else {
                        Debug.dbg("INFO", request.subject);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
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
