package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.serialization.Representation;

/**
 * Implementation of the SHA-256 hash function.
 */
public class SHA256HashFunction extends AbstractSHAHashFunction {

    public SHA256HashFunction(Representation repr) {
        this();
    }

    public SHA256HashFunction() {
        super("SHA-256");
    }

    @Override
    public int getOutputLength() {
        return 256 / 8;
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