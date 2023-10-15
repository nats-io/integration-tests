package io.nats.compatibility;

public enum Command {
    CREATE, PUT, GET;

    public static Command instance(String text) {
        for (Command c : Command.values()) {
            if (c.name().equalsIgnoreCase(text)) {
                return c;
            }
        }
        System.err.println("Unknown command: " + text);
        System.exit(-7);
        return null;
    }
}
