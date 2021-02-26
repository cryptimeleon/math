package org.cryptimeleon.math.expressions.exponent;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StringRepresentation;

import java.util.Objects;

/**
 * A {@link ExponentVariableExpr} with a specific name.
 */
public final class BasicNamedExponentVariableExpr implements ExponentVariableExpr, Representable, UniqueByteRepresentable {

    /**
     * The name of this variable expression.
     */
    protected final String name;

    public BasicNamedExponentVariableExpr(String name) {
        this.name = name;
    }

    public BasicNamedExponentVariableExpr(Representation repr) {
        this(repr.str().get());
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicNamedExponentVariableExpr)) return false;
        BasicNamedExponentVariableExpr that = (BasicNamedExponentVariableExpr) o;
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
