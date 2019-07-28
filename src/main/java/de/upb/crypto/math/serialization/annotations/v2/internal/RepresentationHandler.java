package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.util.function.Function;

/**
 * Interface internally used within ReprUtil
 */
public interface RepresentationHandler {
    Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer);
    Representation serializeToRepresentation(Object object);
}
