package de.upb.crypto.math.interfaces.mappings;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.util.function.Function;

/**
 * A homomorphism between structures.
 */
public interface GroupHomomorphism extends Function<GroupElement, GroupElement>, StandaloneRepresentable {
}
