package de.upb.crypto.math.serialization;

import java.util.Arrays;
import java.util.Base64;

public class ByteArrayRepresentation extends Representation {
    private static final long serialVersionUID = 5386237030968264355L;
    protected byte[] data;

    public ByteArrayRepresentation() { //needed for Java serialization

    }

    public ByteArrayRepresentation(byte[] data) {
        this.data = data;
    }

    public byte[] get() {
        return data;
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ByteArrayRepresentation other = (ByteArrayRepresentation) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }
}
