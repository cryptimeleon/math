package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.evaluator.trs.*;
import de.upb.crypto.math.expressions.evaluator.trs.group.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Class containing configuration of {@link OptGroupElementExpressionEvaluator}.
 *
 * @author Raphael Heitjohann
 */
public class OptGroupElementExpressionEvaluatorConfig {

    private boolean enableCachingInterleavedSliding;
    private boolean enableCachingSimultaneous;
    private boolean enableCachingInterleavedWnaf;
    private boolean enableMultithreadedPairingEvaluation;
    private ForceMultiExpAlgorithmSetting forcedMultiExpAlgorithm;
    private int windowSizeInterleavedSlidingCaching;
    private int windowSizeInterleavedSlidingNoCaching;
    private int windowSizeInterleavedWnafCaching;
    private int windowSizeInterleavedWnafNoCaching;
    private int windowSizeSimultaneousCaching;
    private int windowSizeSimultaneousNoCaching;
    private int simultaneousNumBasesCutoff;
    /**
     * When to use Wnaf. Default is 100 which means that 100 inversions in the group cost as much
     * as 100 group operations. Smaller values mean that inversions are even cheaper. Wnaf will be
     * used if the groups cost of inversion is <= than this value.
     */
    private int useWnafCostInversion;

    /**
     * Whether to pre-evaluate the expression as much as possible. E.g. for (g^2)^x, the g^2 will be evaluated.
     */
    private boolean enablePrecomputeEvaluation;
    /**
     * Whether to already precompute and cache powers for later multiexponentiations.
     */
    private boolean enablePrecomputeCaching;
    /**
     * Whether to rewrite expression for more efficient evaluation later.
     */
    private boolean enablePrecomputeRewriting;
    /**
     * List of rules used to rewrite expression terms. Order determines precedence when multiple rules are applicable.
     * Rules more towards start of list are preferred.
     */
    private List<ExprRule> groupRewritingRules;

    public OptGroupElementExpressionEvaluatorConfig() {
        // TODO: best default values here? Could use even more finetuning
        enableCachingInterleavedSliding = true;
        enableCachingSimultaneous = true;
        enableCachingInterleavedWnaf = true;
        forcedMultiExpAlgorithm = ForceMultiExpAlgorithmSetting.DISABLED;
        windowSizeInterleavedSlidingCaching = 8;
        windowSizeInterleavedSlidingNoCaching = 4;
        windowSizeInterleavedWnafCaching = 8;
        windowSizeInterleavedWnafNoCaching = 4;
        windowSizeSimultaneousCaching = 1;
        windowSizeSimultaneousNoCaching = 1;
        simultaneousNumBasesCutoff = 10;
        useWnafCostInversion = 100;

        enablePrecomputeCaching = true;
        enablePrecomputeEvaluation = true;
        enablePrecomputeRewriting = true;

        groupRewritingRules = new LinkedList<>();
        groupRewritingRules.add(new OpInPowRule());
        groupRewritingRules.add(new PowExpMulLeftRule());
        groupRewritingRules.add(new PowExpMulRightRule());
        groupRewritingRules.add(new MergeNestedConstExpRule());
        groupRewritingRules.add(new MergeNestedVarExpRule());
        groupRewritingRules.add(new ExpSwapRule());
        groupRewritingRules.add(new PairingMoveLeftVarsOutsideRule());
        groupRewritingRules.add(new PairingMoveRightVarsOutsideRule());
        groupRewritingRules.add(new PairingGtExpRule());

        // For parallel evaluation of both sides of a pairing
        // In the case of expensive pairings such as the BN pairing, this
        // seems to really only be worth it if the multi-exponentiation is very big,
        // and even then, the difference is not really noticeable at all since the pairing
        // is so expensive.
        enableMultithreadedPairingEvaluation = false;
    }

