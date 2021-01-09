package de.upb.crypto.math.expressions;

@FunctionalInterface
public interface Substitution {
    /**
     * Indicates what the given expr shall be replaced with.
     *
     * @param expr the candidate expression to replace
     * @return the new Expression to replace expr with, or null to indicate no substitution shall be applied
     */
    Expression getSubstitution(VariableExpression expr);

    /**
     * Returns a substitution that gets its entries from this substitution or the other.
     * The resulting Substitution will fail with Exception if both this Substitution and the other return non-null on the same input.
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
     * Returns a substitution that gets falls back to "other" substitution if this substitution doesn't have a value.
     */
    default Substitution join(Substitution other) {
        return expr -> {
            Expression ours = this.getSubstitution(expr);
            if (ours == null)
                return other.getSubstitution(expr);
            return ours;
        };
    }

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
