package org.cryptimeleon.math.expressions.group;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StringRepresentation;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A {@link GroupVariableExpr} with a specific name.
 */
public final class BasicNamedGroupVariableExpr implements GroupVariableExpr, Representable, UniqueByteRepresentable {

    /**
     * The name of this variable expression.
     */
    protected final String name;

    public BasicNamedGroupVariableExpr(@Nonnull String name) {
        this.name = name;
    }

    public BasicNamedGroupVariableExpr(Representation repr) {
        this(repr.str().get());
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicNamedGroupVariableExpr)) return false;
        BasicNamedGroupVariableExpr that = (BasicNamedGroupVariableExpr) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public Representation getRepresentation() {
        return new StringRepresentation(name);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        accumulator.append(name);
        return accumulator;
    }
}
