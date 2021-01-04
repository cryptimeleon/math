package de.upb.crypto.math.factory;

import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;

import java.util.List;

/**
 * Provides a concrete instance of a {@link BilinearGroup} given a {@code security parameter} and an
 * {@link BilinearGroupRequirement} object specifying the desired properties for the provided group.
 * <p>
 * The contract is that the provider should check the requirements internally and throws a suitable exception (e.g., a
 * {@link UnsupportedOperationException}) in case the provider does not fulfill the specified requirements.
 * In particular, if the call of {@link #checkRequirements(int, BilinearGroupRequirement)} returns true, the call of
 * {@link #provideBilinearGroup(int, BilinearGroupRequirement)} must not fail!
 * <p>
 * In general, {@link BilinearGroupProvider}s should be registered at {@link BilinearGroupFactory} using
 * {@link BilinearGroupFactory#registerProvider(List)} which chooses the most suitable provider for the group
 * generation.
 *
 * @author Denis Diemert
 */
public interface BilinearGroupProvider {
    /**
     * Provides a {@link BilinearGroup} for the given security parameter and requirements.
     * <p>
     * Before the group is provided it should be checked whether the provider meets the
     * {@code requirements} stated by the user.
     *
     * @param securityParameter supposed security of discrete logarithm of the groups G1, G2, GT
     *                          of the bilinear group provided (in bits)
     * @param requirements      requirements the provided bilinear group needs to fulfill
     * @return a concrete instance of a {@link BilinearGroup} meeting the given parameters
     */
    BilinearGroup provideBilinearGroup(int securityParameter, BilinearGroupRequirement requirements);

    /**
     * Provides a {@link BilinearGroupImpl} for the given security parameter and requirements.
     * <p>
     * Before the group is provided it should be checked whether the provider meets the
     * {@code requirements} stated by the user.
     * <p>
     * Note that this returns a {@link BilinearGroupImpl}, which usually should not be used directly.
     * Instead, use {@link #provideBilinearGroup(int, BilinearGroupRequirement)} or wrap the result of
     * this method into a {@code BilinearGroup} yourself, such as {@link BasicBilinearGroup} or
     * {@link LazyBilinearGroup}.
     *
     * @param securityParameter supposed security of discrete logarithm of the groups G1, G2, GT
     *                          of the bilinear group provided (in bits)
     * @param requirements      requirements the provided bilinear group need to fulfill
     * @return a concrete instance of a {@link BilinearGroupImpl} meeting the given parameters
     */
    BilinearGroupImpl provideBilinearGroupImpl(int securityParameter, BilinearGroupRequirement requirements);

    /**
     * Validates that the given requirements can be fulfilled by this provider.
     *
     * @param requirements      requirements to be checked
     * @param securityParameter certain groups, like standard curves, can only provide a fixed security parameter.
     *                          Therefore, this should
     *                          also be checked in this method. If the provider is not restrited to any security
     *                          parameters, the parameter
     *                          can be ignored in the implementation.
     * @return true iff the bilinear group provided by {@link #provideBilinearGroup(int, BilinearGroupRequirement)}
     *         meets the requirements defined by {@code requirements}.
     */
    boolean checkRequirements(int securityParameter, BilinearGroupRequirement requirements);
}
