package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.hash.HashFunction;
import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.hash.impl.SHA512HashFunction;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMapImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.GroupHomomorphismImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

import java.math.BigInteger;
import java.util.Objects;


/**
 * The implementation of our Barreto-Naehrig bilinear group.
 * <p>
 * For BN curves, \(\mathbb{G}_1\), \(\mathbb{G}_2\), and \(\mathbb{G}_T\) are defined based on:
 * <ul>
 * <li> a finite field \(\mathbb{F}_p\) defined by a prime \(p\)
 * <li> a quadratic extension \(\mathbb{F}_2=\mathbb{F}_p(\alpha)\) defined by a quadratic non residue \(\alpha\)
 *      and the irreducible binomial \(x^2-\alpha\)
 * <li> a sextic extension \(\mathbb{F}_{12}=\mathbb{F}_2(\beta)\) defined by the irreducible binomial \(x^6-\beta\)
 * <li> an element \(b\) in \(\mathbb{F}_p\) with prime order in \(\mathbb{G}_1=E:y^2=x^3+b\)
 * <li> an element \(b'=b/\alpha\) in \(\mathbb{F}_2\)
 *      with \(\mathbb{G}_2\) a subgroup of size \(n\) in \(E':y^2=x^3+b'\)
 * </ul>
 */
public class BarretoNaehrigBilinearGroupImpl implements BilinearGroupImpl {

    @Represented
    private Integer securityParameter;
    // (ordered ascending)
    protected final int[] securityLimits = {100, 128};
    // semantics: to achieve security securityLimits[i], you need a group of bit size minimumGroupBitSize[i]
    protected final int[] minimumGroupBitSize = {256, 461};

    //Impl
    @Represented
    private BarretoNaehrigGroup1Impl g1impl;
    @Represented
    private BarretoNaehrigGroup2Impl g2impl;
    @Represented
    private BarretoNaehrigTargetGroupImpl gtimpl;
    @Represented
    private BarretoNaehrigPointEncoding hashIntoG1impl;
    @Represented
    private BarretoNaehrigPointEncoding hashIntoG2impl;

    private BarretoNaehrigTatePairing bilinearMapImpl;

    public BarretoNaehrigBilinearGroupImpl(int securityParameter) {
        if (securityParameter > securityLimits[securityLimits.length -1]) {
            throw new IllegalArgumentException("Cannot accommodate a security parameter of " + securityParameter
                    + ", please choose one of at most " + securityLimits[securityLimits.length - 1]);
        }
        this.securityParameter = securityParameter;
        int groupBitSize = 0;
        for (int i = 0; i < securityLimits.length; i++) {
            if (securityParameter <= securityLimits[i]) {
                groupBitSize = minimumGroupBitSize[i];
                break;
            }
        }
        init(groupBitSize);
    }

    public BarretoNaehrigBilinearGroupImpl(String spec) {
        this(BarretoNaehrigParameterSpec.getParameters(spec));
    }

