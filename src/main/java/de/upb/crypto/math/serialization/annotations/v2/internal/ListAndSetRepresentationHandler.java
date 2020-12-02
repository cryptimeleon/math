package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * A handler for serializing/deserializing {@link List} and {@link Set} instances.
 */
public class ListAndSetRepresentationHandler implements RepresentationHandler {
    private static final Class[] supportedFallbackClasses = new Class[] {ArrayList.class, HashSet.class};
    private final RepresentationHandler elementHandler;
    private final Class collectionType;
    private final Type elementType;

    public ListAndSetRepresentationHandler(RepresentationHandler elementHandler, Type collectionType) {
        this.elementHandler = elementHandler;
        this.collectionType = (Class) ((ParameterizedType) collectionType).getRawType();
        this.elementType = getElementType(collectionType);
    }

    /**
     * Retrieves the type of the elements of the given collection type.
     * @param collectionType the type of the collection
     * @return the type of the elements of the collection type
     */
    public static Type getElementType(Type collectionType) {
        Type[] typeArguments = ((ParameterizedType) collectionType).getActualTypeArguments();
        if (typeArguments.length != 1) {
            throw new IllegalArgumentException("Cannot handle collections with more than one generic type");
        }
        return typeArguments[0];
    }

    /**
     * Checks whether this handler can handle lists/sets of the given type.
     * @param collectionType the type to check
     * @return true if this handler can handle the given type, else false
     */
    public static boolean canHandle(Type collectionType) { //handles List|Set<anything>.
        if (!(collectionType instanceof ParameterizedType))
            return false;

        Type rawType = ((ParameterizedType) collectionType).getRawType();

        if (!(rawType instanceof Class))
            return false;

        if (!List.class.isAssignableFrom((Class) rawType) && !Set.class.isAssignableFrom((Class) rawType))
            return false;

        try { //Check if generic type argument is okay
            getElementType(collectionType);
        } catch (IllegalArgumentException e) {
            return false;
        }

        //Check if the collection has a default constructor or is an interface.
        try {
            ((Class) rawType).getConstructor();
        } catch (NoSuchMethodException e) {
            return ((Class) rawType).isInterface() && Arrays.stream(supportedFallbackClasses).anyMatch(fallback -> ((Class) rawType).isAssignableFrom(fallback));
        }

        return true;
    }

    @Override
    public Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer) {
        Collection result = null;

        //Try to call default constructor to create collection.
        try {
            result = (Collection) collectionType.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) { //fall back to ArrayList (e.g., if the field type is just List<>)
            for (Class fallback : supportedFallbackClasses) {
                try {
                    if (collectionType.isAssignableFrom(fallback))
                        result = (Collection) fallback.getConstructor().newInstance();
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException("Cannot instantiate type "+collectionType.getName());
                }
            }
        }
        if (result == null)
            throw new RuntimeException("Cannot instantiate type "+collectionType.getName());

        //Restore elements
        for (Representation inner : repr.list())
            result.add(elementHandler.deserializeFromRepresentation(inner, getRegisteredRestorer));

        return result;
    }

    @Override
    public Representation serializeToRepresentation(Object obj) {
        if (!(obj instanceof List || obj instanceof Set))
            throw new IllegalArgumentException("Cannot handle representation of "+obj.getClass().getName());
        ListRepresentation repr = new ListRepresentation();
        for (Object inner : (Iterable) obj) //TODO sort elements in case of a Set type to avoid leaking something. Or is that done by the Converter? No, cannot.
            repr.put(elementHandler.serializeToRepresentation(inner));
        return repr;
    }
}
