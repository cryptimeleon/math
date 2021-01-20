package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.hash.ByteAccumulator;
import de.upb.crypto.math.hash.UniqueByteRepresentable;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StringRepresentation;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A {@link BoolVariableExpr} with a specific name.
 */
public final class BasicNamedBoolVariableExpr implements BoolVariableExpr, Representable, UniqueByteRepresentable {

    /**
     * The name of this variable expression.
     */
    protected final String name;

    public BasicNamedBoolVariableExpr(@Nonnull String name) {
        this.name = name;
    }

    public BasicNamedBoolVariableExpr(Representation repr) {
        this(repr.str().get());
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicNamedBoolVariableExpr)) return false;
        BasicNamedBoolVariableExpr that = (BasicNamedBoolVariableExpr) o;
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
