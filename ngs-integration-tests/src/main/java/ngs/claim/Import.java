package ngs.claim;

public class Import {
    private String name;
    private String subject;
    private String account;
    private String token;
    private String to;
    private String localSubject;
    private String type;
    private boolean share;

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getAccount() {
        return account;
    }

    public String getToken() {
        return token;
    }

    public String getTo() {
        return to;
    }

    public String getLocalSubject() {
        return localSubject;
    }

    public String getType() {
        return type;
    }

    public boolean isShare() {
        return share;
    }
}