    /**
     * Calculates which multi-exponentiation algorithm to use according to chosen settings.
     * @param numBases Number of bases in multi-exponentiation to evaluate.
     * @param costInversion Cost of inversion in group of multi-exponentiation.
     * @return The multi-exponentiation algorithm that would be chosen.
     */
    public MultiExpAlgorithm getCurrentlyChosenMultiExpAlgorithm(int numBases, int costInversion) {
        switch (getForcedMultiExpAlgorithm()) {
            case SIMULTANEOUS:
                return MultiExpAlgorithm.SIMULTANEOUS;
            case INTERLEAVED_SLIDING:
                return MultiExpAlgorithm.INTERLEAVED_SLIDING;
            case INTERLEAVED_WNAF:
                return MultiExpAlgorithm.INTERLEAVED_WNAF;
            case DISABLED:
                // select algorithm based on swante scholz's recommendations
                if (numBases < getSimultaneousNumBasesCutoff()
                        && isEnableCachingSimultaneous()) {
                    return MultiExpAlgorithm.SIMULTANEOUS;
                } else if (costInversion <= getUseWnafCostInversion()) {
                    return MultiExpAlgorithm.INTERLEAVED_WNAF;
                } else {
                    return MultiExpAlgorithm.INTERLEAVED_SLIDING;
                }
        }
        throw new IllegalArgumentException("Unsupported forcedMultiExpAlgorithm value.");
    }

    /**
     * Enable/Disable caching for specified algorithm. Can be useful if you know a base
     * will only be used once. Otherwise (especially for the simultaneous algorithm) caching
     * should be enabled.
     * @param alg The algorithm to enable/disable caching for.
     * @param newSetting Whether caching should be enabled or disabled.
     */
    public void setEnableCachingForAlg(ForceMultiExpAlgorithmSetting alg, boolean newSetting) {
        switch (alg) {
            case INTERLEAVED_SLIDING:
                setEnableCachingInterleavedSliding(newSetting);
                return;
            case INTERLEAVED_WNAF:
                setEnableCachingInterleavedWnaf(newSetting);
                return;
            case SIMULTANEOUS:
                setEnableCachingSimultaneous(newSetting);
                return;
        }
        throw new IllegalArgumentException("Unsupported ForceMultiExpAlgorithmSetting value.");
    }

    public enum ForceMultiExpAlgorithmSetting {
        DISABLED, INTERLEAVED_SLIDING, INTERLEAVED_WNAF, SIMULTANEOUS
    }

    public boolean isEnableCachingInterleavedSliding() {
        return enableCachingInterleavedSliding;
    }

    public void setEnableCachingInterleavedSliding(boolean enableCachingInterleavedSliding) {
        this.enableCachingInterleavedSliding = enableCachingInterleavedSliding;
    }

    public boolean isEnableCachingSimultaneous() {
        return enableCachingSimultaneous;
    }

    public void setEnableCachingSimultaneous(boolean enableCachingSimultaneous) {
        this.enableCachingSimultaneous = enableCachingSimultaneous;
    }

    public boolean isEnableCachingInterleavedWnaf() {
        return enableCachingInterleavedWnaf;
    }

    public void setEnableCachingInterleavedWnaf(boolean enableCachingInterleavedWnaf) {
        this.enableCachingInterleavedWnaf = enableCachingInterleavedWnaf;
    }

    public boolean isEnableMultithreadedPairingEvaluation() {
        return enableMultithreadedPairingEvaluation;
    }

    public void setEnableMultithreadedPairingEvaluation(boolean enableMultithreadedPairingEvaluation) {
        this.enableMultithreadedPairingEvaluation = enableMultithreadedPairingEvaluation;
    }

    public ForceMultiExpAlgorithmSetting getForcedMultiExpAlgorithm() {
        return forcedMultiExpAlgorithm;
    }

    /**
     * Allows forcing a specific multi-exponentiation algorithm to be used.
     * Inappropriate usage (such as forcing simultaneous without caching) can lead
     * to terrible performance.
     * @param forcedMultiExpAlgorithm Algorithm to force usage of.
     */
    public void setForcedMultiExpAlgorithm(ForceMultiExpAlgorithmSetting forcedMultiExpAlgorithm) {
        this.forcedMultiExpAlgorithm = forcedMultiExpAlgorithm;
    }

    public int getWindowSizeInterleavedSlidingCaching() {
        return windowSizeInterleavedSlidingCaching;
    }

