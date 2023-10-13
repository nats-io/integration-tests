package io.nats.compatibility;

public enum Suite {
    OBJECT_STORE("object_store", "tests.object-store.");

    public final String id;
    public final String subjectPrefix;
    public final int prefixLen;

    Suite(String id, String subjectPrefix) {
        this.id = id;
        this.subjectPrefix = subjectPrefix;
        prefixLen = subjectPrefix.length();
    }

    public static Suite instance(String subject) {
        if (subject.startsWith(OBJECT_STORE.subjectPrefix)) {
            return OBJECT_STORE;
        }
        throw new IllegalArgumentException("Unknown Suite");
    }

    @Override
    public String toString() {
        return id;
    }
}
