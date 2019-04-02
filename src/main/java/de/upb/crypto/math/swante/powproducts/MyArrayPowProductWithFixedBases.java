package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;

/**
 * Common base class representing a multi-exponentiation: b_0^e_0*b_1^e_1*...
 * Bases are assumed to be known in advance (precomputations can therefore be
 * done on them). Exponents are unknown and passed during the evaluation phase.
 */
public class MyArrayPowProductWithFixedBases {
    
    public final int numBases;
    protected final GroupElement[] bases;
    protected final Group group;
    
    public MyArrayPowProductWithFixedBases(GroupElement[] bases) {
        this.bases = bases;
        this.numBases = bases.length;
        this.group = bases[0].getStructure();
    }
    
    /**
     * Very simple implementation without any caching, simply doing the exponentiation
     * seperately for each base.
     * Child classes should override this with something more elaborate.
     *
     * @return bases[0]^exponents[0]*bases[1]^exponents[1]*...
     */
    public GroupElement evaluate(BigInteger[] exponents) {
        GroupElement res = group.getNeutralElement();
        for (int i = 0; i < numBases; i++) {
            res = res.op(bases[i].pow(exponents[i]));
        }
        return res;
    }
    
    public GroupElement getBase(int index) {
        return bases[index];
    }
    
    public Group getGroup() {
        return group;
    }
    
    protected int getLongestExponentBitLength(BigInteger[] exponents) {
        int longestExponentBitLength = 0;
        
        for (int i = 0; i < numBases; i++) {
            longestExponentBitLength = Math.max(longestExponentBitLength, exponents[i].bitLength());
        }
        return longestExponentBitLength;
    }
    
    protected GroupElement[] computeAllSmallPowerProducts(int windowSize) {
        int numPrecomputedPowers = 1 << (windowSize * numBases);
        GroupElement[] smallPowers = new GroupElement[numPrecomputedPowers];
        smallPowers[0] = group.getNeutralElement();
        for (int i = 1; i < (1 << windowSize); i++) {
            smallPowers[i] = smallPowers[i - 1].op(bases[0]);
        }
        for (int b = 1; b < numBases; b++) {
            int shift = windowSize * b;
            for (int e = 1; e < (1 << windowSize); e++) {
                int eShifted = e << shift;
                int previousEShifted = (e - 1) << shift;
                for (int i = 0; i < (1 << shift); i++) {
                    smallPowers[eShifted + i] = smallPowers[previousEShifted + i].op(bases[b]);
                }
            }
        }
        return smallPowers;
    }
    
}
