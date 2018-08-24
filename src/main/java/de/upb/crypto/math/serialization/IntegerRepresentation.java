package de.upb.crypto.math.serialization;

public class IntegerRepresentation extends Representation {
    private static final long serialVersionUID = 2L;
    protected long i;

    public IntegerRepresentation() { //needed for Java serialization
        i = 0;
    }

    public IntegerRepresentation(int b) {
        this.i = b;
    }

    public IntegerRepresentation(long b) {
        this.i = b;
    }

    public long get() {
        return i;
    }

    @Override
    public String toString() {
        return String.valueOf(i);
    }

    @Override
    public int hashCode() {
        return (int) i;
    }

    @Override
    public boolean equals(Object obj) { //mostly Eclipse generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntegerRepresentation other = (IntegerRepresentation) obj;
        return i == other.i;
    }
}
