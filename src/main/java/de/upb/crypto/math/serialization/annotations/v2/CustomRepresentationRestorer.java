package de.upb.crypto.math.serialization.annotations.v2;

import de.upb.crypto.math.serialization.Representation;

import java.lang.reflect.Type;
import java.util.function.Function;

public class CustomRepresentationRestorer implements RepresentationRestorer {
    protected final Function<? super Representation, ?> restorer;

    public CustomRepresentationRestorer(Function<? super Representation, ?> restorer) {
        this.restorer = restorer;
    }

    @Override
    public Object recreateFromRepresentation(Type type, Representation repr) {
        return restorer.apply(repr);
    }
}
