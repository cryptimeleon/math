package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.Function;

public class ArrayRepresentationHandler implements RepresentationHandler {
    protected RepresentationHandler elementHandler;
    protected Class elementType;

    public ArrayRepresentationHandler(RepresentationHandler elementHandler, Type typeofArray) {
        this.elementHandler = elementHandler;
        this.elementType = getTypeOfElements(typeofArray);
    }

    public static Class getTypeOfElements(Type typeOfArray) {
        return ((Class) typeOfArray).getComponentType();
    }

    public static boolean canHandle(Type type) {
        if (!(type instanceof Class))
            return false;

        Class arrayType = (Class) type;
        if (!arrayType.isArray())
            return false;

        return true;
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
