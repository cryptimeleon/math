package de.upb.crypto.math.structures.rings.zn;

import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.hash.HashFunction;
import de.upb.crypto.math.hash.HashIntoStructure;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A hash function that maps to {@link Zn}.
 */
public class HashIntoZn implements HashIntoStructure {

    /**
     * The hash function.
     */
    @Represented
    protected HashFunction hashIntoZn;

    /**
     * The hash target structure.
     */
    @Represented
    protected Zn structure;

    public HashIntoZn(HashFunction hashFunction, Zn zn) {
        if (zn.getCharacteristic().bitLength() - 1 < 8)
            throw new IllegalArgumentException("HashIntoZn requires n to be at least 2^8, but given n is "
                    + zn.getCharacteristic());
        this.structure = zn;
        // Removing one bit to ensure injective mapping of hash values into Zn. It's _almost_ full domain hash then
        this.hashIntoZn = new VariableOutputLengthHashFunction(hashFunction,
                (zn.getCharacteristic().bitLength() - 1) / 8);
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
     * Reconstructs the hash function from its representation.
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

    @Override
    public Zn.ZnElement hashIntoStructure(String x) {
        return (Zn.ZnElement) HashIntoStructure.super.hashIntoStructure(x);
    }

    /**
     * Returns the ring Zn that this function hashes to.
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
        return Objects.equals(hashIntoZn, other.hashIntoZn)
                && Objects.equals(structure, other.structure);
    }
}
