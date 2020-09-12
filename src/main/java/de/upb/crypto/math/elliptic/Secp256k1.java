package de.upb.crypto.math.elliptic;

import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StringRepresentation;
import de.upb.crypto.math.structures.ec.AffineEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Parameters from https://www.secg.org/sec2-v2.pdf
 */
public class Secp256k1 implements WeierstrassCurve {
    public static final BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    public static final BigInteger n = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
    public static final Zp zp = new Zp(p);
    public static final Zp.ZpElement b = zp.valueOf(7);
    public static final Zp.ZpElement generatorX = zp.valueOf(new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16));
    public static final Zp.ZpElement generatorY = zp.valueOf(new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16));

    public Secp256k1() {}
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
        return getGenerator().pow(RandomGeneratorSupplier.getRnd().getRandomElement(n));
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
}