    public void setWindowSizeInterleavedSlidingCaching(int windowSizeInterleavedSlidingCaching) {
        this.windowSizeInterleavedSlidingCaching = windowSizeInterleavedSlidingCaching;
    }

    public int getWindowSizeInterleavedSlidingNoCaching() {
        return windowSizeInterleavedSlidingNoCaching;
    }

    public void setWindowSizeInterleavedSlidingNoCaching(int windowSizeInterleavedSlidingNoCaching) {
        this.windowSizeInterleavedSlidingNoCaching = windowSizeInterleavedSlidingNoCaching;
    }

    public int getWindowSizeInterleavedWnafCaching() {
        return windowSizeInterleavedWnafCaching;
    }

    public void setWindowSizeInterleavedWnafCaching(int windowSizeInterleavedWnafCaching) {
        this.windowSizeInterleavedWnafCaching = windowSizeInterleavedWnafCaching;
    }

    public int getWindowSizeInterleavedWnafNoCaching() {
        return windowSizeInterleavedWnafNoCaching;
    }

    public void setWindowSizeInterleavedWnafNoCaching(int windowSizeInterleavedWnafNoCaching) {
        this.windowSizeInterleavedWnafNoCaching = windowSizeInterleavedWnafNoCaching;
    }

    public int getWindowSizeSimultaneousCaching() {
        return windowSizeSimultaneousCaching;
    }

    public void setWindowSizeSimultaneousCaching(int windowSizeSimultaneousCaching) {
        this.windowSizeSimultaneousCaching = windowSizeSimultaneousCaching;
    }

    public int getWindowSizeSimultaneousNoCaching() {
        return windowSizeSimultaneousNoCaching;
    }

    public void setWindowSizeSimultaneousNoCaching(int windowSizeSimultaneousNoCaching) {
        this.windowSizeSimultaneousNoCaching = windowSizeSimultaneousNoCaching;
    }


    public int getSimultaneousNumBasesCutoff() {
        return simultaneousNumBasesCutoff;
    }

    /**
     * Upper bound for number of bases below which the simultaneous multi-exponentiation
     * approach may be used. Increasing this value too much can lead to full heap memory errors if
     * the precomputed power products take up too much space. Caching should then also be enabled
     * for the simultaneous algorithm to avoid the expensive repeated precomputation of
     * power products.
     * @param simultaneousNumBasesCutoff New upper bound for simultaneous algorithm usage.
     */
    public void setSimultaneousNumBasesCutoff(int simultaneousNumBasesCutoff) {
        this.simultaneousNumBasesCutoff = simultaneousNumBasesCutoff;
    }

    public int getUseWnafCostInversion() {
        return useWnafCostInversion;
    }

    /**
     * Upper bound for cost of inversion in the group below the evaluator should use a WNAF-bases
     * multi-exponentiation algorithm. A value of 100 means that if 100 inversions cost as much or
     * less than 100 group operations, then the WNAF-based algorithm will be used.
     * @param useWnafCostInversion New upper bound for WNAF usage.
     */
    public void setUseWnafCostInversion(int useWnafCostInversion) {
        this.useWnafCostInversion = useWnafCostInversion;
    }

    public boolean isEnablePrecomputeEvaluation() {
        return enablePrecomputeEvaluation;
    }

    public void setEnablePrecomputeEvaluation(boolean enablePrecomputeEvaluation) {
        this.enablePrecomputeEvaluation = enablePrecomputeEvaluation;
    }

    public boolean isEnablePrecomputeCaching() {
        return enablePrecomputeCaching;
    }

    public void setEnablePrecomputeCaching(boolean enablePrecomputeCaching) {
        this.enablePrecomputeCaching = enablePrecomputeCaching;
    }

    public boolean isEnablePrecomputeRewriting() {
        return enablePrecomputeRewriting;
    }

    public void setEnablePrecomputeRewriting(boolean enablePrecomputeRewriting) {
        this.enablePrecomputeRewriting = enablePrecomputeRewriting;
    }

    public List<ExprRule> getGroupRewritingRules() {
        return groupRewritingRules;
    }

    public void setGroupRewritingRules(List<ExprRule> groupRewritingRules) {
        this.groupRewritingRules = groupRewritingRules;
    }
}
