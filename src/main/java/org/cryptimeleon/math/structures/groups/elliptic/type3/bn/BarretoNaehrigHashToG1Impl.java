package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.HashFunction;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.hash.impl.VariableOutputLengthHashFunction;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.helpers.FiniteFieldTools;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Indifferentiable encoding, that is, an encoding that can be used to implement a hash function that can be used
 * to replace a ROM. More general than Boneh-Franklin admissible in that regard.
 */
public class BarretoNaehrigHashToG1Impl implements HashIntoGroupImpl {

    private final BarretoNaehrigGroup1Impl group1Impl;
    private final Field baseField;
    private final FieldElement b;

    private final FieldElement c1;
    private final FieldElement c2;

    private final HashFunction hashFunction;

    public BarretoNaehrigHashToG1Impl(BarretoNaehrigGroup1Impl group1Impl) {
        this.group1Impl = group1Impl;
        b = group1Impl.getA6();
        baseField = group1Impl.getFieldOfDefinition();
        hashFunction = new VariableOutputLengthHashFunction((baseField.size().bitLength() - 1) / 8);
        // c1 = sqrt{-3}
        c1 = FiniteFieldTools.sqrt(baseField.getElement(-3));
        // c2 = (-1 + sqrt{-3})/2
        c2 = baseField.getOneElement().neg().add(c1).div(baseField.getElement(2));
    }

    @Override
    public GroupElementImpl hashIntoGroupImpl(byte[] x) {
        // Hashes given bytes to field element and applies SW encoding to it
        ByteAccumulator accumulator = new ByteArrayAccumulator();
        accumulator.append(x);
        byte[] h = hashFunction.hash(accumulator.extractBytes());
        BigInteger b = new BigInteger(h);
        return SWEncode(baseField.getElement(b));
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
    public Representation getRepresentation() {
        return null;
    }
}
