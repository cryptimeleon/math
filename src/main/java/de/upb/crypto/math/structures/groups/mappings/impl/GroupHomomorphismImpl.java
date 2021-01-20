package de.upb.crypto.math.structures.groups.mappings.impl;

import de.upb.crypto.math.structures.groups.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.util.function.Function;

/**
 * Interface for implementing a homomorphism between structures.
 */
public interface GroupHomomorphismImpl extends Function<GroupElementImpl, GroupElementImpl>, StandaloneRepresentable {
}
