package de.upb.crypto.math.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A set of homogenic elements that are all restoreable in the same way (e.g. elements of the same group)
 *
 * @author Lukas Eilers
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface RepresentedSet {
    Represented elementRestorer();
}
