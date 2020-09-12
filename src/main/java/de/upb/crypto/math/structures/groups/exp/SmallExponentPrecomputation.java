package de.upb.crypto.math.structures.groups.exp;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

import java.util.ArrayList;

public class SmallExponentPrecomputation {
    GroupElementImpl base;
    ArrayList<GroupElementImpl> oddPowers = null; //oddPowers.get(i) == base^(2*i+1)
    int windowSize = 0;

    public SmallExponentPrecomputation(GroupElementImpl base) {
        this.base = base;
    }

    public SmallExponentPrecomputation(GroupElementImpl base, int windowSize) {
        this.base = base;
        compute(windowSize);
    }

    public int currentMaxExponent() {
        return oddPowers == null ? 0 : 2*oddPowers.size()-1;
    }

    public int getCurrentSupportedWindowSize() {
        return windowSize;
    }

    public GroupElementImpl get(int exponent) {
        if (exponent == 0)
            return base.getStructure().getNeutralElement();
        if (exponent < 0)
            return get(-exponent).inv();
        if (exponent % 2 != 1)
            return get(exponent-1).op(base);

        return getOddPositivePower(exponent);
    }

    public GroupElementImpl getOddPositivePower(int exponent) {
        int index = (exponent-1)/2;
        return oddPowers.get(index);
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
}
