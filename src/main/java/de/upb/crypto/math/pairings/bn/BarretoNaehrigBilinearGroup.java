package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.serialization.util.RepresentationUtil;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearMap;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyHashIntoStructure;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.math.BigInteger;


/**
 * This class provides a minimal representation to reconstruct public parameters of pairings based schemes.
 * <p>
 * For BN curves, G1, G2, and GT are defined based on:
 * - a finite field F_p defined by a prime p
 * - a quadratic extension F2=F_p(alpha) defined by a QNR alpha and the irreducible binomial x^2-alpha
 * - a sextic extension F12=F_2(beta) defined by the irreducible binomial x^6-beta.
 * - an element b in F_p with prime order G1=E:y^2=x^3+b
 * - an element b'=b/alpha in F2 with G2 a subgroup of size n in E':y^2=x^3+b'
 *
 * @author Peter Guenther (peter.guenther@wincor-nixdorf.com)
 */
public class BarretoNaehrigBilinearGroup implements BilinearGroup {
    //Impl
    @Represented
    private BarretoNaehrigGroup1Impl g1impl;
    @Represented
    private BarretoNaehrigGroup2Impl g2impl;
    @Represented
    private BarretoNaehrigTargetGroupImpl gtimpl;
    private BarretoNaehrigTatePairing bilinearMapImpl;
    @Represented
    private BarretoNaehrigPointEncoding hashIntoG1impl;
    @Represented
    private BarretoNaehrigPointEncoding hashIntoG2impl;

    //User facing
    private Group g1, g2, gt;
    private BilinearMap bilinearMap;
    private HashIntoStructure hashIntoG1, hashIntoG2;



    public BarretoNaehrigBilinearGroup(BarretoNaehrigGroup1Impl g1impl, BarretoNaehrigGroup2Impl g2impl, BarretoNaehrigTargetGroupImpl gtimpl,
                                       BarretoNaehrigPointEncoding hashIntoG1impl, BarretoNaehrigPointEncoding hashIntoG2impl,
                                       BarretoNaehrigTatePairing bilinearMapImpl) {
        this.hashIntoG1impl = hashIntoG1impl;
        this.hashIntoG2impl = hashIntoG2impl;
        this.g1impl = g1impl;
        this.g2impl = g2impl;
        this.gtimpl = gtimpl;
        this.bilinearMapImpl = bilinearMapImpl;
        setPublicFacingStuff();
    }

    public BarretoNaehrigBilinearGroup(Representation representation) {
        new ReprUtil(this).deserialize(representation);
        bilinearMapImpl = new BarretoNaehrigTatePairing(g1impl, g2impl, gtimpl);
        setPublicFacingStuff();
    }

    private void setPublicFacingStuff() {
        g1 = new LazyGroup(g1impl);
        g2 = new LazyGroup(g2impl);
        gt = new LazyGroup(gtimpl);
        bilinearMap = new LazyBilinearMap(bilinearMapImpl, (LazyGroup) g1, (LazyGroup) g2, (LazyGroup) gt);
        hashIntoG1 = new LazyHashIntoStructure(hashIntoG1impl, (LazyGroup) g1);
        hashIntoG2 = new LazyHashIntoStructure(hashIntoG2impl, (LazyGroup) g2);
    }

    public BarretoNaehrigBilinearGroup(BarretoNaehrigParameterSpec spec) {
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
        g2impl = new BarretoNaehrigGroup2Impl(n, t, (ExtensionFieldElement) F2.lift(b).div(beta.neg()));

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


        setPublicFacingStuff();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public Group getG1() {
        return g1;
    }

    @Override
    public Group getG2() {
        return g2;
    }

    @Override
    public Group getGT() {
        return gt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BarretoNaehrigBilinearGroup that = (BarretoNaehrigBilinearGroup) o;

        if (hashIntoG1impl != null ? !hashIntoG1impl.equals(that.hashIntoG1impl) : that.hashIntoG1impl != null) return false;
        if (hashIntoG2impl != null ? !hashIntoG2impl.equals(that.hashIntoG2impl) : that.hashIntoG2impl != null) return false;
        return bilinearMap != null ? bilinearMap.equals(that.bilinearMap) : that.bilinearMap == null;
    }

    @Override
    public int hashCode() {
        int result = hashIntoG1impl != null ? hashIntoG1impl.hashCode() : 0;
        result = 31 * result + (hashIntoG2impl != null ? hashIntoG2impl.hashCode() : 0);
        result = 31 * result + (bilinearMap != null ? bilinearMap.hashCode() : 0);
        return result;
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilinearMap;
    }

    @Override
    public HashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        return this.hashIntoG1;
    }

    @Override
    public HashIntoStructure getHashIntoG2() {
        return this.hashIntoG2;
    }

    /* We only know how to hash into GT for embedding degree 1.
     * Because for hashing, we require injective mapping from strings into group.
     * But if GT is subgroup of finite field, exponentiation with cofactor of group order is required.
     * This is not injective for embedding degrees > 1.
     *
     * (non-Javadoc)
     * @see de.upb.crypto.math.factory.BilinearGroup#getHomomorphismG2toG1()
     */
    @Override
    public HashIntoStructure getHashIntoGT() {
        throw new UnsupportedOperationException();
    }


    /**
     * This functions throws an exception because for type 3 pairings there is not efficient map H:G2->G1.
     */
    @Override
    public GroupHomomorphism getHomomorphismG2toG1() {
        throw new UnsupportedOperationException("Map G2->G1 not available for BN Type 3 Pairings.");
    }

    @Override
    public HashIntoStructure getHashIntoZGroupExponent() {
        return new HashIntoZn(this.getG1().getZn());
    }

    @Override
    public String toString() {
        String s = "";

        s += "G1: " + this.getG1().toString() + "\n";
        s += "G2: " + this.getG2().toString() + "\n";
        s += "Gt: " + this.getGT().toString() + "\n";
        s += "P1: " + this.getG1().getGenerator().toString() + "\n";
        s += "P2: " + this.getG2().getGenerator().toString() + "\n";
        s += "gt: " + this.getGT().getGenerator().toString() + "\n";
        return s;

    }

}
