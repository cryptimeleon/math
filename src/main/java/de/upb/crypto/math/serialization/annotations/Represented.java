package de.upb.crypto.math.serialization.annotations;

import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Enables automatic serialization and deserialization of the annotated field via {@link ReprUtil}.
 * <p>
 * Add this annotation to all fields that need to be part of the object's representation.
 * Then use {@code ReprUtil} to automatically serialize and deserialize your object according to these annotations.
 * @see ReprUtil
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Represented {

    /**
     * Specifies the name of the restorer to use to restore the fields value after having been serialized.
     * <p>
     * Usually, this is just a name like {@code "G1"}, meaning that the serialization and deserialization of this field
     * should be handled by the restorer registered with name {@code "G1"}.
     * <p>
     * If this is a {@code Collection} or a {@code Map} field, the restorer string must explain
     * how to restore its elements.
     * For example, for a list/set whose elements should be deserialized by restorer {@code "G1"},
     * you'd write {@code "[G1]"} (i.e. a list/set of elements each of which is restorable by restorer {@code "G1"}).
     * For a map, whose keys are handled by {@code "G1"} and whose values by {@code "G2"}, write {@code "G1 -> G2"}.
     * <p>
     * These can be combined, e.g., {@code "G1 -> [[G2]]"} for a map whose keys are handled by {@code "G1"}
     * and whose values are lists of lists of elements handled by {@code "G2"}.
     * <p>
     * You can use parentheses to ensure precedence, e.g., {@code "(G1 -> G2) -> G3"} is a map whose keys are maps
     * from G1 to G2.
     * <p>
     * If the type is simple (i.e. {@link StandaloneRepresentable}, {@code BigInteger}, {@code Integer},
     * {@code String}, {@code Boolean}, or {@code byte[]}), this value is ignored.
     * This is again true for nested expressions, e.g., {@code "FOO -> G2"} works for a map from {@code String} to
     * {@code GroupElement}, as the string {@code "FOO"} is simply ignored.
     * <p>
     * Keep in mind that the unwrapped variants such as {@code int} or {@code boolean} are not supported.
     */
    String restorer() default "";
}
