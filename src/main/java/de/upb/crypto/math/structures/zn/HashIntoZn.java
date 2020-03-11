package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.math.BigInteger;

/**
 * A hash function that maps into Zn.
 */
public class HashIntoZn implements HashIntoStructure {

    @Represented
    protected HashFunction hashIntoZn;

    /**
     * The hash target
     */
    @Represented
    protected Zn structure;

    public HashIntoZn(HashFunction hashFunction, Zn zn) {
        if (zn.getCharacteristic().bitLength() - 1 < 8)
            throw new IllegalArgumentException("HashIntoZn requires n to be at least 2^8, but given n is " + zn.getCharacteristic());
        this.structure = zn;
        this.hashIntoZn = new VariableOutputLengthHashFunction(hashFunction, (zn.getCharacteristic().bitLength() - 1) / 8); //removing one bit to ensure injective mapping of hash values into Zn. It's _almost_ full domain hash then
    }

    public HashIntoZn(HashFunction hashFunction, BigInteger n) {
        this(hashFunction, new Zn(n));
    }

    public HashIntoZn(BigInteger n) {
        this(new Zn(n));
    }

    public HashIntoZn(Zn zn) {
        this(new SHA256HashFunction(), zn);
    }


    /**
     * Reconstructs the hash function from its representation
     */
    public HashIntoZn(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public Zn.ZnElement hashIntoStructure(byte[] x) {
        byte[] hash = hashIntoZn.hash(x);
        return structure.injectiveValueOf(hash);
    }

    /**
     * Returns the ring Zn that this function hashes into
     */
    public Zn getTargetStructure() {
        return structure;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hashIntoZn == null) ? 0 : hashIntoZn.hashCode());
        result = prime * result + ((structure == null) ? 0 : structure.hashCode());
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
        HashIntoZn other = (HashIntoZn) obj;
        if (hashIntoZn == null) {
            if (other.hashIntoZn != null)
                return false;
        } else if (!hashIntoZn.equals(other.hashIntoZn))
            return false;
        if (structure == null) {
            if (other.structure != null)
                return false;
        } else if (!structure.equals(other.structure))
            return false;
        return true;
    }


}
