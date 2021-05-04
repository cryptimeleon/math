package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMapImpl;
import org.cryptimeleon.math.structures.groups.mappings.IdentityIsomorphism;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.helpers.FiniteFieldTools;

import java.math.BigInteger;
import java.util.Objects;

/**
 * The implementation of our supersingular bilinear group.
 */
class SupersingularTateGroupImpl implements BilinearGroupImpl {

    @Represented
    private Integer securityParameter;
    // (ordered ascending)
    protected final int[] securityLimits = {48, 56, 64, 80, 112, 128, 160, 192, 256};
    // semantics: to achieve security securityLimits[i], you need an extension field of size minimumFieldSize[i]
    protected final int[] minimumFieldSize = {480, 640, 816, 1248, 2432, 3248, 5312, 7936, 15424};

    @Represented
    private SupersingularSourceGroupImpl g1;
    @Represented
    private SupersingularTargetGroupImpl gt;
    private SupersingularTatePairing pairing;
    @Represented
    private SupersingularSourceHash hashIntoG1;

    public SupersingularTateGroupImpl(int securityParameter) {
        if (securityParameter > securityLimits[securityLimits.length -1]) {
            throw new IllegalArgumentException("Cannot accommodate a security parameter of " + securityParameter
                    + ", please choose one of at most " + securityLimits[securityLimits.length - 1]);
        }
        this.securityParameter = securityParameter;
        // Select size of the extension field from the security parameter.
        // See: ECRYPT II Yearly Report on Algorithms and Keysizes (2011-2012)
        int logExtFieldSize = 0;
        for (int i = 0; i < securityLimits.length; i++) {
            if (securityParameter <= securityLimits[i]) {
                logExtFieldSize = minimumFieldSize[i];
                break;
            }
        }
        init(logExtFieldSize);
    }

    public SupersingularTateGroupImpl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
        pairing = new SupersingularTatePairing(g1, gt);
    }

    @Override
    public GroupImpl getG1() {
        return g1;
    }

    @Override
    public GroupImpl getG2() {
        return getG1();
    }

    @Override
    public GroupImpl getGT() {
        return gt;
    }

    @Override
    public BilinearMapImpl getBilinearMap() {
        return pairing;
    }

    @Override
    public IdentityIsomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        return new IdentityIsomorphism();
    }

    @Override
    public HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException {
        return hashIntoG1;
    }

    @Override
    public HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException {
        return getHashIntoG1();
    }

    @Override
    public HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The hash function into the target group is not implemented yet!");
    }

    @Override
    public Integer getSecurityLevel() {
        return securityParameter;
    }

    @Override
    public BilinearGroup.Type getPairingType() {
        return BilinearGroup.Type.TYPE_1;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupersingularTateGroupImpl that = (SupersingularTateGroupImpl) o;
        return g1.equals(that.g1) &&
                gt.equals(that.gt) &&
                pairing.equals(that.pairing) &&
                hashIntoG1.equals(that.hashIntoG1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g1);
    }

    protected void init(int logExtFieldSize) {
        BigInteger groupOrder;

        // Select a group order from the security parameter
        // Group size in bits is twice the security parameter to resist Pollard's Rho
        groupOrder = RandomGenerator.getRandomPrime(2 * securityParameter);

        // Find the field characteristic q and cofactor
        /*
         * We know that [number of curve points over F_q] = #E(F_q) = q+1.
         * We are going to work over the torsion point subgroup E(F_q)[groupOrder] of size groupOrder.
         * For this, we're looking for a cofactor such that #E(F_q) = #E(F_q)[groupOrder]*cofactor,
         * that is q+1 = groupOrder*cofactor
         * (and cofactor large enough for security. And characteristic is 3 mod 4).
         */
        BigInteger minCofactorSize = BigInteger.ONE.shiftLeft(logExtFieldSize / 2 - securityParameter * 2);
        BigInteger cofactor = minCofactorSize.shiftLeft(2);
        BigInteger characteristic; // the characteristic q of the two fields F_q and F_q^2.

        do { //choose cofactor, then check whether it fulfills our requirements
            // try small numbers as they tend to have many zeros in their bit representation
            cofactor = cofactor.add(BigInteger.ONE);
            characteristic = groupOrder.multiply(cofactor).subtract(BigInteger.ONE);
        } while (!characteristic.isProbablePrime(100)
                || 3 != characteristic.mod(BigInteger.valueOf(4)).intValue() // for easy square root computation in Zq
                || logExtFieldSize / 2 > characteristic.bitLength()
                || !groupOrder.gcd(cofactor).equals(BigInteger.ONE) //to ensure cofactor multiplication in E(F_q) does not consistently result in the neutral element.
        );

        //Instantiate the source group
        ExtensionField fieldOfDefinition = new ExtensionField(characteristic); //TODO maybe I can also just use Zp for this
        SupersingularSourceGroupImpl sourceGroup = new SupersingularSourceGroupImpl(groupOrder, cofactor,
                fieldOfDefinition);
        sourceGroup.setGenerator(sourceGroup.getGenerator());


        //Set up the target extension field F_q^2 by choosing a suitable irreducible polynomial x^2-qnr over F_q.
        FieldElement qnr = fieldOfDefinition.getElement(-1);
        while (FiniteFieldTools.isSquare(qnr)) {
            qnr = qnr.add(fieldOfDefinition.getElement(-1));
        }

        ExtensionField targetGroupField = new ExtensionField(qnr.neg(), 2);
        SupersingularTargetGroupImpl targetGroup = new SupersingularTargetGroupImpl(targetGroupField, groupOrder);

        this.g1 = sourceGroup;
        this.gt = targetGroup;
        this.pairing = new SupersingularTatePairing(sourceGroup, targetGroup);
        this.hashIntoG1 = new SupersingularSourceHash(sourceGroup);
    }
}
