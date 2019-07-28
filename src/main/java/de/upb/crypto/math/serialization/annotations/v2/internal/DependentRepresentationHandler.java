package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Handles representations that depend on some RepresentationRestorer in order to be recreated.
 */
public class DependentRepresentationHandler implements RepresentationHandler {
    protected String restorerName;
    protected Type typeToRestore;

    public DependentRepresentationHandler(String restorerName, Type typeToRestore) {
        this.restorerName = restorerName;
        this.typeToRestore = typeToRestore;
    }

    public static boolean canHandle(Type type) {
        return type instanceof Class && Representable.class.isAssignableFrom((Class) type);
    }

    @Override
    public Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer) {
        return getRegisteredRestorer.apply(restorerName).recreateFromRepresentation(typeToRestore, repr);
    }

    @Override
    public Representation serializeToRepresentation(Object object) {
        return ((Representable) object).getRepresentation();
    }
}
