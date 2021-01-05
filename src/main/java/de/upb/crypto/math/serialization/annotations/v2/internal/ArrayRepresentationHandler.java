package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * A handler for serializing/deserializing arrays.
 */
public class ArrayRepresentationHandler implements RepresentationHandler {
    /**
     * Handler for the array elements.
     */
    protected RepresentationHandler elementHandler;

    /**
     * Type of the array elements.
     */
    protected Class<?> elementType;

    public ArrayRepresentationHandler(RepresentationHandler elementHandler, Type typeofArray) {
        this.elementHandler = elementHandler;
        this.elementType = getTypeOfElements(typeofArray);
    }

    /**
     * Retrieves the class of the elements of the given array type.
     * @param typeOfArray the type of the array
     * @return the class of the array's elements
     */
    public static Class<?> getTypeOfElements(Type typeOfArray) {
        return ((Class<?>) typeOfArray).getComponentType();
    }

    /**
     * Checks whether this array handler can handle the given type, which is the case if it is an array type.
     * @param type the type to check
     * @return true if this handler can handle the given type, else false
     */
    public static boolean canHandle(Type type) {
        if (!(type instanceof Class))
            return false;

        Class<?> arrayType = (Class<?>) type;
        return arrayType.isArray();
    }

    @Override
    public Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer) {
        //Create array
        Object result = Array.newInstance(elementType, repr.list().size());

        //Restore elements
        int i=0;
        for (Representation inner : repr.list())
            Array.set(result, i++, elementHandler.deserializeFromRepresentation(inner, getRegisteredRestorer));

        return result;
    }

    @Override
    public Representation serializeToRepresentation(Object obj) {
        ListRepresentation repr = new ListRepresentation();

        for (int i=0; i<Array.getLength(obj); i++)
            repr.put(elementHandler.serializeToRepresentation(Array.get(obj, i)));

        return repr;
    }
}
