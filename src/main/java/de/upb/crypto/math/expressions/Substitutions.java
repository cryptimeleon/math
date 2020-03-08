package de.upb.crypto.math.expressions;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;

/**
 * A class that specifies a String -> Expression mapping that can be used to substitute Variables in Expressions.
 */
public interface Substitutions {
    /**
     * Defines how to replace a GroupVariableExpr with another expression.
     * @param key name of the variable to replace
     * @return an expression to replace the variable with, or null if nothing shall be done for that variable.
     */
    GroupElementExpression getSubstitutionForGroupElementVariable(String key);

    /**
     * Defines how to replace an ExponentVariableExpr with another expression.
     * @param key name of the variable to replace
     * @return an expression to replace the variable with, or null if nothing shall be done for that variable.
     */
    ExponentExpr getSubstitutionForExponentVariable(String key);


    /**
     * Defines how to replace a BoolVariableExpr with another expression.
     * @param key name of the variable to replace
     * @return an expression to replace the variable with, or null if nothing shall be done for that variable.
     */
    BooleanExpression getSubstitutionForBooleanVariable(String key);
}
