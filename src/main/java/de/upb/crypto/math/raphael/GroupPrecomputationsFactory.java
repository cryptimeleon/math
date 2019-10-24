package de.upb.crypto.math.raphael;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
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
     * Need to keep thread-safety in mind. There should never be a need to update any values,
     * since since a precomputed values is either correct or not correct. So only adding
     * values needs aquisition of lock?
     *
     * Also, does the interface as is provide enough performance since we just retrieve powers
     * one by one?
     */
    public static final class GroupPrecomputations implements Representable {

        /**
         * Stores precomputed powers.
         *
         * TODO: Do we even need BigInteger here? Makes no sense to cache really big powers.
         */
        @Represented(restorer = "G->long->G")
        private Map<GroupElement, Map<BigInteger, GroupElement>> powers;

        /**
         * Group this object stores precomputations for.
         * TODO: How to serialize this?
         */
        private Group group;

        public GroupPrecomputations(Group group) {
            // TODO: Using this enough for thread safety?
            powers = new ConcurrentHashMap<>();
            this.group = group;
        }

        public GroupPrecomputations(Representation repr, Group group) {
            new ReprUtil(this).register(group, "G").deserialize(repr);
            this.group = group;
        }

        /**
         * Enter computed power in precomputation table.
         * TODO: Should we offer this method? Allows inserting incorrect elements.
         *
         * @param base The base of the power.
         * @param exponent The exponent.
         * @param result Result of base^exponent.
         */
        public void addPower(GroupElement base, BigInteger exponent, GroupElement result) {
            Map<BigInteger, GroupElement> baseEntry =
                    powers.computeIfAbsent(base, k -> new ConcurrentHashMap<>());
            baseEntry.put(exponent, result);
        }

        public GroupElement getPower(GroupElement base, BigInteger exponent) {
            return getPower(base, exponent, true);
        }

        /**
         * Retrieve power from precomputations.
         *
         * @param base Base of the power.
         * @param exponent Exponent of the power.
         * @param computeIfMissing Whether to precompute if power is not precomputed yet.
         * @return Power base^exponent. Null if computeIfMissing set to false and power
         *  not computed yet.
         */
        public GroupElement getPower(GroupElement base, BigInteger exponent,
                                     boolean computeIfMissing) {
            Map<BigInteger, GroupElement> baseEntry =
                    powers.computeIfAbsent(base, k -> new ConcurrentHashMap<>());
            GroupElement result = baseEntry.get(exponent);
            if (result == null && computeIfMissing) {
                // TODO: Should we even offer this? Seems weird for precomputations storage to do
                //  computations itself. Better to let user always decide maybe?
                result = base.pow(exponent);
                baseEntry.put(exponent, result);
                return result;
            }
            return result;
        }

        /**
         * Add precomputed odd powers to storage.
         * @param base Base to add odd powers for.
         * @param oddPowers Array of odd powers. Must contain base^1, base^3, ..., base^m for
         *                  some uneven m. Otherwise powers map will be corrupted.
         */
        public void addOddPowers(GroupElement base, GroupElement[] oddPowers) {
            Map<BigInteger, GroupElement> baseEntry =
                    powers.computeIfAbsent(base, k -> new ConcurrentHashMap<>());
            for (int i = 1; i < 2*oddPowers.length; i+=2) {
                baseEntry.put(BigInteger.valueOf(i), oddPowers[i/2]);
            }

        }

        /**
         * Retrieve odd powers base^1, base^3, ..., base^maxExp (if maxExp is odd else maxExp-1).
         *
         * @param base
         * @param maxExp
         * @return
         */
        public GroupElement[] getOddPowers(GroupElement base, int maxExp) {
            GroupElement[] oddPowers = new GroupElement[(maxExp+1)/2];
            Map<BigInteger, GroupElement> baseEntry = powers.get(base);
            // if no precomputations for entry yet,
            if (baseEntry == null) {
                return null;
            }
            GroupElement entry;
            for (int i = 1; i < maxExp+1; i+=2) {
                entry = baseEntry.get(BigInteger.valueOf(i));
                if (entry == null)
                    return null;
                oddPowers[i/2] = entry;
            }
            return oddPowers;
        }

        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
        }

        /**
         * Delete all precomputations.
         */
        public void reset() {
            powers = new ConcurrentHashMap<>();
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
                result.powers.putAll(gp.powers);
            } else {
                store.put(gp.group, gp);
            }
        }
    }
}
