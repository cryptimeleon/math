package de.upb.crypto.math.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A map of homogenous elements that are all restorable in the same way (e.g. elements of the same group)
 *
 * @author Mirko Jürgens
 * @deprecated Superseded by the v2 framework
 */
@Deprecated
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface RepresentedMap {
    Represented keyRestorer();

    Represented valueRestorer();

}