    public BarretoNaehrigBilinearGroupImpl(BarretoNaehrigParameterSpec spec) {
        /* get size of groups */
        BigInteger n = spec.size;

        /* get characteristic of fields */
        BigInteger p = spec.characteristic;

        /* setup base field of size p */
        ExtensionField baseField = new ExtensionField(p);
        baseField.generatePrimitiveCubeRoot();

        /* get element a_6 of Weierstrass equation defining G1 */
        ExtensionFieldElement b = baseField.createElement(spec.b);

        /* setup group based on given parameters */
        g1impl = new BarretoNaehrigGroup1Impl(n, BigInteger.ONE, b);

        /* get elemnet defining first extension field of degree 2 */
        ExtensionFieldElement alpha = baseField.createElement(spec.alpha);

        ExtensionField F2 = new ExtensionField(alpha, 2);
        F2.generatePrimitiveCubeRoot();

        /* get element defining extension field of degree 6 over previous degree 2 extension */
        ExtensionFieldElement beta = F2.createElement(baseField.createElement(spec.beta0),
                baseField.createElement(spec.beta1));

        // #E(F_p)=n=p+1-t
        BigInteger t = p.add(BigInteger.ONE).subtract(n);

        /* construct G2 */
        g2impl = new BarretoNaehrigGroup2Impl(n, t, (ExtensionFieldElement) F2.createElement(b).div(beta.neg()));

        /* get generators of G1 and G2 */
        BarretoNaehrigGroup1ElementImpl P1 = g1impl.getElement(baseField.createElement(spec.x1),
                baseField.createElement(spec.y1));
        BarretoNaehrigGroup2ElementImpl P2 = g2impl.getElement(
                F2.createElement(baseField.createElement(spec.x20), baseField.createElement(spec.x21)),
                F2.createElement(baseField.createElement(spec.y20), baseField.createElement(spec.y21)));

        g1impl.setGenerator(P1);
        g2impl.setGenerator(P2);

        /* construct Gt of size n over degree 12 extension field implicitly given by beta */
        gtimpl = new BarretoNaehrigTargetGroupImpl(beta, n);

        /* construct hash functions to G1 and G2 based on given algorithm */
        HashFunction hash;
        switch (spec.hash) {
            case "SHA-256":
                hash = new SHA256HashFunction();
                break;
            case "SHA-512":
                hash = new SHA512HashFunction();
                break;
            default:
                throw new IllegalArgumentException("Unknown hash function " + spec.hash);
        }
        hashIntoG1impl = new BarretoNaehrigPointEncoding(hash, g1impl);
        hashIntoG2impl = new BarretoNaehrigPointEncoding(hash, g2impl);

        /* construct new bilinearMap based on its name */
        if ("Tate".equals(spec.pairing)) {
            bilinearMapImpl = new BarretoNaehrigTatePairing(g1impl, g2impl, gtimpl);
        } else {
            throw new IllegalArgumentException("Pairing of type " + spec.pairing + " not supported.");
        }
    }

