package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.hash.impl.ByteArrayAccumulator;
import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * Hash function into G1 and G2.
 *
 * @author peter.guenther
 */
public class BarretoNaehrigPointEncoding implements HashIntoStructure {

    private BarretoNaehrigSourceGroup codomain;
    private HashFunction hashFunction;

    protected void setup(HashFunction digest, BarretoNaehrigSourceGroup codomain) {
        /*
         * check if codomain is large enough for injective encoding of hash values as elements. We could do something
         * less restrictive here (see Admissible Encoding in Bonhe Franklin IBE Paper)
         */
        BigInteger digestSize = BigInteger.ONE.shiftLeft(digest.getOutputLength() * 8);
        BigInteger groupSize = codomain.getFieldOfDefinition().size();
        if (digestSize.compareTo(groupSize) >= 0) {
            throw new IllegalArgumentException("Codomain to small for injective hashing hash of length "
                    + (digest.getOutputLength() * 8) + "bit.");
        }

        this.hashFunction = digest;
        this.codomain = codomain;
    }

    public BarretoNaehrigPointEncoding(HashFunction algorithm, BarretoNaehrigSourceGroup codomain) {
        setup(algorithm, codomain);
    }

    public BarretoNaehrigPointEncoding(BarretoNaehrigSourceGroup codomain) {
        BigInteger s = codomain.size();

        if (s.compareTo(BigInteger.ONE.shiftLeft(512)) > 0) {
            // s > 2^512
            setup(new SHA512HashFunction(), codomain);
        } else if (s.compareTo(BigInteger.ONE.shiftLeft(256)) > 0) {
            // 2^512 >= s > 2^256
            setup(new SHA256HashFunction(), codomain);
        } else {
            setup(new VariableOutputLengthHashFunction((codomain.getFieldOfDefinition().size().bitLength() - 1) / 8), codomain);
        }
    }

    public BarretoNaehrigPointEncoding(Representation r) {
        ObjectRepresentation or = (ObjectRepresentation) r;

        setup((HashFunction) or.get("hashFunction").repr().recreateRepresentable(), (BarretoNaehrigSourceGroup) or.get("codomain").repr().recreateRepresentable());
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation or = new ObjectRepresentation();

        or.put("hashFunction", new RepresentableRepresentation(hashFunction));
        or.put("codomain", new RepresentableRepresentation(codomain));
        return or;
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }

    @Override
    public BarretoNaehrigSourceGroupElement hashIntoStructure(byte[] x) {

        byte i = 0;
        do {
            ByteAccumulator accumulator = new ByteArrayAccumulator();
            accumulator.append(x);
            accumulator.append(new byte[]{i});
            byte[] h = hashFunction.hash(accumulator.extractBytes());
            BigInteger b = new BigInteger(h);
            /*
             * TODO: this is not an admissible encoding in the sense of Boneh Franclin because not every element in the codomain has the same number of pre-images. E.g. by setting sel to 0, we discard 2/3 of all points. Furthermore, we can have a.e.
             * in the sense of Boneh Franclin only if the codomain of the hash function is larger than the codomain of the encoding.
             */
            ExtensionFieldElement y = ((ExtensionField) codomain.getFieldOfDefinition()).createElement(b);
            try {
                /* this includes cofactor multiplication */
                BarretoNaehrigSourceGroupElement P = (BarretoNaehrigSourceGroupElement) this.codomain.mapToSubgroup(y, 0);
                return P;
            } catch (IllegalArgumentException e) {

            }
            i++;
        } while (i != 0);

        /* heuristically, the probability for failure is 2^-sizeof(i) */
        throw new InternalError("Was not able to hash " + x + ".\n This should not happen with reasonable probability.");

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codomain == null) ? 0 : codomain.hashCode());
        result = prime * result + ((hashFunction == null) ? 0 : hashFunction.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BarretoNaehrigPointEncoding)) {
            return false;
        }
        BarretoNaehrigPointEncoding other = (BarretoNaehrigPointEncoding) obj;
        if (codomain == null) {
            if (other.codomain != null) {
                return false;
            }
        } else if (!codomain.equals(other.codomain)) {
            return false;
        }
        if (hashFunction == null) {
            if (other.hashFunction != null) {
                return false;
            }
        } else if (!hashFunction.equals(other.hashFunction)) {
            return false;
        }
        return true;
    }

    @Override
    public Element hashIntoStructure(UniqueByteRepresentable ubr) {
        ByteAccumulator acc = new ByteArrayAccumulator();
        acc = ubr.updateAccumulator(acc);
        return this.hashIntoStructure(acc.extractBytes());
    }

}
