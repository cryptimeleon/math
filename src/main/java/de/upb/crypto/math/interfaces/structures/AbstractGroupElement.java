package de.upb.crypto.math.interfaces.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides additional functionality for group elements which cannot be realized by an
 * interface such as {@link GroupElement} due to interfaces not supporting attributes.
 * For example, group elements can cache their own precomputed odd powers to make retrieving them easier
 * for exponentiation algorithms.
 *
 * @author Raphael Heitjohann
 */
public abstract class AbstractGroupElement {

    List<GroupElement> cachedOddPowers;

    public AbstractGroupElement() {
        cachedOddPowers = new ArrayList<>();
    }

    /**
     * Retrieve cached odd powers.
     * @return List of cached odd powers b_1, b_3, ... b^{cachedOddPowers.size()*2-1}
     */
    public List<GroupElement> getCachedOddPowers() {
        return cachedOddPowers;
    }

    public boolean hasEnoughOddPowers(int maxExp) {
        return cachedOddPowers.size() >= (maxExp+1)/2;
    }

    public void setCachedOddPowers(List<GroupElement> cachedOddPowers) {
        this.cachedOddPowers = cachedOddPowers;
    }
}