    public BarretoNaehrigBilinearGroupImpl(Representation representation) {
        new ReprUtil(this).deserialize(representation);
        bilinearMapImpl = new BarretoNaehrigTatePairing(g1impl, g2impl, gtimpl);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupImpl getG1() {
        return g1impl;
    }

    @Override
    public GroupImpl getG2() {
        return g2impl;
    }

    @Override
    public GroupImpl getGT() {
        return gtimpl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarretoNaehrigBilinearGroupImpl that = (BarretoNaehrigBilinearGroupImpl) o;
        return g1impl.equals(that.g1impl) &&
                g2impl.equals(that.g2impl) &&
                gtimpl.equals(that.gtimpl) &&
                bilinearMapImpl.equals(that.bilinearMapImpl) &&
                hashIntoG1impl.equals(that.hashIntoG1impl) &&
                hashIntoG2impl.equals(that.hashIntoG2impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g1impl);
    }

    @Override
    public BilinearMapImpl getBilinearMap() {
        return bilinearMapImpl;
    }

    @Override
    public HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException {
        return this.hashIntoG1impl;
    }

    @Override
    public HashIntoGroupImpl getHashIntoG2() {
        return this.hashIntoG2impl;
    }

    /* We only know how to hash into GT for embedding degree 1.
     * Because for hashing, we require injective mapping from strings into group.
     * But if GT is subgroup of finite field, exponentiation with cofactor of group order is required.
     * This is not injective for embedding degrees > 1.
     *
     * (non-Javadoc)
     * @see org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup#getHomomorphismG2toG1()
     */
    @Override
    public HashIntoGroupImpl getHashIntoGT() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getSecurityLevel() {
        return securityParameter;
    }

    @Override
    public BilinearGroup.Type getPairingType() {
        return BilinearGroup.Type.TYPE_3;
    }

    /**
     * This functions throws an exception because for type 3 pairings there is no efficient homomorphism
     * {@code H : G2 -> G1}.
     */
    @Override
    public GroupHomomorphismImpl getHomomorphismG2toG1() {
        throw new UnsupportedOperationException("Map G2->G1 not available for BN Type 3 Pairings.");
    }

    @Override
    public String toString() {
        return "BarretoNaehrigBilinearGroupImpl{" +
                "g1impl=" + g1impl +
                ", g2impl=" + g2impl +
                ", gtimpl=" + gtimpl +
                ", bilinearMapImpl=" + bilinearMapImpl +
                ", hashIntoG1impl=" + hashIntoG1impl +
                ", hashIntoG2impl=" + hashIntoG2impl +
                '}';
    }

    /**
     * Construct all required objects based on a given group size.
     * <p>
     * Basically implements Algorithm 2.1 of [1]
     */
    protected void init(int groupBitSize) {
        BarretoNaehrigGroup1Impl g1;
        BarretoNaehrigGroup2Impl g2;
        BarretoNaehrigTargetGroupImpl gT;

        BarretoNaehrigGroup1ElementImpl P1;
        BarretoNaehrigGroup2ElementImpl P2;

        BigInteger u = BigInteger.ONE.shiftLeft(groupBitSize / 4 + 1);

        /*
         * assure that u=1 mod 2 and u=1 mod 3. u=1 mod 2 assures that p(u) = 3 mod 4 and hence -1 is QNR in F_p u=1 mod
         * 3 assures that p(u)== 4 mod 9 for efficient cube root computations
         */
        u = u.add(BigInteger.ONE).subtract(u.mod(BigInteger.valueOf(6)));

        BigInteger t;

        /* characerisitc of fields */
        BigInteger q;

        /* size of groups */
        BigInteger n;

        /* search for primes with required properties */
        do {
            t = t(u);
            q = p(u);
            n = groupOrder(q, t);

            if (q.isProbablePrime(10) && n.isProbablePrime(10)) {
                break;
            }

            /* next u with u=1 mod 6 */
            u = u.add(BigInteger.valueOf(6));
        } while (true);

        /* runtime check */
        if (q.mod(BigInteger.valueOf(4)).intValue() != 3) {
            throw new IllegalArgumentException();
        }

        if (q.mod(BigInteger.valueOf(9)).intValue() != 4) {
            throw new IllegalArgumentException();
        }

        ExtensionField baseField = new ExtensionField(q);

        /* parameter a_6 of base-field curve */
        ExtensionFieldElement b;

        /* search for generator G1 with y-coordinate 1 (efficiency) */
        ExtensionFieldElement y = baseField.getOneElement();
        do {
            do {
                // According to [1], Algorithm 2.3 search for b \in F_p( (F_p)^2 v (F_p)^3)
                do {
                    b = baseField.getUniformlyRandomElement();
                    if (b.pow(baseField.sizeUnitGroup().divide(BigInteger.valueOf(2))).isOne())
                        continue;

                    if (b.pow(baseField.sizeUnitGroup().divide(BigInteger.valueOf(3))).isOne())
                        continue;

                    break;
                } while (true);

                /* setup curve based on b */
                g1 = new BarretoNaehrigGroup1Impl(n, BigInteger.ONE, b);

                /* possibly y^2-b = 1-b is not a cubic residue in Z_q and we cannont find point with y-coordinate 1 */
                try {
                    P1 = (BarretoNaehrigGroup1ElementImpl) g1.mapToSubgroup(y, 0);
                    /* if we found Point with y-coordinate 1 we are done */
                    g1.setGenerator(P1);
                    break;
                } catch (IllegalArgumentException e) {
                    // intentionally left empty: next loop iteration
                }
            } while (true);

            /*
             * check that P1 is really generator of G1. The following check is correct because group is of prime order
             */
        } while (!P1.pow(g1.size()).isNeutralElement());

        /*
         * Now we use that q=3 mod 4 with -1 QNR to generate quadratic extension, the field of definition of the twist
         * by irreducible polynomial x^2+1
         */
        ExtensionField extField1 = new ExtensionField(baseField.getOneElement(), 2);

        /* search for element that is neither square nor cube in F_p^2 to define extension of degree 6 */
        ExtensionFieldElement v = extField1.createElement(baseField.getZeroElement(), baseField.getOneElement());
        do {
            v = v.add(extField1.getOneElement());

            if (v.pow(extField1.sizeUnitGroup().divide(BigInteger.valueOf(2))).isOne())
                continue;

            if (v.pow(extField1.sizeUnitGroup().divide(BigInteger.valueOf(3))).isOne())
                continue;

            /* map b to extension field */
            FieldElement bInExt = extField1.createElement(b);

            /*
             * iterate through degree 6 twists of E until we find correct order by iterating through powers coprime to
             * 6, see also Algorithm 2.3 of [1]
             */

            // always use i=1 twist to assure that v remains small
            for (int i : new int[]{1}) {
                v = (ExtensionFieldElement) v.pow(BigInteger.valueOf(i));

                /* b'=b/-v where v will define extension field as F_2/(x^6+v) */
                ExtensionFieldElement bTwist = (ExtensionFieldElement) bInExt.div(v).neg();

                /* setup twist */
                g2 = new BarretoNaehrigGroup2Impl(n, t, bTwist);

                /* search for generator of g2 */
                do {
                    /* uses cofactor multiplication to map to subgroup */
                    P2 = (BarretoNaehrigGroup2ElementImpl) g2.getUniformlyRandomElement();
                } while (P2.isNeutralElement());

                g2.setGenerator(P2);

                /*
                 * now check that we are really on twist with correct order by checking that in subgroup of correct
                 * order
                 */
                if (P2.pow(g2.size()).isNeutralElement()) {
                    /* tschakka, we are done */
                    gT = new BarretoNaehrigTargetGroupImpl(v, n);

                    init(P1, P2, gT);
                    return;
                }
            }
        } while (true);
    }

    /**
     * Initialize this bilinear group from given generators of BN groups G1 and G2.
     *
     * @param P1 generator of G1
     * @param P2 generator of G2
     * @param gT target group
     */
    private void init(BarretoNaehrigGroup1ElementImpl P1, BarretoNaehrigGroup2ElementImpl P2,
                                                 BarretoNaehrigTargetGroupImpl gT) {
        g1impl = (BarretoNaehrigGroup1Impl) P1.getStructure();
        g2impl = (BarretoNaehrigGroup2Impl) P2.getStructure();
        gtimpl = gT;

        bilinearMapImpl = new BarretoNaehrigTatePairing(g1impl, g2impl, gT);
        hashIntoG1impl = new BarretoNaehrigPointEncoding(g1impl);
        hashIntoG2impl = new BarretoNaehrigPointEncoding(g2impl);
    }

    /**
     * Characteristic \(p\) of BN curves is parameterized as follows:
     * <p>
     * \(p = p(u) = 36u^4 + 36u^3 + 24u^2 + 6u + 1\)
     */
    private static BigInteger p(BigInteger u) {
        return BigInteger.valueOf(36).multiply((u.pow(4).add(u.pow(3))))
                .add(BigInteger.valueOf(24).multiply(u.pow(2)))
                .add(BigInteger.valueOf(6).multiply(u)).add(BigInteger.ONE);
    }

    /**
     * Trace of Frobenius of BN curves is parameterized as follows:
     * <p>
     * \(t = 6u^2 + 1\)
     */
    private static BigInteger t(BigInteger u) {
        return BigInteger.valueOf(6).multiply(u.pow(2)).add(BigInteger.ONE);
    }

    /**
     * Group order \(r\) of BN curves is parameterized as follows:
     * <p>
     * \(r = r(u) = 36u^4 + 36u^3 + 18u^2 + 6u + 1 = p - t\)
     */
    private static BigInteger groupOrder(BigInteger q, BigInteger t) {
        return q.add(BigInteger.ONE).subtract(t);
    }
}
