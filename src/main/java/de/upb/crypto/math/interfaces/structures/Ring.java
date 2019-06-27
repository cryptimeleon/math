package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A Ring (with 1).
 * Operations are defined on its elements.
 */
public interface Ring extends Structure, RepresentationRestorer {
    /**
     * Returns an object representing the additive group of this ring
     */
    default RingAdditiveGroup asAdditiveGroup() {
        return new RingAdditiveGroup(this);
    }

    /**
     * Returns an object representing the (multiplicative) unit group of this ring
     */
    default RingUnitGroup asUnitGroup() {
        return new RingUnitGroup(this);
    }

    /**
     * Returns the number of units in this ring.
     *
     * @return size of the unit group or null if infinite
     * @throws UnsupportedOperationException if size is unknown / computationally expensive to compute
     */
    BigInteger sizeUnitGroup() throws UnsupportedOperationException;

    /**
     * Returns the additive neutral element of this ring
     */
    RingElement getZeroElement();

    /**
     * Returns the multiplicative neutral element of this ring
     */
    RingElement getOneElement();

    @Override
    RingElement getElement(Representation repr);

    @Override
    default RingElement recreateFromRepresentation(Type type, Representation repr) {
        if (!(type instanceof Class && RingElement.class.isAssignableFrom((Class) type)))
            throw new IllegalArgumentException("Ring cannot recreate type "+type.getTypeName()+" from representation");

        return getElement(repr);
    }

    @Override
    RingElement getUniformlyRandomElement() throws UnsupportedOperationException;

    /**
     * Generates an invertible element from this ring uniformly at random (using cryptographically strong RNG).
     * <p>
     * The default implementation generates random ring elements until it hits a unit.
     * Implementors should override if this is not feasible or if there is a better way!
     *
     * @throws UnsupportedOperationException
     */
    default RingElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        try {
            RingElement result;
            do {
                result = getUniformlyRandomElement();
            } while (!result.isUnit());
            return result;
        } catch (RuntimeException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * The characteristic of the ring,
     * i.e. the number n such that there is a ring homomorphism Z/nZ -> Ring
     *
     * @throws UnsupportedOperationException if unknown
     */
    BigInteger getCharacteristic() throws UnsupportedOperationException;

    /**
     * Maps the integer i into the ring, such that this map is a
     * homomorphism from the Z/getCharacteristic()*Z onto the Ring.
     *
     * @param i the integer to map
     * @return an element of the ring
     */
    RingElement getElement(BigInteger i);

    /**
     * Maps the integer i into the ring, such that this map is a
     * homomorphism from the integers onto the Ring.
     *
     * @param i the integer to map
     * @return an element of the ring
     */
    default RingElement getElement(long i) {
        return getElement(BigInteger.valueOf(i));
    }

    /**
     * This function executes the extended euclidean algorithm for the elements
     * a and b. In other words, it computes an array [x, y, gcd(a,b)]
     * such that ax+by=gcd(a,b).
     * The gcd(a,b) is an element such that: c divides a and c divides b => c divides gcd(a,b).
     * (the result is not unique - they may vary by a unit factor)
     * <p>
     * This algorithm only works on euclidean domains (i.e. RingElements must implement divideWithRemainder())
     *
     * @param a
     * @param b
     * @returns [x, y, gcd(a,b)]
     */
    public default RingElement[] extendedEuclideanAlgorithm(RingElement a, RingElement b) {
        //VariableExpression names and algorithm taken from http://anh.cs.luc.edu/331/notes/xgcd.pdf
        RingElement x = getZeroElement(), prevx = getOneElement();
        RingElement y = getOneElement(), prevy = getZeroElement();

        while (!b.isZero()) {
            RingElement q, r;
            RingElement[] divisionAndRemainder = a.divideWithRemainder(b);
            q = divisionAndRemainder[0];
            r = divisionAndRemainder[1];

            RingElement tmp = x;
            x = prevx.sub(q.mul(x));
            prevx = tmp;

            tmp = y;
            y = prevy.sub(q.mul(y));
            prevy = tmp;

            a = b;
            b = r;
        }

        return new RingElement[]{prevx, prevy, a};
    }

    /**
     * This function executes the extended euclidean algorithm of the passed elements.
     * In other words, it computes an array [x[0], x[1], ..., x[n-1], gcd(elements)]
     * such that gcd(elements)=elements[0]*x[0]+elements[1]*x[1]+...+elements[n-1]*x[n-1].
     * The gcd(elements) is an element such that: c divides all elements => c divides gcd(elements).
     * (the result is not unique - they may vary by a unit factor)
     * <p>
     * This algorithm only works on euclidean domains (i.e. RingElements must implement divideWithRemainder())
     *
     * @param a
     * @param b
     * @returns an array with coefficients and the gcd: [x[0], x[1], ..., x[n-1], gcd(elements)]
     */
    public default ArrayList<RingElement> extendedEuclideanAlgorithm(List<RingElement> elements) {
        if (elements == null || elements.size() == 0)
            return new ArrayList<>(Collections.singleton(getOneElement()));
        if (elements.size() == 1)
            return new ArrayList<>(Arrays.asList(getOneElement(), elements.get(0))); //1*elements[0] = elements[0] = gcd(elements)

        //Compute the extended euclidean algorithm for one element less
        ArrayList<RingElement> result = extendedEuclideanAlgorithm(elements.subList(0, elements.size() - 1));
        RingElement missingElement = elements.get(elements.size() - 1);

        //Compute gcd(gcd(result), missingElement)
        RingElement[] tmp = extendedEuclideanAlgorithm(result.get(result.size() - 1)/*gcd*/, missingElement);
        //Now tmp[0]*gcd(result) + tmp[1]*missingElement = gcd(elements) = tmp[2]

        //Update coefficients from the recursive result vector
        for (int i = 0; i < result.size() - 1; i++)
            result.set(i, result.get(i).mul(tmp[0]));

        //Set coefficient for the missingElement
        result.set(result.size() - 1, tmp[1]); //(overwriting the previous gcd on that position - we don't need that anymore)

        //Append the gcd of all elements
        result.add(tmp[2]);

        return result;
    }

    /**
     * Returns true if this ring is known to be commutative
     */
    boolean isCommutative();
}
