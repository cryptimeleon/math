package de.upb.crypto.math.structures.groups.mappings;

import de.upb.crypto.math.structures.groups.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.util.function.Function;

/**
 * A homomorphism between structures.
 * <p>
 * Usually used as a wrapper around a {@link GroupHomomorphismImpl} to offer additional evaluation capabilities.
 * You should use {@link GroupHomomorphismImpl} for your implementation instead.
 */
public interface GroupHomomorphism extends Function<GroupElement, GroupElement>, StandaloneRepresentable {
}
