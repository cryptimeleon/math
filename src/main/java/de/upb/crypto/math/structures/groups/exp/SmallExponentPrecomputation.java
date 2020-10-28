package de.upb.crypto.math.structures.groups.exp;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

import java.util.ArrayList;

public class SmallExponentPrecomputation {
    GroupElementImpl base;
    ArrayList<GroupElementImpl> oddPowers = null; //oddPowers.get(i) == base^(2*i+1)
    ArrayList<GroupElementImpl> oddNegativePowers = null; //oddNegativePowers.get(i) == base^(-2*i-1)
    int windowSize = 0;
    int negativeWindowSize = 0;

    public SmallExponentPrecomputation(GroupElementImpl base) {
        this.base = base;
    }

    public int getCurrentMaxPositiveExponent() {
        return oddPowers == null ? 0 : 2*oddPowers.size()-1;
    }

    public int getCurrentMaxNegativeExponent() {
        return oddNegativePowers == null ? 0 : -2*oddNegativePowers.size()+1;
    }

    public int getCurrentlySupportedPositiveWindowSize() {
        return windowSize;
    }

    public int getCurrentlySupportedNegativeWindowSize() {
        return negativeWindowSize;
    }

    public int getCurrentlySupportedWindowSize() {
        return Math.max(windowSize, negativeWindowSize);
    }

    public GroupElementImpl get(int exponent) {
        if (exponent == 0)
            return base.getStructure().getNeutralElement();
        if (exponent < 0)
            return getOddNegativePower(exponent);
        if (exponent % 2 != 1)
            return get(exponent-1).op(base);

        return getOddPositivePower(exponent);
    }

    public GroupElementImpl getOddPositivePower(int exponent) {
        if (getCurrentMaxPositiveExponent() < exponent) {
            return getOddNegativePower(-exponent).inv();
        }
        int index = (exponent-1)/2;
        return oddPowers.get(index);
    }

    public GroupElementImpl getOddNegativePower(int exponent) {
        if (getCurrentMaxNegativeExponent() > exponent) {
            return getOddPositivePower(-exponent).inv();
        }
        int index = (-exponent-1)/2;
        return oddNegativePowers.get(index);
    }

    public void compute(int windowSize) {
        if (this.windowSize < windowSize) {
            int maximumPower = (1 << windowSize) - 1;
            int numElements = (maximumPower+1)/2;

            synchronized (this) {
                if (this.windowSize < windowSize) {
                    if (oddPowers == null) {
                        oddPowers = new ArrayList<>(numElements);
                        oddPowers.add(base);
                    }

                    GroupElementImpl square = base.square();
                    GroupElementImpl currentSmallPower = oddPowers.get(oddPowers.size() - 1);
                    for (int i = oddPowers.size(); i < numElements; i++) {
                        currentSmallPower = currentSmallPower.op(square);
                        oddPowers.add(i, currentSmallPower);
                    }

                    this.windowSize = windowSize;
                }
            }
        }
    }

    public void computeNegativePowers(int windowSize) {
        if (this.negativeWindowSize < windowSize) {
            int maximumPower = (1 << windowSize) - 1;
            int numElements = (maximumPower+1)/2;

            synchronized (this) {
                if (this.negativeWindowSize < windowSize) {
                    GroupElementImpl invBase = base.inv();
                    if (oddNegativePowers == null) {
                        oddNegativePowers = new ArrayList<>(numElements);
                        oddNegativePowers.add(invBase);
                    }

                    GroupElementImpl square = invBase.square();
                    GroupElementImpl currentSmallPower = oddNegativePowers.get(oddNegativePowers.size() - 1);
                    for (int i = oddNegativePowers.size(); i < numElements; i++) {
                        currentSmallPower = currentSmallPower.op(square);
                        oddNegativePowers.add(i, currentSmallPower);
                    }

                    this.negativeWindowSize = windowSize;
                }
            }
        }
    }
}
