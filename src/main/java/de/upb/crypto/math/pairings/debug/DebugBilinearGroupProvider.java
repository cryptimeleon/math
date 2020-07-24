package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;

import java.math.BigInteger;
import java.util.ArrayList;

public class DebugBilinearGroupProvider implements BilinearGroupProvider {
    /**
     * Creates a new Debug group whose prime factors have bit size {@code securityParameter}
     *
     * @param type              type of the pairing (type 1: G1 = G2; type 2: G1 != G2 and there is a nondegenerate homomorphism G2 -> G1; type 3: G1 != G2 and there are no efficiently computable injective homomorphisms between G1 and G2
     * @param securityParameter bit size of the prime factors
     * @param numPrimeFactors   number of prime factors
     */
    public DebugBilinearGroup provideBilinearGroup(int securityParameter, BilinearGroup.Type type, int numPrimeFactors, boolean wantHash) {
        if (securityParameter < 2)
            throw new IllegalArgumentException("Cannot create debug pairing of bit size " + securityParameter);

        ArrayList<BigInteger> primeFactors = new ArrayList<>();
        RandomGenerator rnd = RandomGeneratorSupplier.getRnd();
        for (int i = 0; i < numPrimeFactors; i++)
            primeFactors.add(rnd.getRandomPrime(securityParameter));

        return new DebugBilinearGroup(type, primeFactors.stream().reduce(BigInteger.ONE, BigInteger::multiply), wantHash);
    }

    @Override
    public BilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements) {
        return provideBilinearGroup(securityParameter, requirements.getType(), requirements.getNumPrimeFactorsOfSize(), requirements.isHashIntoG1Needed() || requirements.isHashIntoG2Needed() || requirements.isHashIntoGTNeeded());
    }

    @Override
    public boolean checkRequirements(int securityParameter, BilinearGroupRequirement requirements) {
        return true;
    }
}
