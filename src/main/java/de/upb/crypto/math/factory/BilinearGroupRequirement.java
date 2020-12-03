package de.upb.crypto.math.factory;

/**
 * Requirements for a bilinear group.
 * <p>
 * This class is indented to be used for the configuration of the {@link BilinearGroupFactory}.
 * A user sets up an object with the settings they need the bilinear group to fulfill and registers the config using
 * {@link BilinearGroupFactory#setRequirements(BilinearGroupRequirement)}.
 * The bilinear group factory then uses these requirements to select a bilinear group implementation that fulfills
 * those requirements
 *
 * @author Denis Diemert
 */
public class BilinearGroupRequirement {

    /**
     * The type of the configured bilinear pairing.
     * <p>
     * For the possible values, {@see BilinearGroup.Type}.
     */
    protected BilinearGroup.Type type;

    /**
     * Determines whether the desired bilinear group is configured to support hashing from {@code byte[]} to G1.
     * <p>
     * If true, the resulting factory will be able to supply a hash function {@code byte[] -> G1}; otherwise, it
     * may or may not supply such a function.
     */
    protected boolean hashIntoG1Needed = false;

    /**
     * Determines whether the desired bilinear group is configured to support hashing from {@code byte[]} to G2.
     * <p>
     * If true, the resulting factory will be able to supply a hash function {@code byte[] -> G2}; otherwise, it
     * may or may not supply such a function.
     */
    protected boolean hashIntoG2Needed = false;

    /**
     * Determines whether the desired bilinear group is configured to support hashing from {@code byte[]} to GT.
     * <p>
     * If true, the resulting factory will be able to supply a hash function {@code byte[] -> GT}; otherwise, it
     * may or may not supply such a function.
     */
    protected boolean hashIntoGTNeeded = false;

    /**
     * Determines the number of prime factors of the size of the bilinear group's order.
     * <p>
     * If set to 1, the resulting {@link BilinearGroup} will consist of (G1, G2, GT) of prime order. Else, if set to a
     * value greater than 1, the group order is a composite number with {@code numPrimeFactorsOfSize} prime factors.
     */
    protected int numPrimeFactorsOfSize;

    /**
     * Standard constructor to set all requirements by hand.
     *
     * @param type                       the desired type of the resulting bilinear group
     * @param hashIntoG1Needed           true enforces that the resulting bilinear group provides a mapping from
     *                                   {@code byte[]} to G1. When set to false,
     *                                   it may still be supported.
     * @param hashIntoG2Needed           true enforces that the resulting bilinear group provides a mapping from
     *                                   {@code byte[]} to G2. When set to false,
     *                                   it may still be supported.
     * @param hashIntoGTNeeded           true enforces that the resulting bilinear group provides a mapping from
     *                                   {@code byte[]} to GT. When set to false,
     *                                   it may still be supported.
     * @param numPrimeFactorsOfSize desired number of prime factors of the size of the resulting groups
     */
    public BilinearGroupRequirement(BilinearGroup.Type type, boolean hashIntoG1Needed, boolean hashIntoG2Needed,
                                    boolean hashIntoGTNeeded, int numPrimeFactorsOfSize) {
        this.type = type;
        this.hashIntoG1Needed = hashIntoG1Needed;
        this.hashIntoG2Needed = hashIntoG2Needed;
        this.hashIntoGTNeeded = hashIntoGTNeeded;
        this.numPrimeFactorsOfSize = numPrimeFactorsOfSize;
    }

    /**
     * Constructor for prime order groups (the group size factors into a single prime factor).
     *
     * @param type             the desired type of the resulting bilinear group
     * @param hashIntoG1Needed           true enforces that the resulting bilinear group provides a mapping from
     *                                   {@code byte[]} to G1. When set to false,
     *                                   it may still be supported.
     * @param hashIntoG2Needed           true enforces that the resulting bilinear group provides a mapping from
     *                                   {@code byte[]} to G2. When set to false,
     *                                   it may still be supported.
     * @param hashIntoGTNeeded           true enforces that the resulting bilinear group provides a mapping from
     *                                   {@code byte[]} to GT. When set to false,
     *                                   it may still be supported.
     */
    public BilinearGroupRequirement(BilinearGroup.Type type, boolean hashIntoG1Needed, boolean hashIntoG2Needed,
                                    boolean hashIntoGTNeeded) {
        this(type, hashIntoG1Needed, hashIntoG2Needed, hashIntoGTNeeded, 1);
    }

    /**
     * Constructor for prime order groups without any requirements for hashing.
     *
     * @param type the desired type of the resulting bilinear group
     */
    public BilinearGroupRequirement(BilinearGroup.Type type) {
        this(type, false, false, false);
    }

    /**
     * Constructor for composite order groups without any requirements for hashing.
     *
     * @param type the desired type of the resulting bilinear group
     * @param numPrimeFactorsOfSize number of prime factors of the resulting groups
     */
    public BilinearGroupRequirement(BilinearGroup.Type type, int numPrimeFactorsOfSize) {
        this(type, false, false, false, numPrimeFactorsOfSize);
    }

    /**
     * Retrieves the type of the configured bilinear pairing.
     * <p>
     * For the possible values, see {@link BilinearGroup.Type}.
     */
    public BilinearGroup.Type getType() {
        return type;
    }

    /**
     * Checks whether the desired bilinear group is configured to support hashing from {@code byte[]} to G1.
     * <p>
     * If true, the resulting factory will be able to supply a hash function {@code byte[] -> G1}; otherwise, it
     * may or may not supply such a function.
     */
    public boolean isHashIntoG1Needed() {
        return hashIntoG1Needed;
    }

    /**
     * Checks whether the desired bilinear group is configured to support hashing from {@code byte[]} to G2.
     * <p>
     * If true, the resulting factory will be able to supply a hash function {@code byte[] -> G2}; otherwise, it
     * may or may not supply such a function.
     */
    public boolean isHashIntoG2Needed() {
        return hashIntoG2Needed;
    }

    /**
     * Checks whether the desired bilinear group is configured to support hashing from {@code byte[]} to GT.
     * <p>
     * If true, the resulting factory will be able to supply a hash function {@code byte[] -> GT}; otherwise, it
     * may or may not supply such a function.
     */
    public boolean isHashIntoGTNeeded() {
        return hashIntoGTNeeded;
    }

    /**
     * Retrieves the number of prime factors of the size of the bilinear group's order.
     * <p>
     * If set to 1, the resulting {@link BilinearGroup} will consist of (G1, G2, GT) of prime order. Else, if set to a
     * value greater than 1, the group order is a composite number with {@code numPrimeFactorsOfSize} prime factors.
     */
    public int getNumPrimeFactorsOfSize() {
        return numPrimeFactorsOfSize;
    }
}
