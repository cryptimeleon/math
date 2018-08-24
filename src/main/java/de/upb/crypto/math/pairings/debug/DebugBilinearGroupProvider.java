package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.serialization.annotations.RepresentedList;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

import static de.upb.crypto.math.factory.BilinearGroup.Type.TYPE_1;
import static de.upb.crypto.math.factory.BilinearGroup.Type.TYPE_3;

/**
 * Creates bilinear groups based on the integer ring modulo n for some number n.
 * The bilinear map (Zn,+) x (Zn,+) -> (Zn,+) is the ring multiplication.
 * <p>
 * This is intentionally not a {@link BilinearGroupProvider}, because the returned group are not secure!
 */
public class DebugBilinearGroupProvider implements BilinearGroup {
    @Represented
    protected DebugIsomorphism homomorphism;
    @RepresentedList(elementRestorer = @Represented)
    protected ArrayList<BigInteger> primeFactors;
    @Represented
    protected DebugBilinearMap bilinearMap;

    protected void init(int type, BigInteger size) {
        bilinearMap = new DebugBilinearMap(type, size);

        if (type < 3)
            homomorphism = new DebugIsomorphism(getG2(), getG1());
    }

    public DebugBilinearGroupProvider() {
    }

    public DebugBilinearGroupProvider(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
    }

    @Override
    public DebugGroup getG1() {
        return bilinearMap.g1;
    }

    @Override
    public DebugGroup getG2() {
        return bilinearMap.g2;
    }

    @Override
    public DebugGroup getGT() {
        return bilinearMap.gt;
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilinearMap;
    }

    @Override
    public HashIntoDebugGroup getHashIntoG1() {
        return new HashIntoDebugGroup(bilinearMap.g1);
    }

    @Override
    public HashIntoDebugGroup getHashIntoG2() {
        return new HashIntoDebugGroup(bilinearMap.g2);
    }

    @Override
    public HashIntoDebugGroup getHashIntoGT() {
        return new HashIntoDebugGroup(bilinearMap.gt);
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() {
        return homomorphism;
    }

    @Override
    public HashIntoStructure getHashIntoZGroupExponent() throws UnsupportedOperationException {
        return new HashIntoZn(bilinearMap.size);
    }

    public DebugBilinearGroupProvider getPairingParameters() {
        return this;
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugBilinearGroupProvider that = (DebugBilinearGroupProvider) o;
        return Objects.equals(homomorphism, that.homomorphism) &&
                Objects.equals(primeFactors, that.primeFactors) &&
                Objects.equals(bilinearMap, that.bilinearMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(homomorphism, primeFactors, bilinearMap);
    }

    /**
     * Creates a new Debug group whose prime factors have bit size {@code securityParameter}
     *
     * @param type              type of the pairing (type 1: G1 = G2; type 2: G1 != G2 and there is a nondegenerate homomorphism G2 -> G1; type 3: G1 != G2 and there are no efficiently computable injective homomorphisms between G1 and G2
     * @param securityParameter bit size of the prime factors
     * @param numPrimeFactors   number of prime factors
     */
    public DebugBilinearGroupProvider provideBilinearGroup(int securityParameter, BilinearGroup.Type type, int numPrimeFactors) {
        if (securityParameter < 2)
            throw new IllegalArgumentException("Cannot create debug pairing of bit size " + securityParameter);

        primeFactors = new ArrayList<>();
        RandomGenerator rnd = RandomGeneratorSupplier.getRnd();
        for (int i = 0; i < numPrimeFactors; i++)
            primeFactors.add(rnd.getRandomPrime(securityParameter));

        init((type == TYPE_3) ? 3 : ((type == TYPE_1) ? 1 : 2), primeFactors.stream().reduce(BigInteger.ONE, BigInteger::multiply));

        return getPairingParameters();
    }

    /**
     * Short hand for a type 1 prime order bilinear debug group.
     *
     * @param securityParameter
     */
    public DebugBilinearGroupProvider provideBilinearGroup(int securityParameter) {
        return this.provideBilinearGroup(securityParameter, TYPE_1, 1);
    }
}
