package org.cryptimeleon.math.hash.annotations;

import org.cryptimeleon.math.hash.UniqueByteRepresentable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field supports a unique byte representation and can be accumulated automatically by the
 * {@link AnnotatedUbrUtil}.
 * <p>
 * To work correctly, the annotated field type must either implement {@link UniqueByteRepresentable}
 * or must be one of {@code byte[]}, {@code Byte}, {@code String}, {@code Integer}, {@code BigInteger},
 * {@code List}, {@code Array}, {@code Enum}, {@code Map} or {@code Set}.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface UniqueByteRepresented {
}
