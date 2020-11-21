package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;

import java.math.BigInteger;

import static de.upb.crypto.math.factory.BilinearGroup.Type.TYPE_3;

/**
 * Provider for Barreto-Naehrig based bilinear groups.
 * <p>
 * For background on BN curves see [1] Constructive and Computational Aspects of cryptrographic Pairings, Thesis of
 * Naehrig.
 */
public class BarretoNaehrigProvider implements BilinearGroupProvider {

    /**
     * {@link de.upb.crypto.math.factory.BilinearGroup} produced by this factory.
     */
    private BarretoNaehrigBilinearGroupImpl result;

    /**
     * Create an uninitialized instance of the factory.
     */
    public BarretoNaehrigProvider() {
    }

    /**
     * Characteristic p of BN curves are parameterized as follows:
     * <p>
     * p = p(u) = 36u^4 + 36u^3 + 24u^2 + 6u + 1
     */
    private static BigInteger p(BigInteger u) {
        return BigInteger.valueOf(36).multiply((u.pow(4).add(u.pow(3))))
                .add(BigInteger.valueOf(24).multiply(u.pow(2)))
                .add(BigInteger.valueOf(6).multiply(u)).add(BigInteger.ONE);
    }

    /**
     * Trace of frobenius of BN curves are parameterized as follows:
     * <p>
     * t = 6u^2 + 1
     */
    private static BigInteger t(BigInteger u) {
        return BigInteger.valueOf(6).multiply(u.pow(2)).add(BigInteger.ONE);
    }

    /**
     * Group order r of BN curves are parameterized as follows:
     * <p>
     * r = r(u) = 36u^4 + 36u^3 + 18u^2 + 6u + 1 = p - t
     */
    private static BigInteger groupOrder(BigInteger q, BigInteger t) {
        return q.add(BigInteger.ONE).subtract(t);
    }

    /**
     * Initialize factory from given generators of BN groups G1 and G2.
     *
     * @param P1 - Generator of G1
     * @param P2 - Generator of G2
     * @param gT - Target group
     */
    private void init(BarretoNaehrigGroup1ElementImpl P1, BarretoNaehrigGroup2ElementImpl P2, BarretoNaehrigTargetGroupImpl gT) {
        BarretoNaehrigGroup1Impl G1 = (BarretoNaehrigGroup1Impl) P1.getStructure();
        BarretoNaehrigGroup2Impl G2 = (BarretoNaehrigGroup2Impl) P2.getStructure();

        BarretoNaehrigTatePairing pairing = new BarretoNaehrigTatePairing(G1, G2, gT);
        BarretoNaehrigPointEncoding H1 = new BarretoNaehrigPointEncoding(G1);
        BarretoNaehrigPointEncoding H2 = new BarretoNaehrigPointEncoding(G2);
        this.result = new BarretoNaehrigBilinearGroupImpl(G1, G2, gT, H1, H2, pairing);
    }

    // public Representation compressParameters() {
    //
    // ObjectRepresentation r = new ObjectRepresentation();
    //
    // r.put("p", new BigIntegerRepresentation(this.params.getG1().getA6().getStructure().size()));
    //
    // r.put("alpha", this.params.getG2().getFieldOfDefinition().getConstant().getRepresentation()); //get constant
    // coefficieent with implicit assumption that extension is defined by binomial
    //
    // r.put("b", this.params.getG1().getA6().getRepresentation());
    //
    // r.put("beta", this.params.getGT().getFieldOfDefinition().getConstant().getRepresentation());
    //
    // r.put("n",new BigIntegerRepresentation(this.params.getG1().size()));
    //
    // r.put("P1", this.params.getG1().getGenerator().getRepresentation());
    // r.put("P2", this.params.getG2().getGenerator().getRepresentation());
    // r.put("bilinearMap", new StringRepresentation(this.params.getBilinearMap().getRepresentedTypeName()));
    // r.put("hashIntoG1", new RepresentableRepresentation(this.params.getHashIntoG1().getHashFunction()));
    // r.put("hashIntoG2", new RepresentableRepresentation(this.params.getHashIntoG2().getHashFunction()));
    //
    // return r;
    // }

    /**
     * Construct all required objects based on a given group size.
     * <p>
     * Basically implements Algorithm 2.1 of [1]
     */
    private void init(int groupBitSize) {
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
            FieldElement bInExt = extField1.lift(b);

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

    @Override
    public BilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements) {
        return new LazyBilinearGroup(provideBilinearGroupImpl(securityParameter, requirements));
    }

    /**
     * {@inheritDoc}
     *
     * @param securityParameter Discrete logarithm of the groups G1, G2, GT of the bilinear group provided.
     * @param requirements      Requirements the provided bilinear group need to fulfill.
     * @return a Barreto-Naehrig type 3 bilinear group with security parameter {@code securityParameter} fulfilling the
     * requirements given in {@code requirements}
     */
    @Override
    public BarretoNaehrigBilinearGroupImpl provideBilinearGroupImpl(int securityParameter, BilinearGroupRequirement requirements) {
        if (!checkRequirements(securityParameter, requirements))
            throw new UnsupportedOperationException("The requirements are not fulfilled by this Bilinear Group!");

        // TODO: Not accurate anymore, 256 bit group size only provides roughly 100 bits of security now
        //  according to https://tools.ietf.org/html/draft-irtf-cfrg-pairing-friendly-curves-06#section-3.2
        init(securityParameter * 2);

        return result;
    }

    /**
     * Provides a BN group {@link BarretoNaehrigBilinearGroupImpl} according to a given specification.
     * <p>
     * The returned group will for a given specification always be the same, see
     * {@link BarretoNaehrigParameterSpec#sfc256()}.
     *
     * @param spec group specification
     * @return BN group from given {@code spec}
     */
    public BarretoNaehrigBilinearGroupImpl provideBilinearGroupFromSpec(String spec) {
        if (spec.equals(ParamSpecs.SFC256))
            // security parameter is 128, ie bit length of group order is at least 256
            this.result = decompressParameters(BarretoNaehrigParameterSpec.sfc256());
        else
            throw new IllegalArgumentException("Cannot find given specification!");

        return result;
    }

    @Override
    public boolean checkRequirements(int securityParameter, BilinearGroupRequirement requirements) {
        return requirements.getNumPrimeFactorsOfSize() == 1
                && requirements.getType() == TYPE_3 && !requirements.isHashIntoGTNeeded();
    }

    /**
     * Reconstruct {@link BarretoNaehrigBilinearGroupImpl} from efficient representation.
     */
    public BarretoNaehrigBilinearGroupImpl decompressParameters(BarretoNaehrigParameterSpec spec) {
        return new BarretoNaehrigBilinearGroupImpl(spec);
    }

    public static class ParamSpecs {
        public static String SFC256 = "SFC-256";
    }
}
