package de.upb.crypto.math.structures.groups.elliptic.nopairing;

import de.upb.crypto.math.hash.HashFunction;
import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.random.RandomGenerator;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StringRepresentation;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.structures.groups.GroupElementImpl;
import de.upb.crypto.math.structures.groups.elliptic.AffineEllipticCurvePoint;
import de.upb.crypto.math.structures.groups.elliptic.EllipticCurvePoint;
import de.upb.crypto.math.structures.groups.elliptic.WeierstrassCurve;
import de.upb.crypto.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.structures.rings.Field;
import de.upb.crypto.math.structures.rings.FieldElement;
import de.upb.crypto.math.structures.rings.helpers.FiniteFieldTools;
import de.upb.crypto.math.structures.rings.zn.HashIntoZn;
import de.upb.crypto.math.structures.rings.zn.HashIntoZp;
import de.upb.crypto.math.structures.rings.zn.Zn;
import de.upb.crypto.math.structures.rings.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;

/**
 * An implementation of the secp256k1 curve.
 * <p>
 * The curve is defined in Weierstrass short form \(y^2 = x^3 + b\) over a field \(\mathbb{F}_p\).
 * Specific parameters are taken from <a href="https://www.secg.org/sec2-v2.pdf">here</a>.
 */
public class Secp256k1 implements WeierstrassCurve {
    /**
     * The prime used to instantiate the field \(\mathbb{F}_p\).
     */
    public static final BigInteger p =
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    /**
     * The number of elements on the curve.
     */
    public static final BigInteger n =
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

    /**
     * The field \(\mathbb{F}_p\) over which the curve is defined.
     */
    public static final Zp zp = new Zp(p);

    /**
     * Parameter for the weierstrass equation \(y^2 = x^3 + b\).
     */
    public static final Zp.ZpElement b = zp.valueOf(7);

    /**
     * x-coordinate of generator element.
     */
    public static final Zp.ZpElement generatorX =
            zp.valueOf(new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16));

    /**
     * y-coordinate of generator element.
     */
    public static final Zp.ZpElement generatorY =
            zp.valueOf(new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16));

    /**
     * Initialize the curve.
     */
    public Secp256k1() {}

    /**
     * Initialize the curve from a representation (not used since all parameters are fixed).
     * <p>
     * The representation is not used, as all parameters are fixed.
     * Hence, it can be any value.
     *
     * @param repr the representation to use for restoration. Not used
     */
    public Secp256k1(Representation repr) {this();}

    @Override
    public FieldElement getA6() {
        return b;
    }

    @Override
    public FieldElement getA4() {
        return zp.getZeroElement();
    }

    @Override
    public FieldElement getA3() {
        return zp.getZeroElement();
    }

    @Override
    public FieldElement getA2() {
        return zp.getZeroElement();
    }

    @Override
    public FieldElement getA1() {
        return zp.getZeroElement();
    }

    @Override
    public EllipticCurvePoint getElement(FieldElement x, FieldElement y) {
        return new AffineEllipticCurvePoint(this, x,y);
    }

    @Override
    public Field getFieldOfDefinition() {
        return zp;
    }

    @Override
    public GroupElementImpl getNeutralElement() {
        return new AffineEllipticCurvePoint(this);
    }

    @Override
    public GroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        return getGenerator().pow(RandomGenerator.getRandomNumber(n));
    }

    @Override
    public GroupElementImpl getElement(Representation repr) {
        return new AffineEllipticCurvePoint(this, repr);
    }

    @Override
    public GroupElementImpl getGenerator() throws UnsupportedOperationException {
        return getElement(generatorX, generatorY);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return n;
    }

    @Override
    public boolean hasPrimeSize() {
        return true;
    }

    @Override
    public double estimateCostInvPerOp() {
        return 346;
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.empty();
    }

    @Override
    public Representation getRepresentation() {
        return new StringRepresentation("secp256k1");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Secp256k1;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public static class HashIntoSecp256k1 implements HashIntoGroupImpl {
        private HashIntoZp hash;

        /**
         * Instantiate this hash function
         * @param hash a random oracle hash function to the curve's base field
         */
        public HashIntoSecp256k1(HashIntoZp hash) {
            this.hash = hash;
            if (!hash.getTargetStructure().equals(zp))
                throw new IllegalStateException("Hash must be into Z"+p);
        }

        public HashIntoSecp256k1() {
            this(new HashIntoZp(zp));
        }

        public HashIntoSecp256k1(Representation repr) {
            hash = (HashIntoZp) repr.repr().recreateRepresentable();
        }

        @Override
        public Representation getRepresentation() {
            return new RepresentableRepresentation(hash);
        }

        @Override
        public GroupElementImpl hashIntoGroupImpl(byte[] x) {
            Zp.ZpElement xCoordinate = this.hash.hash(x);

            while (true) {
                Zp.ZpElement ySquared = xCoordinate.pow(3).add(b);

                if (ySquared.isSquare()) //check if y is quadratic residue.
                    return new Secp256k1().getElement(xCoordinate, ySquared.sqrt());

                //If we were unlucky: try next x
                xCoordinate = xCoordinate.add(zp.getOneElement());
            }
        }
    }
}
