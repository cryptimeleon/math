package de.upb.crypto.math.serialization.annotations.v2.internal;

import de.upb.crypto.math.serialization.*;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * Takes care of
 * 1) StandaloneRepresentable
 * 2) Some basic types (see static supportedTypes variable).
 */
public class StandaloneRepresentationHandler implements RepresentationHandler {
    private static Class[] supportedTypes = new Class[] {StandaloneRepresentable.class, BigInteger.class, Integer.class, String.class, Boolean.class, byte[].class, Enum.class}; //it may be temping to add int.class etc. here, but it doesn't work because the ReprUtil assumes that everything that's not null is already set (and int is auto-initialized with 0)
    protected Class type;

    public StandaloneRepresentationHandler(Class type) {
        this.type = type;
    }

    public static boolean canHandle(Type type) {
        if (!(type instanceof Class))
            return false;
        Class clazz = ((Class) type);
        for (Class supported : supportedTypes)
            if (supported.isAssignableFrom(clazz))
                return true;
        return false;
    }

    @Override
    public Object deserializeFromRepresentation(Representation repr, Function<String, RepresentationRestorer> getRegisteredRestorer) {
        if (repr == null) {
            return null;
        }

        try {
            if (repr instanceof RepresentableRepresentation && type.isAssignableFrom(Class.forName(repr.repr().getRepresentedTypeName()) )) {
                return repr.repr().recreateRepresentable();
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Don't know how to recreate " + repr.repr().getRepresentedTypeName() + " - Class not found.", e);
        }

        if (type.isAssignableFrom(BigInteger.class) && repr instanceof BigIntegerRepresentation) {
            return repr.bigInt().get();
        }

        if (type.isAssignableFrom(Integer.class) && repr instanceof BigIntegerRepresentation) {
            return repr.bigInt().get().intValueExact();
        }

        if (type.isAssignableFrom(String.class) && repr instanceof StringRepresentation) {
            return repr.str().get();
        }

        if (type.isAssignableFrom(Boolean.class) && repr instanceof BigIntegerRepresentation) {
            return !repr.bigInt().get().equals(BigInteger.ZERO);
        }

        if (type.isAssignableFrom(byte[].class) && repr instanceof ByteArrayRepresentation) {
            return repr.bytes().get();
        }

        throw new IllegalArgumentException("Don't know how to recreate " + type.getName() + " from a "+repr.getClass().getName());
    }

    @Override
    public Representation serializeToRepresentation(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Enum) {
            Enum enumValue = (Enum) value;
            return new RepresentableRepresentation(enumValue);
        }

        if (value instanceof StandaloneRepresentable) {
            Representable repr = (Representable) value;
            return new RepresentableRepresentation(repr);
        }

        if (value instanceof Representable) {
            Representable repr = (Representable) value;
            return repr.getRepresentation();
        }

        if (value instanceof BigInteger) {
            BigInteger bigInt = (BigInteger) value;
            return new BigIntegerRepresentation(bigInt);
        }

        if (value instanceof Integer) {
            Integer integer = (Integer) value;
            return new BigIntegerRepresentation(integer);
        }

        if (value instanceof String) {
            String string = (String) value;
            return new StringRepresentation(string);
        }

        if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            return new BigIntegerRepresentation(bool ? 1 : 0);
        }

        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            return new ByteArrayRepresentation(bytes);
        }

        throw new IllegalArgumentException("Object of type "+value.getClass().getName()+" is not StandaloneRepresentable" +
                "(you may have to add an explicit 'handler' argument to the @Represented annotation)");
    }

}
