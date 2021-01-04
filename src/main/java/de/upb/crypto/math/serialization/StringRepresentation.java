package de.upb.crypto.math.serialization;

/**
 * Representation of a {@code String}.
 */
public class StringRepresentation extends Representation {
    private static final long serialVersionUID = 4508386585732032537L;
    /**
     * The {@code String} represented by this representation.
     */
    protected String s;

    private StringRepresentation() { // needed for Java serialization

    }

    public StringRepresentation(String s) {
        this.s = s;
    }

    /**
     * Returns the represented {@code String}.
     */
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
    public boolean equals(Object obj) {
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
