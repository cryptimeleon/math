package de.upb.crypto.math.expressions;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public class BasicEvaluator {
    public GroupElement evaluateGroupElementExpression(GroupElementExpression expr) {
        return expr.evaluateNaive();
    }
}
