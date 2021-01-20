package de.upb.crypto.math.structures.groups.elliptic.type3.bn;

import de.upb.crypto.math.hash.impl.ByteArrayAccumulator;
import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.hash.ByteAccumulator;
import de.upb.crypto.math.hash.HashFunction;
import de.upb.crypto.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.structures.rings.extfield.ExtensionField;
import de.upb.crypto.math.structures.rings.extfield.ExtensionFieldElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Hash function into G1 and G2.
 */
public class BarretoNaehrigPointEncoding implements HashIntoGroupImpl {

    @Represented
    private BarretoNaehrigSourceGroupImpl codomain;
    @Represented
    private HashFunction hashFunction;

    protected void check() {
        /*
         * check if codomain is large enough for injective encoding of hash values as elements. We could do something
         * less restrictive here (see Admissible Encoding in Bonhe Franklin IBE Paper)
         */
        BigInteger digestSize = BigInteger.ONE.shiftLeft(hashFunction.getOutputLength() * 8);
        BigInteger groupSize = codomain.getFieldOfDefinition().size();
        if (digestSize.compareTo(groupSize) >= 0) {
            throw new IllegalArgumentException("Codomain to small for injective hashing hash of length "
                    + (hashFunction.getOutputLength() * 8) + " bit.");
        }
    }

    public BarretoNaehrigPointEncoding(HashFunction hashFunction, BarretoNaehrigSourceGroupImpl codomain) {
        this.codomain = codomain;
        this.hashFunction = hashFunction;
        check();
    }

    public BarretoNaehrigPointEncoding(BarretoNaehrigSourceGroupImpl codomain) {
        BigInteger s = codomain.size();
        this.codomain = codomain;

        if (s.compareTo(BigInteger.ONE.shiftLeft(512)) > 0) {
            // s > 2^512
            hashFunction = new SHA512HashFunction();
        } else if (s.compareTo(BigInteger.ONE.shiftLeft(256)) > 0) {
            // 2^512 >= s > 2^256
            hashFunction = new SHA256HashFunction();
        } else {
            hashFunction = new VariableOutputLengthHashFunction((codomain.getFieldOfDefinition().size().bitLength() - 1) / 8);
        }

        check();
    }

    public BarretoNaehrigPointEncoding(Representation r) {
        new ReprUtil(this).deserialize(r);
        check();
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }

    @Override
    public BarretoNaehrigSourceGroupElementImpl hashIntoGroupImpl(byte[] x) {

        byte i = 0;
        do {
            ByteAccumulator accumulator = new ByteArrayAccumulator();
            accumulator.append(x);
            accumulator.append(new byte[]{i});
            byte[] h = hashFunction.hash(accumulator.extractBytes());
            BigInteger b = new BigInteger(h);
            /*
             * TODO: this is not an admissible encoding in the sense of Boneh Franklin because not every element in the codomain has the same number of pre-images. E.g. by setting sel to 0, we discard 2/3 of all points. Furthermore, we can have a.e.
             * in the sense of Boneh Franklin only if the codomain of the hash function is larger than the codomain of the encoding.
             */
            ExtensionFieldElement y = ((ExtensionField) codomain.getFieldOfDefinition()).createElement(b);
            try {
                /* this includes cofactor multiplication */
                return (BarretoNaehrigSourceGroupElementImpl) this.codomain.mapToSubgroup(y, 0);
            } catch (IllegalArgumentException e) {

            }
            i++;
        } while (i != 0);

        /* heuristically, the probability for failure is 2^-sizeof(i) */
        throw new InternalError("Was not able to hash " + Arrays.toString(x) + ".\n This should not happen with reasonable probability.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarretoNaehrigPointEncoding that = (BarretoNaehrigPointEncoding) o;
        return codomain.equals(that.codomain) &&
                hashFunction.equals(that.hashFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codomain, hashFunction);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
