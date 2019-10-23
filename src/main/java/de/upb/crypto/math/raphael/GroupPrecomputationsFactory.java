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
         */
        @Represented(restorer = "G -> BigInt -> G")
        private Map<GroupElement, Map<BigInteger, GroupElement>> powers;

        /**
         * Group this object stores precomputations for.
         * TODO: How to serialize this?
         */
        @Represented()
        private Group group;

        private GroupPrecomputations(Group group) {
            // TODO: Using this enough for thread safety?
            powers = new ConcurrentHashMap<>();
            this.group = group;
        }

        /**
         * Enter computed power in precomputation table.
         *
         * @param base The base of the power.
         * @param exponent The exponent.
         * @param result Result of base^exponent.
         */
        public void addPower(GroupElement base, BigInteger exponent, GroupElement result) {
            Map<BigInteger, GroupElement> baseEntry =
                    powers.computeIfAbsent(base, k -> new HashMap<>());
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
         * @param precomputeIfMissing Whether to precompute if power is not precomputed yet.
         * @return Power base^exponent.
         */
        public GroupElement getPower(GroupElement base, BigInteger exponent,
                                     boolean precomputeIfMissing) {
            Map<BigInteger, GroupElement> baseEntry =
                    powers.computeIfAbsent(base, k -> new HashMap<>());
            GroupElement result = baseEntry.get(exponent);
            if (result == null && precomputeIfMissing) {
                result = base.pow(exponent);
                baseEntry.put(exponent, result);
                return result;
            } else if (result == null) {
                // TODO: Is this right behaviour if power is missing and should not precompute
                //  ourselves?
                throw new IllegalStateException("Missing power.");
            }
            return result;
        }

        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
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
     * Deserialize a group's precomputations from the representation and add to store. Makes
     * sure that a precomputation object only exists once per group. So if
     * there would be multiple objects after deserialization, their precomputed values are
     * simply combined in a single object.
     */
    public static void addFromRepresentation(Representation repr) {
        // Need to reconstruct the group of the precomputation first to check if already exists.
        System.out.println(repr);
    }
}
