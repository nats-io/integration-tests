package io.nats;

import io.nats.client.*;
import io.nats.client.support.JsonValue;
import io.nats.service.Service;
import io.nats.service.ServiceBuilder;
import io.nats.service.ServiceEndpoint;
import io.nats.service.ServiceMessageHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.nats.service.ServiceMessage.NATS_SERVICE_ERROR;
import static io.nats.service.ServiceMessage.NATS_SERVICE_ERROR_CODE;

public class ServiceCrossClientValidator {

    // TO TEST, RUN THIS CLASS THEN THIS COMMAND:
    // deno run --unstable -A https://raw.githubusercontent.com/nats-io/nats.deno/main/tests/helpers/service-check.ts --server localhost:4222 --name JavaCrossClientValidator

    // TO RESET TEST CODE IF THERE ARE UPDATES:
    // deno cache --reload "https://raw.githubusercontent.com/nats-io/nats.deno/main/tests/helpers/service-check.ts"

    public static void main(String[] args) throws IOException {

        Options options = new Options.Builder()
            .server("nats://localhost:4222")
            .errorListener(new ErrorListener() {})
            .build();

        try (Connection nc = Nats.connect(options)) {
            ServiceMessageHandler handler = smsg -> {
                byte[] payload = smsg.getData();
                if (payload == null || payload.length == 0) {
                    smsg.respondStandardError(nc, "need a string", 400);
                }
                else {
                    String data = new String(payload);
                    if (data.equals("error")) {
                        throw new RuntimeException("service asked to throw an error");
                    }
                    smsg.respond(nc, payload);
                }
            };

            // create the services
            Service service = new ServiceBuilder()
                .connection(nc)
                .name("JavaCrossClientValidator")
                .description("Java Cross Client Validator")
                .version("0.0.1")
                .addServiceEndpoint(ServiceEndpoint.builder()
                    .endpointName("jccv")
                    .endpointSchemaRequest("schema request string/url")
                    .endpointSchemaResponse("schema response string/url")
                    .statsDataSupplier(() -> new JsonValue(randomText()))
                    .handler(handler)
                    .build())
                .build();

            System.out.println(service);

            CompletableFuture<Boolean> doneFuture = service.startService();

            CompletableFuture<Message> reply = nc.request("jccv", "hello".getBytes());
            Message msg = reply.get();
            String response = new String(msg.getData());
            System.out.println("Called jccv with 'hello'. Received [" + response + "]");

            reply = nc.request("jccv", null);
            msg = reply.get();
            String se = msg.getHeaders().getFirst(NATS_SERVICE_ERROR);
            String sec = msg.getHeaders().getFirst(NATS_SERVICE_ERROR_CODE);
            System.out.println("Called jccv with null. Received [" + se + ", " + sec + "]");

            reply = nc.request("jccv", "".getBytes());
            msg = reply.get();
            se = msg.getHeaders().getFirst(NATS_SERVICE_ERROR);
            sec = msg.getHeaders().getFirst(NATS_SERVICE_ERROR_CODE);
            System.out.println("Called jccv with empty. Received [" + se + ", " + sec + "]");

            reply = nc.request("jccv", "error".getBytes());
            msg = reply.get();
            se = msg.getHeaders().getFirst(NATS_SERVICE_ERROR);
            sec = msg.getHeaders().getFirst(NATS_SERVICE_ERROR_CODE);
            System.out.println("Called jccv with 'error'. Received [" + se + ", " + sec + "]");

            try {
                doneFuture.get(1, TimeUnit.MINUTES);
            }
            catch (Exception ignore) {
                // We expect this to timeout because we don't stop the service,
                // so it can run long enough for the test to complete.
                // You can just stop the program also.
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String randomText() {
        return Long.toHexString(System.currentTimeMillis()) + Long.toHexString(System.nanoTime());
    }
}

