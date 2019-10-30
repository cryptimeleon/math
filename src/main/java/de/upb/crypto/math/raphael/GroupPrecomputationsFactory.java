package de.upb.crypto.math.raphael;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.math.BigInteger;
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
     * Need to keep thread-safety in mind. There should never be a need to update any values,
     * since since a precomputed values is either correct or not correct. So only adding
     * values needs aquisition of lock?
     *
     * Also, does the interface as is provide enough performance since we just retrieve powers
     * one by one?
     */
    public static final class GroupPrecomputations implements Representable {

        /**
         * Stores precomputed odd powers per base.
         */
        @Represented(restorer = "G->[G]]")
        private Map<GroupElement, List<GroupElement>> oddPowers;

        /**
         * Group this object stores precomputations for.
         */
        private Group group;


        public GroupPrecomputations(Group group) {
            // TODO: Using this enough for thread safety?
            oddPowers = new ConcurrentHashMap<>();
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

        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
        }

        /**
         * Delete all precomputations.
         */
        public void reset() {
            oddPowers = new ConcurrentHashMap<>();
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
