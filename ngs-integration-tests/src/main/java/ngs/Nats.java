package ngs;

import java.util.List;

public class Nats {
    private List<Import> imports;
    private OperatorLimitsV2 limits;
    private String type;
    private int version;

    public List<Import> getImports() {
        return imports;
    }

    public void setImports(List<Import> imports) {
        this.imports = imports;
    }

    public OperatorLimitsV2 getLimits() {
        return limits;
    }

    public void setLimits(OperatorLimitsV2 limits) {
        this.limits = limits;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
