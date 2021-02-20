package org.cryptimeleon.math.serialization.annotations;

import org.cryptimeleon.math.serialization.Representation;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * A class for custom {@code RepresentationRestorer}s implemented via a restorer function.
 */
public class CustomRepresentationRestorer implements RepresentationRestorer {
    /**
     * Maps a representation back to the corresponding object (restores the object).
     */
    protected final Function<? super Representation, ?> restorer;

    /**
     * Creates a restorer using the given restorer function.
     * @param restorer the restorer function to use
     */
    public CustomRepresentationRestorer(Function<? super Representation, ?> restorer) {
        this.restorer = restorer;
    }

    @Override
    public Object recreateFromRepresentation(Type type, Representation repr) {
        return restorer.apply(repr);
    }
}
