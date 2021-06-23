package org.cryptimeleon.math.serialization.annotations;

import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Handles representations that depend on some {@link RepresentationRestorer} in order to be recreated.
 */
class DependentRepresentationHandler implements RepresentationHandler {
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
        if (repr == null)
            return null;
        return getRegisteredRestorer.apply(restorerString).restoreFromRepresentation(typeToRestore, repr);
    }

    @Override
    public Representation serializeToRepresentation(Object object) {
        if (object == null)
            return null;
        return ((Representable) object).getRepresentation();
    }
}
