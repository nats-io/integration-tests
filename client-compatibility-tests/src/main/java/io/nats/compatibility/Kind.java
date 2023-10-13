package io.nats.compatibility;

public enum Kind {
    COMMAND("command"),
    RESULT("result");

    public final String name;

    Kind(String name) {
        this.name = name;
    }

    public static Kind instance(String test) {
        for (Kind os : Kind.values()) {
            if (os.name.equals(test)) {
                return os;
            }
        }
        throw new IllegalArgumentException("Unknown Request Kind");
    }

    @Override
    public String toString() {
        return name;
    }
}
