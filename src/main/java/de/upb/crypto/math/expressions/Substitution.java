package de.upb.crypto.math.expressions;

@FunctionalInterface
public interface Substitution {

    /**
     * Indicates what the given expr shall be replaced with.
     *
     * @param expr the candidate expression to replace
     * @return the new expression to replace expr with, or null to indicate no substitution shall be applied
     */
    Expression getSubstitution(VariableExpression expr);

    /**
     * Returns a substitution that gets its entries from this substitution or the given one.
     * The resulting substitution will fail with {@code IllegalArgumentException} if both this substitution
     * and the given one return non-null on the same input, i.e. they are not disjoint.
     *
     * @param other the substitution to execute the disjoint join with
     */
    default Substitution joinDisjoint(Substitution other) {
        return expr -> {
            Expression ours = this.getSubstitution(expr);
            Expression theirs = other.getSubstitution(expr);

            if (ours != null && theirs != null)
                throw new IllegalArgumentException("Substitutions are not disjoint");

            return theirs == null ? ours : theirs;
        };
    }

    /**
     * Returns a substitution that falls back to the given substitution if this substitution doesn't have a value.
     *
     * @param other the substitution to fall back to
     */
    default Substitution join(Substitution other) {
        return expr -> {
            Expression ours = this.getSubstitution(expr);
            if (ours == null)
                return other.getSubstitution(expr);
            return ours;
        };
    }

    /**
     * Returns a substitution that falls back to the given substitutions if this substitution doesn't have a value.
     * The fallback substitutions are considered in the given order.
     *
     * @param subs the substitutions to fall back to
     */
    static Substitution join(Substitution... subs) {
        return expr -> {
            for (Substitution sub : subs) {
                Expression result = sub.getSubstitution(expr);
                if (result != null)
                    return result;
            }
            return null;
        };
    }

    /**
     * Returns a substitution that falls back to the given substitutions if this substitution doesn't have a value
     * and additionally ignores any {@code NullPointerException}s that occur.
     * The fallback substitutions are considered in the given order.
     *
     * @param subs the substitutions to fall back to
     */
    static Substitution joinAndIgnoreNullpointers(Substitution... subs) {
        return expr -> {
            for (Substitution sub : subs) {
                try {
                    Expression result = sub.getSubstitution(expr);
                    if (result != null)
                        return result;
                } catch (NullPointerException ignored) {}
            }
            return null;
        };
    }


    /**
     * Returns a new substitution that is constructed by executing a disjoint join on this substitution and the
     * given ones. Disjoint means that the resulting substitution throws a {@code IllegalArgumentException} if
     * more than one of the base substitutions has a value for a given expression.
     *
     * @see #joinDisjoint(Substitution)
     *
     * @param subs the substitutions to join with
     */
    static Substitution joinDisjoint(Substitution... subs) {
        return expr -> {
            int numResults = 0;
            Expression result = null;
            for (Substitution sub : subs) {
                result = sub.getSubstitution(expr);
                numResults++;
            }

            if (numResults > 1)
                throw new IllegalArgumentException("Substitutions are not disjoint");

            return result;
        };
    }
}
