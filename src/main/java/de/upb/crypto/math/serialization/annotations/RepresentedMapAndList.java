package de.upb.crypto.math.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface RepresentedMapAndList {
    Represented keyRestorer();

    RepresentedList valueRestorer();
}
