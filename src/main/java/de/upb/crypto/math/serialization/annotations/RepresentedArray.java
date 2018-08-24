package de.upb.crypto.math.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A list of homogenic elements that are all restoreable in the same way (e.g. elements of the same group)
 *
 * @author Mirko Jürgens
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface RepresentedArray {
    Represented elementRestorer();

}
