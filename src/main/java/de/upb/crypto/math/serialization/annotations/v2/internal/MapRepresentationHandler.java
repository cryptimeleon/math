package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.MapRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class MapRepresentationHandler implements RepresentationHandler {
    protected RepresentationHandler keyHandler, valueHandler;
    protected Class mapType;
    protected Type keyType;
    protected Type valueType;

    public MapRepresentationHandler(RepresentationHandler keyHandler, RepresentationHandler valueHandler, Type mapType) {
        this.keyHandler = keyHandler;
        this.valueHandler = valueHandler;
        this.mapType = (Class) ((ParameterizedType) mapType).getRawType();
        this.keyType = getKeyType(mapType);
        this.valueType = getValueType(mapType);
    }

    public static Type getKeyType(Type mapType) {
        Type[] typeArguments = ((ParameterizedType) mapType).getActualTypeArguments();
        if (typeArguments.length != 2) {
            throw new IllegalArgumentException("Can only handle maps with two generic type arguments");
        }
        return typeArguments[0];
    }

    public static Type getValueType(Type mapType) {
        Type[] typeArguments = ((ParameterizedType) mapType).getActualTypeArguments();
        if (typeArguments.length != 2) {
            throw new IllegalArgumentException("Can only handle maps with two generic type arguments");
        }
        return typeArguments[1];
    }

    public static boolean canHandle(Type mapType) { //handles Map<anything>.
        if (!(mapType instanceof ParameterizedType))
            return false;

        Type rawType = ((ParameterizedType) mapType).getRawType();

        if (!(rawType instanceof Class))
            return false;

        if (!Map.class.isAssignableFrom((Class) rawType))
            return false;

        try { //Check if generic type argument is okay
            getKeyType(mapType);
            getValueType(mapType);
        } catch (IllegalArgumentException e) {
            return false;
        }

        //Check if the collection has a default constructor or is an interface.
        try {
            ((Class) rawType).getConstructor();
        } catch (NoSuchMethodException e) {
            return ((Class) rawType).isInterface() && ((Class) rawType).isAssignableFrom(LinkedHashMap.class);
        }

        return true;
    }

    @Override
    public Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer) {
        Map result = null;

        //Try to call default constructor to create collection.
        try {
            result = (Map) mapType.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) { //fall back to LinkedHashMap (e.g., if the field type is just Map<>)
            if (mapType.isAssignableFrom(LinkedHashMap.class))
                result = new LinkedHashMap();
        }
        if (result == null)
            throw new RuntimeException("Cannot instantiate type "+ mapType.getName());

        //Restore elements
        for (Map.Entry<Representation, Representation> entry : repr.map())
            result.put(keyHandler.deserializeFromRepresentation(entry.getKey(), getRegisteredRestorer),
                    valueHandler.deserializeFromRepresentation(entry.getValue(), getRegisteredRestorer));

        return result;
    }

    @Override
    public Representation serializeToRepresentation(Object obj) {
        if (!(obj instanceof Map))
            throw new IllegalArgumentException("Cannot handle representation of "+obj.getClass().getName());
        MapRepresentation repr = new MapRepresentation();
        ((Map) obj).forEach((k, v) -> repr.put(keyHandler.serializeToRepresentation(k), valueHandler.serializeToRepresentation(v)));
        return repr;
    }
}
