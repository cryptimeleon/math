package de.upb.crypto.math.interfaces.mappings.impl;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.util.function.Function;

/**
 * A homomorphism between structures
 */
public interface GroupHomomorphismImpl extends Function<GroupElementImpl, GroupElementImpl>, StandaloneRepresentable {
}
