package de.upb.crypto.math.serialization;

public class StringRepresentation extends Representation {
    private static final long serialVersionUID = 4508386585732032537L;
    protected String s;

    /**
     * Instantiates an empty String representation.
     */
    public StringRepresentation() { //needed for Java serialization

    }

    public StringRepresentation(String s) {
        this.s = s;
    }

    public String get() {
        return s;
    }

    @Override
    public String toString() {
        return "\"" + s + "\"";
    }

    @Override
    public int hashCode() {
        return s == null ? 0 : s.hashCode();
    }

    @Override
    public boolean equals(Object obj) { //Eclipse generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StringRepresentation other = (StringRepresentation) obj;
        if (s == null) {
            if (other.s != null)
                return false;
        } else if (!s.equals(other.s))
            return false;
        return true;
    }
}
