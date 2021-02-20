package org.cryptimeleon.math.structures.groups.mappings.impl;

import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;

import java.util.function.Function;

/**
 * Interface for implementing a homomorphism between structures.
 */
public interface GroupHomomorphismImpl extends Function<GroupElementImpl, GroupElementImpl>, StandaloneRepresentable {
}
