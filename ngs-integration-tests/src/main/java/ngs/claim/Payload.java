package ngs.claim;

public class Payload {
    private long iat;
    private String iss;
    private String jti;
    private String name;
    private NatsClaims nats;
    private String sub;

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NatsClaims getNats() {
        return nats;
    }

    public void setNats(NatsClaims natsClaims) {
        this.nats = natsClaims;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}
