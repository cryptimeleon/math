package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.serialization.Representation;

public class SHA512HashFunction extends AbstractSHAHashFunction {

    public SHA512HashFunction(Representation repr) {
        this();
    }

    public SHA512HashFunction() {
        super("SHA-512");
    }


    @Override
    public int getOutputLength() {
        return 512 / 8;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
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
        return true;
    }
}
