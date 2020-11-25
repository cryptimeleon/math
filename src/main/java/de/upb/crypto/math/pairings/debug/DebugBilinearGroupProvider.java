package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupImpl;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;

import java.math.BigInteger;
import java.util.ArrayList;

public class DebugBilinearGroupProvider implements BilinearGroupProvider {
    /**
     * Creates a new bilinear debug group whose prime factors have bit size {@code securityParameter}.
     *
     * @param type              type of the pairing
     * @param securityParameter bit size of the prime factors
     * @param numPrimeFactors   number of prime factors making up the group size
     */
    public DebugBilinearGroupImpl provideBilinearGroup(int securityParameter, BilinearGroup.Type type,
                                                       int numPrimeFactors, boolean wantHash) {
        if (securityParameter < 2)
            throw new IllegalArgumentException("Cannot create debug pairing of bit size " + securityParameter);

        ArrayList<BigInteger> primeFactors = new ArrayList<>();
        RandomGenerator rnd = RandomGeneratorSupplier.getRnd();
        for (int i = 0; i < numPrimeFactors; i++)
            primeFactors.add(rnd.getRandomPrime(securityParameter));

        return new DebugBilinearGroupImpl(type, primeFactors.stream().reduce(BigInteger.ONE, BigInteger::multiply),
                wantHash);
    }

    @Override
    public BilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements) {
        return new BasicBilinearGroup(provideBilinearGroupImpl(securityParameter, requirements));
    }

    @Override
    public BilinearGroupImpl provideBilinearGroupImpl(int securityParameter, BilinearGroupRequirement requirements) {
        return provideBilinearGroup(securityParameter, requirements.getType(), requirements.getNumPrimeFactorsOfSize(),
                requirements.isHashIntoG1Needed() || requirements.isHashIntoG2Needed()
                        || requirements.isHashIntoGTNeeded());
    }

    @Override
    public boolean checkRequirements(int securityParameter, BilinearGroupRequirement requirements) {
        return true;
    }
}
