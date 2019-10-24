package de.upb.crypto.math.serialization.annotations.v2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
/**
 * Part of the framework to simplify serialization and deserialization of objects.
 * Add this annotation to all fields that need to be part of the object's Representation.
 * Then use ReprUtil to automatically serialize and deserialize your object according to these annotations.
 * @see ReprUtil
 */
public @interface Represented {
    /**
     * The handler for the annotated field (corresponds to a name you put into ReprUtil.register or a field name in your class).
     * Usually, this is just a name like "G1", meaning that the serialization and deserialization of this field should be handled by "G1".
     *
     * If this is a Collection or a Map field, the restorer String must explain how to restore its elements.
     * For example, for a list/set whose elements should be deserialized by G1, you'd write "[G1]" (i.e. a list of G1 elements).
     * For a map, whose keys are handled by G1 and whose values by G2, write "G1 -> G2".
     *
     * These can be combined, e.g., "G1 -> [[G2]]" for a map whose keys are handled by G1 and whose values are lists of lists of G2.
     * You can use parentheses to ensure precedence, e.g., "(G1 -> G2) -> G3" is a map whose keys are maps from G1 to G2.
     *
     * If the type is simple (i.e. StandaloneRepresentable, BigInteger, Integer, String, Boolean, or byte[]), this value is ignored.
     * This is again true for nested expressions, e.g., "FOO -> G2" works for a Map from String to GroupElement, as the String "FOO" is simply ignored.
     */
    String restorer() default "";
}
