package org.cryptimeleon.math.structures.rings.polynomial;

import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zp;
import org.cryptimeleon.math.structures.rings.zn.Zp.ZpElement;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains methods for calculating lagrange polynomials as well as
 * interpolation in the exponent of group elements.
 * <p>
 * For interpolating a polynomial given a set of known evaluations, see {@link PolynomialRing#getPoly(Map)}.
 */
public class LagrangeUtils {

    /**
     * Given a set of group elements with a common basis whose exponents implicitly define a polynomial,
     * returns that common basis to the power of that polynomial evaluated at point {@code newPoint}.
     * <p>
     * Specifically, let \(g_{a_1}\) to \(g_{a_m}\) be the group elements contained in map {@code knownPoints}
     * where \(a_i\) is the {@code BigInteger} mapped to \(g_{a_i}\).
     * Given some generator \(g\) of the group, \(g_{a_1}\) to \(g_{a_m}\) can be written as
     * \(g_{a_1} = g^{p(a_1)}, ..., g_{a_m} = g^{p(a_m)}\) for some implicitly defined polynomial
     * \(p\) of degree \(m-1\).
     * <p>
     * Let \(x\) be {@code newPoint} and let \(\ell_{a_i}\) be the i-th Lagrange polynomial.
     * Then this method returns \(g^{p(x)}\) using Lagrange interpolation by computing
     * \(g^{p(x)} = \prod_{a_i}{g_{a_i}^{\ell_{a_i}(x)}}\).
     *
     * @param givenElems maps \(a_i\) to \(g^{p(a_i)}\) for some implicit polynomial \(p\)
     * @param newCoord the x-coordinate to return \(g\) to the power \(p(x)\) for
     * @return the result of interpolating in the exponent, \(g^{p(x)}\)
     */
    public static GroupElement interpolateInTheExponent(Map<BigInteger, GroupElement> givenElems, BigInteger newCoord) {
        if (givenElems.isEmpty()) {
            throw new IllegalArgumentException("Set of known evaluations is empty");
        }
        // Given g_1 = g^p(1), ..., g_m = g^p(m), interpolate g^p(x) by calculating
        // g^p(x) = g_1^l_1(x) op g_2^l_2(x) op ... op g_m^l_m(x)

        // Calculate g_1^l_1(x)
        Set<BigInteger> xcoordinates = givenElems.keySet();
        Iterator<Map.Entry<BigInteger, GroupElement>> iterator = givenElems.entrySet().iterator();
        Map.Entry<BigInteger, GroupElement> firstEntry = iterator.next();
        Zp zp = (Zp) firstEntry.getValue().getStructure().getZn();
        GroupElement result = firstEntry.getValue().pow(
                computeCoefficient(firstEntry.getKey(), xcoordinates, newCoord, zp)
        );
        // Now do interpolation for rest of elements
        while (iterator.hasNext()) {
            Map.Entry<BigInteger, GroupElement> nextEntry = iterator.next();
            result = result.op(nextEntry.getValue().pow(
                    computeCoefficient(nextEntry.getKey(), xcoordinates, newCoord, zp)
            ));
        }
        return result;
    }

    /**
     * Compute the Lagrange coefficient \(\ell_j(x)\).
     *
     * @param i j-th x coordinate
     * @param S set of x coordinates
     * @param x x coordinate to evaluate the lagrange basis polynomial at
     * @return the lagrange basis polynomial evaluated at coordinate {@code x}
     */
    public static ZpElement computeCoefficient(ZpElement i, Set<ZpElement> S,
                                                  ZpElement x) {
        ZpElement result = i.getStructure().getOneElement();
        for (ZpElement j : S) {
            if (j.equals(i)) {
                continue;
            }
            ZpElement numerator = x.sub(j);
            ZpElement denominator = i.sub(j);

            result = result.mul(numerator.div(denominator));
        }
        return result;
    }

    /**
     * Compute the Lagrange coefficient \(\ell_j(x)\) over the specified field.
     *
     * @param i j-th x coordinate
     * @param S set of x coordinates
     * @param x x coordinate to evaluate the lagrange basis polynomial at
     * @param field the field to do the computation over
     * @return the lagrange basis polynomial evaluated at coordinate {@code x} in the given field
     */
    public static BigInteger computeCoefficient(BigInteger i, Set<BigInteger> S, BigInteger x,
                                                Zp field) {
        return computeCoefficient(
                field.createZnElement(i),
                S.parallelStream().map(field::createZnElement).collect(Collectors.toSet()),
                field.createZnElement(x)
        ).asInteger();
    }
}
