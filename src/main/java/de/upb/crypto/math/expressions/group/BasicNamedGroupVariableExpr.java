package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StringRepresentation;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class BasicNamedGroupVariableExpr implements GroupVariableExpr, Representable, UniqueByteRepresentable {
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
