package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentEmptyExpr implements ExponentExpr {
    @Override
    public BigInteger evaluate() {
        return BigInteger.ONE;
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return zn.getOneElement();
    }

    @Override
    public ExponentEmptyExpr substitute(Substitutions variableValues) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        //Intentionally empty.
    }

    @Override
    public ExponentExpr precompute() {
        return this;
    }
}
