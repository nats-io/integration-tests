package io.nats.compatibility;

public enum Command {
    CREATE, PUT;

    public static Command instance(String text) {
        for (Command c : Command.values()) {
            if (c.name().equalsIgnoreCase(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + text);
    }
}
