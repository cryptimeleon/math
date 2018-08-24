package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;

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
    private BarretoNaehrigBilinearGroup params;

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
    private void init(BarretoNaehrigGroup1Element P1, BarretoNaehrigGroup2Element P2, BarretoNaehrigTargetGroup gT) {
        BarretoNaehrigGroup1 G1 = (BarretoNaehrigGroup1) P1.getStructure();
        BarretoNaehrigGroup2 G2 = (BarretoNaehrigGroup2) P2.getStructure();

        BarretoNaehrigTatePairing pairing = new BarretoNaehrigTatePairing(G1, G2, gT);
        BarretoNaehrigPointEncoding H1 = new BarretoNaehrigPointEncoding(G1);
        BarretoNaehrigPointEncoding H2 = new BarretoNaehrigPointEncoding(G2);
        this.params = new BarretoNaehrigBilinearGroup(H1, H2, pairing);
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
        BarretoNaehrigGroup1 g1;
        BarretoNaehrigGroup2 g2;
        BarretoNaehrigTargetGroup gT;

        BarretoNaehrigGroup1Element P1;
        BarretoNaehrigGroup2Element P2;

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
                g1 = new BarretoNaehrigGroup1(n, BigInteger.ONE, b);

                /* possibly y^2-b = 1-b is not a cubic residue in Z_q and we cannont find point with y-coordinate 1 */
                try {
                    P1 = (BarretoNaehrigGroup1Element) g1.mapToSubgroup(y, 0);
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
                g2 = new BarretoNaehrigGroup2(n, t, bTwist);

                /* search for generator of g2 */
                do {
                    /* uses cofactor multiplication to map to subgroup */
                    P2 = (BarretoNaehrigGroup2Element) g2.getUniformlyRandomElement();
                } while (P2.isNeutralElement());

                g2.setGenerator(P2);

                /*
                 * now check that we are really on twist with correct order by checking that in subgroup of correct
                 * order
                 */
                if (P2.pow(g2.size()).isNeutralElement()) {
                    /* chacka, we are done */
                    gT = new BarretoNaehrigTargetGroup(v, n);

                    init(P1, P2, gT);

                    return;
                }
            }
        } while (true);
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
    public BarretoNaehrigBilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements) {
        if (!checkRequirements(securityParameter, requirements))
            throw new UnsupportedOperationException("The requirements are not fulfilled by this Bilinear Group!");

        init(securityParameter * 2);

        return params;
    }

    /**
     * Provides a BN group {@link BarretoNaehrigBilinearGroup} according to a given specification.
     * <p>
     * The returned group will for a given specification always be the same, see
     * {@link BarretoNaehrigParameterSpec#sfc256()}.
     *
     * @param spec group specification
     * @return BN group from given {@code spec}
     */
    public BarretoNaehrigBilinearGroup provideBilinearGroupFromSpec(String spec) {
        if (spec.equals(ParamSpecs.SFC256))
            // security parameter is 128, ie bit length of group order is at least 256
            this.params = decompressParameters(BarretoNaehrigParameterSpec.sfc256());
        else
            throw new IllegalArgumentException("Cannot find given specification!");

        return params;
    }

    @Override
    public boolean checkRequirements(int securityParameter, BilinearGroupRequirement requirements) {
        return requirements.getCardinalityNumPrimeFactors() == 1
                && requirements.getType() == TYPE_3 && !requirements.isHashIntoGTNeeded();
    }

    /**
     * Reconstruct {@link BarretoNaehrigBilinearGroup} from efficient representation.
     */
    public BarretoNaehrigBilinearGroup decompressParameters(BarretoNaehrigParameterSpec spec) {
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
        BarretoNaehrigGroup1 g1 = new BarretoNaehrigGroup1(n, BigInteger.ONE, b);

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
        BarretoNaehrigGroup2 g2 = new BarretoNaehrigGroup2(n, t, (ExtensionFieldElement) F2.lift(b).div(beta.neg()));

        /* get generators of G1 and G2 */
        BarretoNaehrigGroup1Element P1 = g1.getElement(baseField.createElement(spec.x1),
                baseField.createElement(spec.y1));
        BarretoNaehrigGroup2Element P2 = g2.getElement(
                F2.createElement(baseField.createElement(spec.x20), baseField.createElement(spec.x21)),
                F2.createElement(baseField.createElement(spec.y20), baseField.createElement(spec.y21)));

        g1.setGenerator(P1);
        g2.setGenerator(P2);

        /* construct Gt of size n over degree 12 extension field implicitly given by beta */
        BarretoNaehrigTargetGroup gt = new BarretoNaehrigTargetGroup(beta, n);

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
        BarretoNaehrigPointEncoding H1 = new BarretoNaehrigPointEncoding(hash, g1);
        BarretoNaehrigPointEncoding H2 = new BarretoNaehrigPointEncoding(hash, g2);

        /* construct new bilinearMap based on its name */
        BarretoNaehrigTatePairing pairing;
        if ("Tate".equals(spec.pairing)) {
            pairing = new BarretoNaehrigTatePairing(g1, g2, gt);
        } else {
            throw new IllegalArgumentException("Pairing of type " + spec.pairing + " not supported.");
        }

        return new BarretoNaehrigBilinearGroup(H1, H2, pairing);
    }

    public static class ParamSpecs {
        public static String SFC256 = "SFC-256";
    }
}
