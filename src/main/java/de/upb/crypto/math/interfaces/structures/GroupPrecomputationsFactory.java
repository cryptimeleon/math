package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * We use a factory with a store to enable having different precomputations per group.
 * Then we can just serialize the precomputations for a specific group if we want.
 * When deserializing, we can look into the store to see if the object exists already and
 * just update it with the deserialized precomputations.
 */
public class GroupPrecomputationsFactory {

    private static final Map<Group, GroupPrecomputations> store =
            new HashMap<Group, GroupPrecomputations>();

    /**
     * Class that stores precomputed group elements for a specific group.
     */
    public static final class GroupPrecomputations implements Representable {

        /**
         * Stores precomputed odd powers per base.
         */
        @Represented(restorer = "G->[G]]")
        private Map<GroupElement, List<GroupElement>> oddPowers;

        /**
         * Stores power products for simultaneous multiexponentiation algorithm.
         */
        // TODO: How to do this representation?
        private Map<PowerProductKey, List<GroupElement>> powerProducts;

        /**
         * Group this object stores precomputations for.
         */
        private Group group;


        public GroupPrecomputations(Group group) {
            // TODO: Using this enough for thread safety?
            oddPowers = new ConcurrentHashMap<>();
            powerProducts = new ConcurrentHashMap<>();
            this.group = group;
        }

        public GroupPrecomputations(Representation repr, Group group) {
            new ReprUtil(this).register(group, "G").deserialize(repr);
            this.group = group;
        }

        /**
         * Add precomputed odd powers to storage.
         * @param base Base to add odd powers for.
         * @param maxExp maximum exponent to add odd powers up to
         */
        public void addOddPowers(GroupElement base, int maxExp) {
            List<GroupElement> baseOddPowers =
                    this.oddPowers.computeIfAbsent(base, k -> new ArrayList<>());
            GroupElement baseSquared = base.op(base);
            if (baseOddPowers.size() == 0) {
                baseOddPowers.add(base);
            }
            GroupElement newPower = baseOddPowers.get(baseOddPowers.size()-1);;
            for (int i = baseOddPowers.size(); i < (maxExp+1)/2; ++i) {
                newPower = newPower.op(baseSquared);
                baseOddPowers.add(newPower);
            }
        }

        /**
         * Retrieve odd powers base^1, base^3, ..., base^maxExp (if maxExp is odd else maxExp-1).
         * If they are not computed yet, then it computes them first.
         *
         * @param base
         * @param maxExp
         * @return
         */
        public List<GroupElement> getOddPowers(GroupElement base, int maxExp) {
            List<GroupElement> baseOddPowers =
                    this.oddPowers.computeIfAbsent(base, k -> new ArrayList<>());
            // if we are missing some powers, compute them first
            if (baseOddPowers.size() < (maxExp+1)/2) {
                this.addOddPowers(base, maxExp);
            }
            return baseOddPowers.subList(0, (maxExp+1)/2);
        }

        /**
         * Adds all power products of combinations of bases restricted by window size. Used
         * for simultaneous multiexponentiation algorithm.
         *
         * @param bases
         * @param windowSize
         */
        public void addPowerProducts(List<GroupElement> bases, int windowSize) {
            // TODO: prevent computing already computed power products
            int numPrecomputedPowers = 1 << (windowSize * bases.size());
            PowerProductKey key = new PowerProductKey(bases, windowSize);
            List<GroupElement> powerProductsEntry = powerProducts
                    .computeIfAbsent(key, k -> new ArrayList<>(numPrecomputedPowers));
            // prefill arraylist
            for (int i = 0; i < numPrecomputedPowers; ++i) {
                powerProductsEntry.add(group.getNeutralElement());
            }

            powerProductsEntry.set(0, group.getNeutralElement());
            for (int i = 1; i < (1 << windowSize); i++) {
                powerProductsEntry.set(i, powerProductsEntry.get(i-1).op(bases.get(0)));
            }
            for (int b = 1; b < bases.size(); b++) {
                int shift = windowSize * b;
                for (int e = 1; e < (1 << windowSize); e++) {
                    int eShifted = e << shift;
                    int previousEShifted = (e - 1) << shift;
                    for (int i = 0; i < (1 << shift); i++) {
                        powerProductsEntry.set(
                                eShifted + i,
                                powerProductsEntry.get(previousEShifted + i).op(bases.get(b))
                        );
                    }
                }
            }
        }

        /**
         * Retrieve power products for simultaneous multi-exponentiation.
         * @param bases
         * @param windowSize
         * @return
         */
        public List<GroupElement> getPowerProducts(List<GroupElement> bases, int windowSize) {
            PowerProductKey key = new PowerProductKey(bases, windowSize);
            List<GroupElement> powerProductsEntry = powerProducts.get(key);
            if (powerProductsEntry == null) {
                addPowerProducts(bases, windowSize);
            }
            return powerProducts.get(key);
        }


        private static class PowerProductKey {

            private GroupElement[] bases;

            private int windowSize;

            PowerProductKey(List<GroupElement> bases, int windowSize) {
                this.bases = bases.toArray(new GroupElement[0]);
                this.windowSize = windowSize;
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(bases) + 31 * windowSize;
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof PowerProductKey)) {
                    return false;
                }
                PowerProductKey otherPPK = (PowerProductKey) other;
                return Arrays.equals(bases, otherPPK.bases) && windowSize == otherPPK.windowSize;
            }
        }

        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
        }

        /**
         * Delete all precomputations.
         */
        public void reset() {
            oddPowers = new ConcurrentHashMap<>();
            powerProducts = new ConcurrentHashMap<>();
        }
    }

    /**
     * Method to retrieve precomputations for a group.
     *
     * For being able to retrieve using the group it is important that the hashcode of two different
     * group objects that represent the same group match up, and the same holds for equals.
     * @param group The group to obtain precomputations for.
     * @return Precomputations of the group.
     */
    public static GroupPrecomputations get(Group group) {
        synchronized (store) {
            GroupPrecomputations result = store.get(group);
            if (result == null) {
                result = new GroupPrecomputations(group);
                store.put(group, result);
            }
            return result;
        }
    }

    /**
     * Allows adding deserialized precomputations object to the store. Merges with existing
     * precomputations if one already exists for the group.
     * @param gp The precomputations to add to the store.
     */
    public static void addGroupPrecomputations(GroupPrecomputations gp) {
        synchronized (store) {
            // Check if precomputations for the group exist
            GroupPrecomputations result = store.get(gp.group);
            if (result != null) {
                // Combine them
                // TODO: What if one map contains different entries than other?
                result.oddPowers.putAll(gp.oddPowers);
            } else {
                store.put(gp.group, gp);
            }
        }
    }
}