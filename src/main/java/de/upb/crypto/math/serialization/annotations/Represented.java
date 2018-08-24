package de.upb.crypto.math.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that describes how a field of an object should be serialized and deserialized. If the object can be deserialized without any additional information then it is called a PRIMITVE object. If, however, there is additional information,
 * like the parent structure, needed. Then the type of this object (via the TYPE value) and the name of the field of the parent structure can be set (via the structure value).
 *
 * @author Mirko Jürgens
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Represented {
    String recoveryMethod() default "";

    String structure() default "";
}
