package de.upb.crypto.math.serialization.annotations.internal;

import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.RepresentationRestorer;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Handles representations that depend on some {@link RepresentationRestorer} in order to be recreated.
 */
public class DependentRepresentationHandler implements RepresentationHandler {
    /**
     * Restorer string indicating the {@code RepresentationRestorer} to use.
     */
    protected String restorerString;

    /**
     * What type the restored object should be.
     */
    protected Type typeToRestore;

    public DependentRepresentationHandler(String restorerString, Type typeToRestore) {
        this.restorerString = restorerString;
        this.typeToRestore = typeToRestore;
    }

    /**
     * Checks whether this handler can handle the given type, which is the case if it implements
     * {@link Representable}.
     * @param type the type to check
     * @return true if this handler can handle the given type, else false
     */
    public static boolean canHandle(Type type) {
        return type instanceof Class && Representable.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer) {
        return getRegisteredRestorer.apply(restorerString).recreateFromRepresentation(typeToRestore, repr);
    }

    @Override
    public Representation serializeToRepresentation(Object object) {
        return ((Representable) object).getRepresentation();
    }
}
