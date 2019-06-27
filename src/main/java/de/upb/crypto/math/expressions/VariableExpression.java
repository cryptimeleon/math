package de.upb.crypto.math.expressions;

public class VariableExpression implements Expression {
    protected String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
