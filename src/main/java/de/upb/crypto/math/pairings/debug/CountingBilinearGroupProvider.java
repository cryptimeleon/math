package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;

import java.math.BigInteger;
import java.util.ArrayList;

public class CountingBilinearGroupProvider {

    public BilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements, PairingExpGroup pairingExpGroup) {
        if (securityParameter < 2)
            throw new IllegalArgumentException("Cannot create debug pairing of bit size " + securityParameter);

        int numPrimeFactors = requirements.getNumPrimeFactorsOfSize();
        boolean wantHash = requirements.isHashIntoG1Needed() || requirements.isHashIntoG2Needed()
                || requirements.isHashIntoGTNeeded();

        ArrayList<BigInteger> primeFactors = new ArrayList<>();
        RandomGenerator rnd = RandomGeneratorSupplier.getRnd();
        for (int i = 0; i < numPrimeFactors; i++)
            primeFactors.add(rnd.getRandomPrime(securityParameter));

        return new CountingBilinearGroup(
                requirements.getType(),
                primeFactors.stream().reduce(BigInteger.ONE, BigInteger::multiply),
                wantHash,
                pairingExpGroup
        );
    }
}
