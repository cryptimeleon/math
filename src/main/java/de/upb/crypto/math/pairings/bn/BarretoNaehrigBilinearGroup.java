package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.util.RepresentationUtil;
import de.upb.crypto.math.structures.zn.HashIntoZn;


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
    /**
     * The public paramter groupG, which specifies the group of g
     */
    private static final String[] standaloneRepresentables = {"hashIntoG1", "hashIntoG2", "bilinearMap"};

    private BarretoNaehrigPointEncoding hashIntoG1;
    private BarretoNaehrigPointEncoding hashIntoG2;
    private BilinearMap bilinearMap;

    public BarretoNaehrigBilinearGroup(BarretoNaehrigPointEncoding hashIntoG1, BarretoNaehrigPointEncoding hashIntoG2,
                                       BarretoNaehrigTatePairing bilinearMap) {
        this.hashIntoG1 = hashIntoG1;
        this.hashIntoG2 = hashIntoG2;
        this.bilinearMap = bilinearMap;
    }

    public BarretoNaehrigBilinearGroup(Representation representation) {
        for (String representable : standaloneRepresentables) {
            RepresentationUtil.restoreStandaloneRepresentable(this, representation, representable);
        }
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation toReturn = new ObjectRepresentation();

        for (String representable : standaloneRepresentables) {
            RepresentationUtil.putElement(this, toReturn, representable);
        }

        return toReturn;
    }

    @Override
    public BarretoNaehrigGroup1 getG1() {
        return (BarretoNaehrigGroup1) this.bilinearMap.getG1();
    }

    @Override
    public BarretoNaehrigGroup2 getG2() {
        return (BarretoNaehrigGroup2) this.bilinearMap.getG2();
    }

    @Override
    public BarretoNaehrigTargetGroup getGT() {
        return (BarretoNaehrigTargetGroup) this.bilinearMap.getGT();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BarretoNaehrigBilinearGroup that = (BarretoNaehrigBilinearGroup) o;

        if (hashIntoG1 != null ? !hashIntoG1.equals(that.hashIntoG1) : that.hashIntoG1 != null) return false;
        if (hashIntoG2 != null ? !hashIntoG2.equals(that.hashIntoG2) : that.hashIntoG2 != null) return false;
        return bilinearMap != null ? bilinearMap.equals(that.bilinearMap) : that.bilinearMap == null;
    }

    @Override
    public int hashCode() {
        int result = hashIntoG1 != null ? hashIntoG1.hashCode() : 0;
        result = 31 * result + (hashIntoG2 != null ? hashIntoG2.hashCode() : 0);
        result = 31 * result + (bilinearMap != null ? bilinearMap.hashCode() : 0);
        return result;
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilinearMap;
    }

    public void setBilinearMap(BilinearMap bilinearMap) {
        this.bilinearMap = bilinearMap;
    }

    @Override
    public BarretoNaehrigPointEncoding getHashIntoG1() throws UnsupportedOperationException {
        return this.hashIntoG1;
    }

    /**
     * This functions throws an exception because for type 3 pairings there is not efficient map H:G2->G1.
     */
    @Override
    public GroupHomomorphism getHomomorphismG2toG1() {
        throw new UnsupportedOperationException("Map G2->G1 not available for BN Type 3 Pairings.");
    }

    public void setHashIntoG1(BarretoNaehrigPointEncoding h) {
        this.hashIntoG1 = h;
    }

    @Override
    public BarretoNaehrigPointEncoding getHashIntoG2() {
        return this.hashIntoG2;
    }

    public void setHashIntoG2(BarretoNaehrigPointEncoding h) {
        this.hashIntoG2 = h;
    }


    /* We only now how to hash into GT for embedding degree 1.
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

    @Override
    public HashIntoStructure getHashIntoZGroupExponent() {
        return new HashIntoZn(this.getG1().size());
    }

    public BarretoNaehrigGroup1Element getG1Generator() {
        return (BarretoNaehrigGroup1Element) getG1().getGenerator();
    }

    public BarretoNaehrigGroup2Element getG2Generator() {
        return (BarretoNaehrigGroup2Element) getG2().getGenerator();
    }

    @Override
    public String toString() {
        String s = "";

        s += "G1: " + this.getG1().toString() + "\n";
        s += "G2: " + this.getG2().toString() + "\n";
        s += "Gt: " + this.getGT().toString() + "\n";
        s += "P1: " + this.getG1Generator().toString() + "\n";
        s += "P2: " + this.getG2Generator().toString() + "\n";
        s += "gt: " + this.getGT().getGenerator() + "\n";
        return s;

    }

}
