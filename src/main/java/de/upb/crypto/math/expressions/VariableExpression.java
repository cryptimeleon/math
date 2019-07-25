package de.upb.crypto.math.expressions;

import java.util.Map;

public abstract class VariableExpression implements Expression {
    protected String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
