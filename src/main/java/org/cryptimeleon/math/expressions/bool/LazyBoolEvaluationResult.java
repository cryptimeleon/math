package org.cryptimeleon.math.expressions.bool;

/**
 * Placeholder for the evaluation result of a BooleanExpression while it's being evaluated in the background.
 */
public abstract class LazyBoolEvaluationResult {
    public static final LazyBoolEvaluationResult TRUE = new LazyBoolEvaluationResult() {
        @Override
        public boolean getResult() {
            return true;
        }

        @Override
        public boolean isResultKnown() {
            return true;
        }
    };

    public static final LazyBoolEvaluationResult FALSE = new LazyBoolEvaluationResult() {
        @Override
        public boolean getResult() {
            return false;
        }

        @Override
        public boolean isResultKnown() {
            return true;
        }
    };

    /**
     * Returns the result of evaluation (call may block until it's been computed)
     */
    public abstract boolean getResult();

    /**
     * For optimization: returns true if getResult() will return basically immediately.
     */
    abstract boolean isResultKnown();

    public static LazyBoolEvaluationResult valueOf(boolean bool) {
        return bool ? TRUE : FALSE;
    }
}
