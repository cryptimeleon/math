package org.cryptimeleon.math.structures.groups.mappings;

import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.mappings.impl.GroupHomomorphismImpl;

import java.util.function.Function;

/**
 * A homomorphism between structures.
 * <p>
 * Usually used as a wrapper around a {@link GroupHomomorphismImpl} to offer additional evaluation capabilities.
 * You should use {@link GroupHomomorphismImpl} for your implementation instead.
 */
public interface GroupHomomorphism extends Function<GroupElement, GroupElement>, StandaloneRepresentable {
}
