package org.cryptimeleon.math.expressions.group;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;

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
