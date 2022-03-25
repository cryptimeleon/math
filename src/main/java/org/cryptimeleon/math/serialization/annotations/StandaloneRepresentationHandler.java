package org.cryptimeleon.math.serialization.annotations;

import org.cryptimeleon.math.misc.BigIntegerTools;
import org.cryptimeleon.math.serialization.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Function;

/**
 * Handles serialization/deserialization of the representation of {@link StandaloneRepresentable} implementers
 * and some other simple types.
 */
class StandaloneRepresentationHandler implements RepresentationHandler {

    // it may be temping to add int.class etc. here, but it doesn't work because the ReprUtil assumes that everything
    // that's not null is already set (and int is auto-initialized with 0)
    private static final Class<?>[] supportedTypes = new Class[]{
            StandaloneRepresentable.class, BigInteger.class, Integer.class, Long.class, String.class, Boolean.class,
            byte[].class, UUID.class, Enum.class
    };
    /**
     * Type of the represented object.
     */
    protected Class<?> type;

    public StandaloneRepresentationHandler(Class<?> type) {
        this.type = type;
    }

    /**
     * Checks whether this handler can handle objects of the given type.
     * @param type the type to check
     * @return true if this handler can handle the given type, else false
     */
    public static boolean canHandle(Type type) {
        if (!(type instanceof Class))
            return false;
        Class<?> clazz = ((Class<?>) type);
        for (Class<?> supported : supportedTypes)
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
            if (repr instanceof RepresentableRepresentation
                    && type.isAssignableFrom(Class.forName(repr.repr().getRepresentedTypeName()) )) {
                return repr.repr().recreateRepresentable();
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Don't know how to recreate " + repr.repr().getRepresentedTypeName()
                    + " - Class not found.", e);
        }

        if (type.isAssignableFrom(BigInteger.class) && repr instanceof BigIntegerRepresentation) {
            return repr.bigInt().get();
        }

        if (type.isAssignableFrom(Integer.class) && repr instanceof BigIntegerRepresentation) {
            return BigIntegerTools.getExactInt(repr.bigInt().get());
        }

        if (type.isAssignableFrom(Long.class) && repr instanceof BigIntegerRepresentation) {
            return BigIntegerTools.getExactLong(repr.bigInt().get());
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

        if (type.isAssignableFrom(UUID.class) && repr instanceof StringRepresentation) {
            return UUID.fromString(repr.str().get());
        }

        throw new IllegalArgumentException("Don't know how to recreate " + type.getName() + " from a "
                + repr.getClass().getName());
    }

    @Override
    public Representation serializeToRepresentation(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Enum) {
            Enum<?> enumValue = (Enum<?>) value;
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

        if (value instanceof Long) {
            Long longValue = (Long) value;
            return new BigIntegerRepresentation(longValue);
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

        if (value instanceof UUID) {
            UUID uuid = (UUID) value;
            return new StringRepresentation(uuid.toString());
        }

        throw new IllegalArgumentException("Do not know how to handle object of type " + value.getClass().getName()
                + ". You may have to add an explicit 'restorer' argument to the @Represented annotation");
    }

}
