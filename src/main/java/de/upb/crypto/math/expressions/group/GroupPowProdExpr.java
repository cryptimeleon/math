package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.Iterator;


/**
 * An expression of the form Π g_i^(x_i).
 *
 * When iterating over these expressions, you'll get the factors right-to-left.
 */
public class GroupPowProdExpr implements GroupElementExpression, Iterable<GroupPowProdExpr> {
    protected GroupPowProdExpr lhs; //expression is lhs * base^exponent. For lhs == null, this is just base^exponent.
    protected Group group;
    protected GroupElementExpression base;
    protected ExponentExpr exponent;

    public GroupPowProdExpr(GroupPowProdExpr lhs, GroupElementExpression base, ExponentExpr exponent) {
        this.lhs = lhs;
        this.group = lhs.group;
        this.base = base;
        this.exponent = exponent;
    }

    public GroupPowProdExpr(Group group, GroupElementExpression base, ExponentExpr exponent) {
        this.lhs = null;
        this.group = group;
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public GroupElement evaluate() {
        return group.evaluate(this);
    }

    public GroupPowProdExpr getLhs() {
        return lhs;
    }

    public GroupElementExpression getBase() {
        return base;
    }

    public ExponentExpr getExponent() {
        return exponent;
    }

    @Override
    @Nonnull
    public Iterator<GroupPowProdExpr> iterator() {
        return new Iterator<GroupPowProdExpr>() {
            GroupPowProdExpr current = GroupPowProdExpr.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public GroupPowProdExpr next() {
                GroupPowProdExpr tmp = current;
                current = current.getLhs();
                return tmp;
            }
        };
    }
}
