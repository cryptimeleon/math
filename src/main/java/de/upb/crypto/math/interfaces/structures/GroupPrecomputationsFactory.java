package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * We use a factory with a store to enable having different precomputations per group.
 * Then we can just serialize the precomputations for a specific group if we want.
 * When deserializing, we can look into the store to see if the object exists already and
 * just update it with the deserialized precomputations.
 *
 * @author Raphael Heitjohann
 */
public class GroupPrecomputationsFactory {

    private static final Map<Group, GroupPrecomputations> store = new HashMap<>();

    /**
     * Class that stores precomputed group elements for a specific group.
     */
    public static final class GroupPrecomputations implements Representable,
            RepresentationRestorer {

        /**
         * Stores precomputed odd powers per base.
         */
        @Represented(restorer = "group->[group]")
        private Map<GroupElement, List<GroupElement>> oddPowers;

        /**
         * Stores power products for simultaneous multi-exponentiation algorithm.
         */
        @Represented(restorer = "P->[group]")
        private Map<PowerProductKey, List<GroupElement>> powerProducts;

        /**
         * Group this object stores precomputations for.
         */
        @Represented
        private Group group;



        GroupPrecomputations(Group group) {
            oddPowers = new ConcurrentHashMap<>();
            powerProducts = new ConcurrentHashMap<>();
            this.group = group;
        }

        public GroupPrecomputations(Representation repr) {
            new ReprUtil(this).register(this, "P").deserialize(repr);
        }

        /**
         * Add precomputed odd powers to storage. If {@code base} is an instance of {@link AbstractGroupElement},
         * the odd powers are also stored in the base itself for easier retrieval later.
         * @param base Base to add odd powers for.
         * @param maxExp Maximum exponent to add odd powers up to.
         */
        public void addOddPowers(GroupElement base, int maxExp) {
            addOddPowers(base, maxExp,
                    this.oddPowers.computeIfAbsent(base, k -> new ArrayList<>((maxExp+1)/2)));
        }

        /**
         * Add precomputed odd powers to storage. If {@code base} is an instance of {@link AbstractGroupElement},
         * the odd powers are also stored in the base itself for easier retrieval later.
         * @param base Base to add odd powers for.
         * @param maxExp Maximum exponent to add odd powers up to.
         * @param baseOddPowers List of existing bases. New bases will be added here. Saves getting the list from
         *                      the map twice if we already have it.
         */
        public void addOddPowers(GroupElement base, int maxExp, List<GroupElement> baseOddPowers) {
            GroupElement baseSquared = base.op(base);
            if (baseOddPowers.size() == 0) {
                baseOddPowers.add(base);
            }
            GroupElement newPower = baseOddPowers.get(baseOddPowers.size()-1);;
            for (int i = baseOddPowers.size(); i < (maxExp+1)/2; ++i) {
                newPower = newPower.op(baseSquared);
                baseOddPowers.add(newPower);
            }
            if (base instanceof AbstractGroupElement) {
                ((AbstractGroupElement) base).setCachedOddPowers(baseOddPowers);
            }
        }

        /**
         * Retrieve odd powers base^1, base^3, ..., base^minExp, ... (if minExp is odd else minExp-1).
         * Could also be more than that if available.
         * If they are not computed yet, then it computes them first.
         *
         * @param base Base to retrieve odd powers for.
         * @param minExp Minimum exponent up to which (inclusive) powers are supposed to be retrieved.
         * @return List of precomputed odd powers sorted ascending by exponent.
         */
        public List<GroupElement> getOddPowers(GroupElement base, int minExp) {
            return getOddPowers(base, minExp, true);
        }

        /**
         * Retrieve odd powers base^1, base^3, ..., base^minMaxExp, ... (if minMaxExp is odd else minMaxExp-1).
         * Could also be more than that if available.
         * If they are not computed yet, then it can compute them first, depending on parameter.
         *
         * @param base Base to retrieve odd powers for.
         * @param minExp Minimum exponent up to which (inclusive) powers are supposed to be retrieved.
         * @param computeIfMissing Whether to compute odd powers if they have not been precomputed
         *                         yet.
         * @return List of precomputed odd powers sorted ascending by exponent.
         */
        public List<GroupElement> getOddPowers(GroupElement base, int minExp,
                                               boolean computeIfMissing) {
            List<GroupElement> baseOddPowers;
            if (base instanceof AbstractGroupElement) {
                AbstractGroupElement abstractBase = (AbstractGroupElement) base;
                baseOddPowers = abstractBase.getCachedOddPowers();
                // powers are bound to instance so instance might not have all the group elements that are actually
                // cached, e.g. if we cache powers for one instance of a group element only that instance will
                // have them set, any new instances won't. So we have to additionally consider the ones stored
                // in the map, as here the instance does not matter.
                if (baseOddPowers.size() < (minExp+1)/2) {
                    List<GroupElement> mapBaseOddPowers =
                            this.oddPowers.computeIfAbsent(base, k -> new ArrayList<>((minExp+1)/2));
                    if (mapBaseOddPowers.size() > baseOddPowers.size()) {
                        // also store them in this instance
                        abstractBase.setCachedOddPowers(mapBaseOddPowers);
                        baseOddPowers = mapBaseOddPowers;
                    }
                }
            } else {
                baseOddPowers =
                        this.oddPowers.computeIfAbsent(base, k -> new ArrayList<>((minExp+1)/2));
            }
            // if we are missing some powers, compute them first if advised
            if (baseOddPowers.size() < (minExp+1)/2) {
                if (computeIfMissing) {
                    this.addOddPowers(base, minExp, baseOddPowers);
                } else {
                    throw new IllegalStateException("Missing precomputed odd powers for "
                            + base + " and min max exponent " + minExp + ".");
                }
            }
            return baseOddPowers;
        }

        /**
         * Adds all power products of combinations of bases restricted by window size. Used
         * for simultaneous multiexponentiation algorithm.
         *
         * @param bases Bases to precompute and cache power products for.
         * @param windowSize Used to calculate maximum exponent for powers.
         */
        public void addPowerProducts(List<GroupElement> bases, int windowSize) {
            // TODO: reuse already computed power products for smaller window sizes
            //  difficult because of ordering of existing ones (see test for this method)
            int numPrecomputedPowers = 1 << (windowSize * bases.size());
            PowerProductKey key = new PowerProductKey(bases, windowSize);
            List<GroupElement> powerProductsEntry = powerProducts
                    .computeIfAbsent(key, k -> new ArrayList<>(numPrecomputedPowers));
            // prefill arraylist
            GroupElement neutralElement = group.getNeutralElement();
            for (int i = 0; i < numPrecomputedPowers; ++i) {
                powerProductsEntry.add(neutralElement);
            }

            //powerProductsEntry.set(0, group.getNeutralElement());
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
         * @param bases Bases to retrieve power products for.
         * @param windowSize Used to calculate maximum exponent for powers.
         * @return List of power products.
         */
        public List<GroupElement> getPowerProducts(List<GroupElement> bases, int windowSize) {
            return getPowerProducts(bases, windowSize, true);
        }

        /**
         * Retrieve power products for simultaneous multi-exponentiation.
         * @param bases Bases to retrieve power products for.
         * @param windowSize Used to calculate maximum exponent for powers.
         * @param computeIfMissing Whether to compute power products if they are missing.
         * @return List of power products.
         */
        public List<GroupElement> getPowerProducts(List<GroupElement> bases, int windowSize,
                                                   boolean computeIfMissing) {
            PowerProductKey key = new PowerProductKey(bases, windowSize);
            List<GroupElement> powerProductsEntry = powerProducts.get(key);
            if (powerProductsEntry == null) {
                if (computeIfMissing) {
                    addPowerProducts(bases, windowSize);
                } else {
                    throw new IllegalStateException("Missing precomputed product powers for "
                            + key);
                }
            }
            return powerProducts.get(key);
        }

        @Override
        public Object recreateFromRepresentation(Type type, Representation repr) {
            if (type == PowerProductKey.class) {
                return new PowerProductKey(repr, this.group);
            } else {
                throw new IllegalArgumentException("Dont know how to handle type "
                        + type.getTypeName());
            }
        }

        /**
         * A key for the power products map. Represents a set of power products by the bases
         * and the window size.
         */
        private static class PowerProductKey implements Representable {

            @Represented(restorer="[G]")
            private GroupElement[] bases;

            @Represented(restorer="Int")
            private Integer windowSize;

            PowerProductKey(List<GroupElement> bases, int windowSize) {
                this.bases = bases.toArray(new GroupElement[0]);
                this.windowSize = windowSize;
            }

            PowerProductKey(Representation repr, Group group) {
                new ReprUtil(this).register(group, "G").deserialize(repr);
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
                return Arrays.equals(bases, otherPPK.bases)
                        && windowSize.equals(otherPPK.windowSize);
            }

            @Override
            public String toString() {
                return "PowerProductKey for bases " + Arrays.toString(bases) + " and window size "
                        + windowSize;
            }

            @Override
            public Representation getRepresentation() {
                return ReprUtil.serialize(this);
            }
        }

        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
        }

        /**
         * Delete all pre-computations.
         */
        public void reset() {
            for (GroupElement g : oddPowers.keySet()) {
                if (g instanceof AbstractGroupElement) {
                    ((AbstractGroupElement) g).setCachedOddPowers(new ArrayList<>());
                }
            }
            oddPowers = new ConcurrentHashMap<>();
            powerProducts = new ConcurrentHashMap<>();
        }

        @Override
        public boolean equals(Object other) {
            GroupPrecomputations gp = (GroupPrecomputations) other;
            return this.group.equals(gp.group)
                    && this.oddPowers.equals(gp.oddPowers)
                    && this.powerProducts.equals(gp.powerProducts);
        }
    }

    /**
     * Method to retrieve pre-computations for a group.
     *
     * For being able to retrieve using the group it is important that the hashcode of two different
     * group objects that represent the same group match up, and the same holds for equals.
     * @param group The group to obtain pre-computations for.
     * @return Pre-computations of the group.
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
     * Allows adding deserialized pre-computations object to the store. Merges with existing
     * pre-computations if one already exists for the group.
     * @param gp The pre-computations to add to the store.
     */
    public static void addGroupPrecomputations(GroupPrecomputations gp) {
        synchronized (store) {
            // Check if precomputations for the group exist
            GroupPrecomputations result = store.get(gp.group);
            if (result != null) {
                // Add the precomputations to the abstract ones first, else they might be inconsistent with each other
                for (GroupElement g : result.oddPowers.keySet()) {
                    if (g instanceof AbstractGroupElement) {
                        ((AbstractGroupElement) g).setCachedOddPowers(result.oddPowers.get(g));
                    }
                }
                // Combine them
                result.oddPowers.putAll(gp.oddPowers);
                result.powerProducts.putAll(gp.powerProducts);
            } else {
                store.put(gp.group, gp);
            }
        }
    }
}
