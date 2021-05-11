package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.hash.HashFunction;
import org.cryptimeleon.math.hash.impl.VariableOutputLengthHashFunction;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.helpers.FiniteFieldTools;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Indifferentiable encoding, that is, an encoding that can be used to implement a hash function that can be used
 * to replace a ROM. More general than Boneh-Franklin admissible in that regard.
 */
class BarretoNaehrigHashToG1Impl implements HashIntoGroupImpl {

    @Represented
    public BarretoNaehrigGroup1Impl group1Impl;
    private Field baseField;
    private FieldElement b;

    private FieldElement c1;
    private FieldElement c2;

    private HashFunction hashFunction1;
    private HashFunction hashFunction2;

    public BarretoNaehrigHashToG1Impl(BarretoNaehrigGroup1Impl group1Impl) {
        this.group1Impl = group1Impl;
        init();
    }

    public BarretoNaehrigHashToG1Impl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
        init();
    }

    public void init() {
        b = group1Impl.getA6();
        baseField = group1Impl.getFieldOfDefinition();
        hashFunction1 = new VariableOutputLengthHashFunction((baseField.size().bitLength() - 1) / 8);
        // TODO: Need an independent hash function. Using the same two functions does not fulfill that.
        hashFunction2 = new VariableOutputLengthHashFunction((baseField.size().bitLength() - 1) / 8);
        // c1 = sqrt{-3}
        c1 = FiniteFieldTools.sqrt(baseField.getElement(-3));
        // c2 = (-1 + sqrt{-3})/2
        c2 = baseField.getOneElement().neg().add(c1).div(baseField.getElement(2));
    }

    @Override
    public GroupElementImpl hashIntoGroupImpl(byte[] x) {
        // Hashes given bytes to field element and applies SW encoding to it
        // Don't need any cofactor multiplication since the cofactor is always 1 for G1
        // To get indifferentiability (Fouque and Tibouchi, Section 5), we need to
        //  calculate f(h1(m)) + f(h2(m)),
        //  where f is the SW encoding and h1 and h2 are independent random oracles to F_q.
        byte[] h1 = hashFunction1.hash(x);
        BigInteger i1 = new BigInteger(h1);
        byte[] h2 = hashFunction2.hash(x);
        BigInteger i2 = new BigInteger(h2);

        GroupElementImpl result = SWEncode(baseField.getElement(i1)).op(SWEncode(baseField.getElement(i2)));
        System.out.println(result.pow(group1Impl.getCofactor()));
        return result;
    }

    /**
     * Takes in a field element t and applied the Shallue-van de Woestijne encoding to it to obtain
     * an element of the group G1.
     * <p>
     * This is the version designed to be secure against side-channel analysis and other physical attacks.
     * <p>
     * Implements algorithm 1 of P.-A. Fouque and M. Tibouchi, "Indifferentiable Hashing to Barreto–Naehrig Curves"
     * @param t the input
     */
    private GroupElementImpl SWEncode(FieldElement t) {
        if (t.isZero()) {
            // f(0) = ((-1 + \sqrt{-3})/2, \sqrt{1+b}) as suggested by Fouque and Tibouchi, Section 3
            return group1Impl.getElement(c2, FiniteFieldTools.sqrt(baseField.getOneElement().add(b)));
        }
        // w = sqrt{-3} * t/(1+b+t^2)
        FieldElement w = baseField.getOneElement().add(group1Impl.getA6().add(t.pow(2))).inv().mul(t).mul(c1);
        // x_1 = (-1 + sqrt{-3})/2 - tw
        FieldElement x1 = c2.add(t.mul(w).neg());
        // x_2 = -1 - x_1
        FieldElement x2 = baseField.getOneElement().neg().add(x1.neg());
        // x_3 = 1 + 1/w^2
        FieldElement x3 = baseField.getOneElement().add(baseField.getOneElement().div(w.square()));
        // r_1, r_2, r_3 <-$ F_q^*
        FieldElement r1 = baseField.getUniformlyRandomUnit();
        FieldElement r2 = baseField.getUniformlyRandomUnit();
        FieldElement r3 = baseField.getUniformlyRandomUnit();
        // alpha = chi_q(r_1^2 * (x_1^3 + b))
        int alpha = chi(r1.square().mul(x1.pow(3).add(b)));
        // beta = chi_q(r_2^2 * (x_2^3 + b))
        int beta = chi(r2.square().mul(x2.pow(3).add(b)));
        // i = [(alpha - 1) * beta mod 3] + 1
        Zn z3 = new Zn(BigInteger.valueOf(3));
        int i = z3.createZnElement(BigInteger.valueOf((alpha - 1) * beta))
                .getInteger()
                .add(BigInteger.ONE)
                .intValueExact();
        // y = chi_q(r3^2 * t) * sqrt{x_i^3 + b}
        FieldElement y;
        if (i == 1) {
            y = baseField.getElement(chi(r3.square().mul(t))).mul(FiniteFieldTools.sqrt(x1.pow(3).add(b)));
            return group1Impl.getElement(x1, y);
        } else if (i == 2) {
            y = baseField.getElement(chi(r3.square().mul(t))).mul(FiniteFieldTools.sqrt(x2.pow(3).add(b)));
            return group1Impl.getElement(x2, y);
        } else if (i == 3) {
            y = baseField.getElement(chi(r3.square().mul(t))).mul(FiniteFieldTools.sqrt(x3.pow(3).add(b)));
            return group1Impl.getElement(x3, y);
        }
        throw new IllegalStateException("Unreachable!");
    }

    /**
     * Implements character function chi_q of F_q^* extended with zero.
     */
    private int chi(FieldElement t) {
        if (t.equals(baseField.getZeroElement())) {
            return 0;
        }
        if (FiniteFieldTools.isSquare(t)) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarretoNaehrigHashToG1Impl that = (BarretoNaehrigHashToG1Impl) o;
        return Objects.equals(group1Impl, that.group1Impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group1Impl, hashFunction1, hashFunction2);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
