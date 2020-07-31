package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;
import de.upb.crypto.math.structures.helpers.FiniteFieldTools;

import java.math.BigInteger;

/**
 * The factory for constructing everything that is required for supersingular pairings.
 *
 * @author Peter Guenther (peter.guenther@uni-paderborn.de), Jan Bobolz
 */
public class SupersingularProvider implements BilinearGroupProvider {

    public SupersingularProvider() {
    }

    @Override
    public BilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements) {
        return new LazyBilinearGroup(provideBilinearGroupImpl(securityParameter, requirements));
    }

    /**
     * Sets up the factory and constructs the required structures.
     *
     * @param securityParameter The security parameter of the resulting groups,
     *                          i.e., the complexity of DLOG in G1, G2, GT.
     */
    @Override
    public SupersingularTateGroupImpl provideBilinearGroupImpl(int securityParameter, BilinearGroupRequirement requirements) {
        if (!checkRequirements(securityParameter, requirements))
            throw new UnsupportedOperationException("The requirements are not fulfilled by this Bilinear Group!");

        BigInteger groupOrder;
        //Zp baseField;

        RandomGenerator rng = RandomGeneratorSupplier.getRnd();

        //Select a group order from the security parameter
        groupOrder = rng.getRandomPrime(2 * securityParameter); //twice the security parameter to resist Pollard's Rho

        //Select size of the extension field from the security parameter.
        //See: ECRYPT II Yearly Report on Algorithms and Keysizes (2011-2012)
        int logExtFieldSize = 0;
        int[] securityLimits = {48, 56, 64, 80, 112, 128, 160, 192, 256}; //(ordered ascending)
        int[] minimumFieldSize = {480, 640, 816, 1248, 2432, 3248, 5312, 7936, 15424}; //semantics: to achieve security securityLimits[i], you need an extension field of size minimumFieldSize[i]
        for (int i = 0; i < securityLimits.length; i++) {
            if (securityParameter <= securityLimits[i]) {
                logExtFieldSize = minimumFieldSize[i];
                break;
            }
        }
        if (logExtFieldSize == 0)
            throw new IllegalArgumentException("Cannot accomodate a security parameter of " + securityParameter + ", please choose one of at most " + securityLimits[securityLimits.length - 1]);

        //Find the field characteristic q and cofactor.
        /*
         * We know that [number of curve points over F_q] = #E(F_q) = q+1.
         * We are going to work over the torsion point subgroup E(F_q)[groupOrder] of size groupOrder.
         * For this, we're looking for a cofactor such that #E(F_q) = #E(F_q)[groupOrder]*cofactor,
         * that is q+1 = groupOrder*cofactor
         * (and cofactor large enough for security. And characteristic is 3 mod 4).
         */
        BigInteger minCofactorSize = BigInteger.ONE.shiftLeft(logExtFieldSize / 2 - securityParameter * 2);
        BigInteger cofactor = minCofactorSize.shiftLeft(2);
        BigInteger characteristic; //the characteristic q of the two fields F_q and F_q^2.

        do { //choose cofactor, then check whether it fulfills our requirements.
            cofactor = cofactor.add(BigInteger.ONE); //try small numbers as they tend to have many zeros in their bit representation
            characteristic = groupOrder.multiply(cofactor).subtract(BigInteger.ONE);
        } while (!characteristic.isProbablePrime(100)
                || 3 != characteristic.mod(BigInteger.valueOf(4)).intValue() // for easy square root computation in Zq
                || logExtFieldSize / 2 > characteristic.bitLength()
                || !groupOrder.gcd(cofactor).equals(BigInteger.ONE) //to ensure cofactor multiplication in E(F_q) does not consistently result in the neutral element.
        );

        //Instantiate the source group
        ExtensionField fieldOfDefinition = new ExtensionField(characteristic); //TODO maybe I can also just use Zp for this
        SupersingularSourceGroupImpl sourceGroup = new SupersingularSourceGroupImpl(groupOrder, cofactor, fieldOfDefinition);
        sourceGroup.setGenerator(sourceGroup.getGenerator());


        //Set up the target extension field F_q^2 by choosing a suitable irreducible polynomial x^2-qnr over F_q.
        FieldElement qnr = fieldOfDefinition.getElement(-1);
        while (FiniteFieldTools.isSquare(qnr)) {
            qnr = qnr.add(fieldOfDefinition.getElement(-1));
        }

        ExtensionField targetGroupField = new ExtensionField(qnr.neg(), 2);
        SupersingularTargetGroupImpl targetGroup = new SupersingularTargetGroupImpl(targetGroupField, groupOrder);

        return new SupersingularTateGroupImpl(sourceGroup, targetGroup, new SupersingularTatePairing(sourceGroup, targetGroup), new SupersingularSourceHash(sourceGroup));
    }

    @Override
    public boolean checkRequirements(int securityParameter, BilinearGroupRequirement requirements) {
        return requirements.getType() == BilinearGroup.Type.TYPE_1 && !requirements.isHashIntoGTNeeded() && requirements.getNumPrimeFactorsOfSize() == 1;
    }
}
