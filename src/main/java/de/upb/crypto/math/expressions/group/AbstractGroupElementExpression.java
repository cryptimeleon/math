package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

/**
 * An {@link Expression} that evaluates to a {@link GroupElement}.
 */
public abstract class AbstractGroupElementExpression implements GroupElementExpression {
    /**
     * This expression evaluates to an element of this group.
     */
    protected final Group group;

    public AbstractGroupElementExpression() {this(null);}

    public AbstractGroupElementExpression(Group group) {
        this.group = group;
    }

    /**
     * Returns the group such that this expression evaluates to an element of this group, or null if group is unknown
     * (for example, if expression consists only of variables).
     */
    @Override
    public Group getGroup() {
        return group;
    }

    protected BigInteger getGroupOrderIfKnown() {
        try {
            return getGroup().size();
        } catch (UnsupportedOperationException unknownSizeException) {
            return null;
        }
    }
}
