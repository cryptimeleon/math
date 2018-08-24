package de.upb.crypto.math.serialization;

public class BooleanRepresentation extends Representation {
    private static final long serialVersionUID = 1L;
    protected boolean b;

    public BooleanRepresentation() { //needed for Java serialization
        b = false;
    }

    public BooleanRepresentation(boolean b) {
        this.b = b;
    }

    public boolean get() {
        return b;
    }

    @Override
    public String toString() {
        return String.valueOf(b);
    }

    @Override
    public int hashCode() {
        return b ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj) { //mostly Eclipse generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BooleanRepresentation other = (BooleanRepresentation) obj;
        return b == other.b;
    }
}
