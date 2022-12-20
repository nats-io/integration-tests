package io.nats;

import io.nats.client.*;
import io.nats.service.Service;
import io.nats.service.ServiceBuilder;
import io.nats.service.ServiceMessage;
import io.nats.service.StatsData;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.nats.service.ServiceMessage.NATS_SERVICE_ERROR;
import static io.nats.service.ServiceMessage.NATS_SERVICE_ERROR_CODE;

public class ServiceCrossClientValidator {

    // TO TEST, RUN THIS CLASS THEN THIS COMMAND:
    // deno run -A https://raw.githubusercontent.com/nats-io/nats.deno/main/tests/helpers/service-check.ts --server localhost:4222 --name JavaCrossClientValidator

    // TO RESET TEST CODE IF THERE ARE UPDATES:
    // deno cache --reload "https://raw.githubusercontent.com/nats-io/nats.deno/main/tests/helpers/service-check.ts"

    public static void main(String[] args) throws IOException {

        Options options = new Options.Builder()
            .server("nats://localhost:4222")
            .errorListener(new ErrorListener() {})
            .build();

        Supplier<StatsData> sds = () -> new CcvData(randomText());
        Function<String, StatsData> sdd = json -> {
            if (json.startsWith("\"") && json.endsWith("\"")) {
                return new CcvData(json.substring(1, json.length() - 1));
            }
            return new CcvData(json);
        };

        try (Connection nc = Nats.connect(options)) {
            MessageHandler handler = request -> {
                byte[] payload = request.getData();
                if (payload == null || payload.length == 0) {
                    ServiceMessage.replyStandardError(nc, request, "need a string", 400);
                }
                else {
                    String data = new String(payload);
                    if (data.equals("error")) {
                        throw new RuntimeException("service asked to throw an error");
                    }
                    else {
                        ServiceMessage.reply(nc, request, payload);
                    }
                }
            };

            // create the services
            Service service = new ServiceBuilder()
                .connection(nc)
                .name("JavaCrossClientValidator")
                .subject("jccv")
                .description("Java Cross Client Validator")
                .version("0.0.1")
                .schemaRequest("schema request string/url")
                .schemaResponse("schema response string/url")
                .statsDataHandlers(sds, sdd)
                .serviceMessageHandler(handler)
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
                // We expect this to timeout because we don't stop the service.
                // You can just stop the program also.
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class CcvData implements StatsData {
        String text;

        public CcvData(String text) {
            this.text = text;
        }

        @Override
        public String toJson() {
            return "\"" + text + "\"";
        }
    }

    static String randomText() {
        return Long.toHexString(System.currentTimeMillis()) + Long.toHexString(System.nanoTime());
    }
}

