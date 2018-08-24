package de.upb.crypto.math.serialization;

import java.math.BigInteger;

public class BigIntegerRepresentation extends Representation {
    private static final long serialVersionUID = 1243544762886909652L;
    protected BigInteger n;

    public BigIntegerRepresentation() { //needed for Java serialization

    }

    public BigIntegerRepresentation(BigInteger n) {
        this.n = n;
    }

    public BigIntegerRepresentation(int n) {
        this.n = BigInteger.valueOf(n);
    }

    public BigInteger get() {
        return n;
    }

    public int getInt() {
        return n.intValue();
    }

    @Override
    public String toString() {
        return n.toString();
    }

    @Override
    public int hashCode() {
        return ((n == null) ? 0 : n.hashCode());
    }

    @Override
    public boolean equals(Object obj) { //Eclipse-generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BigIntegerRepresentation other = (BigIntegerRepresentation) obj;
        if (n == null) {
            if (other.n != null)
                return false;
        } else if (!n.equals(other.n))
            return false;
        return true;
    }
}
