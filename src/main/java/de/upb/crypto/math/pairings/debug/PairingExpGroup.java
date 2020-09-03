package de.upb.crypto.math.pairings.debug;

/**
 * Tells the {@link CountingBilinearGroup} in which group the exponentiation in e(g1, g2)^a should be counted.
 * Allows the user to configure this since we don't know where the actual group would do it.
 */
public enum PairingExpGroup {
    G1, G2, GT
}
