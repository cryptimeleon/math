package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for storing a multi-exponentiation.
 *
 * @author Raphael Heitjohann
 */
class MultiExpContext {

    private List<GroupElement> bases;
    private List<BigInteger> exponents;

    MultiExpContext() {
        bases = new ArrayList<>();
        exponents = new ArrayList<>();
    }

    void addExponentiation(GroupElement base, BigInteger exponent, boolean inInversion) {
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            return;
        }
        BigInteger realExponent;
        if(inInversion) {
            realExponent = exponent.negate();
        } else {
            realExponent = exponent;
        }
        // Move negative exponent into basis if possible
        if (realExponent.compareTo(BigInteger.ZERO) < 0) {
            exponents.add(realExponent.negate());
            bases.add(base.inv());
        } else {
            exponents.add(realExponent);
            bases.add(base);
        }

    }

    boolean allBasesSameGroup() {
        if (isEmpty()) {
            return true;
        }
        Group group = bases.get(0).getStructure();
        for (GroupElement base : bases) {
            if (base.getStructure() != group) {
                return false;
            }
        }
        return true;
    }

    /**
     * Avoid precomputing elements whose exponent is one (so no exponentiation necessary)
     * by evaluating them and then removing them from the multi-exponentiation context.
     * @return Result of evaluating op of all constants on the left.
     */
    GroupElement evalAndRemoveLeftConstants() {
        // assume size of bases not 0
        int numLeftConstants = 0;
        while (numLeftConstants < exponents.size()
                && exponents.get(numLeftConstants).compareTo(BigInteger.ONE) == 0) {
            ++numLeftConstants;
        }
        GroupElement result = bases.get(0).getStructure().getNeutralElement();
        for (int i = 0; i < numLeftConstants; ++i) {
            result = result.op(bases.get(i));
        }
        // remove evaluated elements
        bases.subList(0, numLeftConstants).clear();
        exponents.subList(0, numLeftConstants).clear();
        return result;
    }

    /**
     * Same as for left but for right instead.
     * @return Result of evaluating op of all constants on the right.
     */
    GroupElement evalAndRemoveRightConstants() {
        // assume size of bases not 0
        int numRightConstants = 0;
        while (numRightConstants < exponents.size()
                && exponents
                .get(exponents.size()-1-numRightConstants).compareTo(BigInteger.ONE) == 0) {
            ++numRightConstants;
        }
        GroupElement result = bases.get(0).getStructure().getNeutralElement();
        for (int i = exponents.size()-numRightConstants; i < exponents.size(); ++i) {
            result = result.op(bases.get(i));
        }
        // remove evaluated elements
        bases.subList(exponents.size()-numRightConstants, exponents.size()).clear();
        exponents.subList(exponents.size()-numRightConstants, exponents.size()).clear();
        return result;
    }

    public boolean isEmpty() {
        return bases.size() == 0;
    }

    public List<GroupElement> getBases() {
        return bases;
    }

    public List<BigInteger> getExponents() {
        return exponents;
    }

    public String toString() {
        return "Bases: " + Arrays.toString(bases.toArray()) + "\n" +
                "Exponents: " + Arrays.toString(exponents.toArray());
    }
}
