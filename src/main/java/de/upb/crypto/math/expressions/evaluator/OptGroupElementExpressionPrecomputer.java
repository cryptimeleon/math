package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.evaluator.trs.RuleApplicator;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OptGroupElementExpressionPrecomputer {

    private OptGroupElementExpressionEvaluatorConfig config;

    public OptGroupElementExpressionPrecomputer() {
        this.config = new OptGroupElementExpressionEvaluatorConfig();
    }

    public OptGroupElementExpressionPrecomputer(OptGroupElementExpressionEvaluatorConfig config) {
        this.config = config;
    }

    /**
     * Tries to fix the problem of not being able to traverse the expression tree back up by just rewriting
     * terms until the expression that comes out is not different anymore.
     * @param expr The expression to rewrite.
     * @param ruleApplicator Contains the rules to apply and how to apply them.
     * @return Rewritten expression.
     */
    public GroupElementExpression rewriteTerms(GroupElementExpression expr, RuleApplicator ruleApplicator) {
        GroupElementExpression newExpr = expr;
        do {
            newExpr = this.rewriteTermsTopDown(newExpr, ruleApplicator);
        } while (ruleApplicator.isAppliedAndReset());
        return newExpr;
    }

    public GroupElementExpression rewriteTermsTopDown(GroupElementExpression expr, RuleApplicator ruleApplicator) {
        // apply as many rules on this expr as possible, constructing a new expression
        // once all rules have been applied, apply this method recursively on its children
        // also need to make sure rule application terminates, so use appropriate rules without infinite derivation
        GroupElementExpression newExpr = ruleApplicator.applyAllRules(expr);
        // now do recursive step, keep in mind this does not work for rules that could be applied multiple times
        // bottom up such as moving exponents into pairing, but that is what the parent method is for.
        if (newExpr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) newExpr;
            return new GroupOpExpr(
                    this.rewriteTermsTopDown(opExpr.getLhs(), ruleApplicator),
                    this.rewriteTermsTopDown(opExpr.getRhs(), ruleApplicator)
            );
        } else if (newExpr instanceof GroupInvExpr) {
            GroupInvExpr invExpr = (GroupInvExpr) newExpr;
            return new GroupInvExpr(
                    this.rewriteTermsTopDown(invExpr.getBase(), ruleApplicator)
            );
        } else if (newExpr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) newExpr;
            return new GroupPowExpr(
                    this.rewriteTermsTopDown(powExpr.getBase(), ruleApplicator), powExpr.getExponent()
            );
        } else if (newExpr instanceof GroupElementConstantExpr) {
            return newExpr;
        } else if (newExpr instanceof GroupEmptyExpr) {
            return newExpr;
        } else if (newExpr instanceof PairingExpr) {
            PairingExpr pairingExpr = (PairingExpr) newExpr;
            return new PairingExpr(
                    pairingExpr.getMap(),
                    this.rewriteTermsTopDown(pairingExpr.getLhs(), ruleApplicator),
                    this.rewriteTermsTopDown(pairingExpr.getRhs(), ruleApplicator)
            );
        } else if (newExpr instanceof GroupVariableExpr) {
            return newExpr;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    /**
     * Traverses the given expression and marks call sub-expressions that contain a variable.
     * @param expr The expression to search.
     * @param exprToContainsVar Maps each sub-expression to a Boolean indicating whether the expression contains
     *                          a variable or not.
     */
    public void markExprWithVars(GroupElementExpression expr, Map<GroupElementExpression, Boolean> exprToContainsVar) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) expr;
            markExprWithVars(opExpr.getRhs(), exprToContainsVar);
            markExprWithVars(opExpr.getLhs(), exprToContainsVar);
            exprToContainsVar.put(
                    opExpr, exprToContainsVar.get(opExpr.getLhs()) || exprToContainsVar.get(opExpr.getRhs())
            );
        } else if (expr instanceof GroupInvExpr) {
            GroupInvExpr invExpr = (GroupInvExpr) expr;
            markExprWithVars(invExpr.getBase(), exprToContainsVar);
            exprToContainsVar.put(
                    invExpr, exprToContainsVar.get(invExpr.getBase())
            );
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) expr;
            markExprWithVars(powExpr.getBase(), exprToContainsVar);
            exprToContainsVar.put(
                    powExpr, exprToContainsVar.get(powExpr.getBase())
                            || ExponentExpressionAnalyzer.containsVariableExpr(powExpr.getExponent())
            );
        } else if (expr instanceof GroupElementConstantExpr) {
            exprToContainsVar.put(expr, false);
        } else if (expr instanceof GroupEmptyExpr) {
            exprToContainsVar.put(expr, false);
        } else if (expr instanceof PairingExpr) {
            PairingExpr pairingExpr = (PairingExpr) expr;
            markExprWithVars(pairingExpr.getLhs(), exprToContainsVar);
            markExprWithVars(pairingExpr.getRhs(), exprToContainsVar);
            exprToContainsVar.put(
                    pairingExpr,
                    exprToContainsVar.get(pairingExpr.getLhs()) || exprToContainsVar.get(pairingExpr.getRhs())
            );
        } else if (expr instanceof GroupVariableExpr) {
            exprToContainsVar.put(expr, true);
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    /**
     * Uses information about which expressions contains variables to create a new reduced expression with as
     * many sub-expressions evaluated already as possible.
     * @param expr The expression to reduce.
     * @param exprToContainsVar Stores for each sub-expression whether it contains variables.
     * @return New reduced expression.
     */
    public GroupElementExpression evalWithoutVars(GroupElementExpression expr,
                                                  Map<GroupElementExpression, Boolean> exprToContainsVar) {
        if (!exprToContainsVar.get(expr)) {
            return expr.evaluate().expr();
        } else {
            if (expr instanceof GroupOpExpr) {
                GroupOpExpr opExpr = (GroupOpExpr) expr;
                return new GroupOpExpr(
                        evalWithoutVars(opExpr.getLhs(), exprToContainsVar),
                        evalWithoutVars(opExpr.getRhs(), exprToContainsVar)
                );
            } else if (expr instanceof GroupInvExpr) {
                GroupInvExpr invExpr = (GroupInvExpr) expr;
                return new GroupInvExpr(
                        evalWithoutVars(invExpr.getBase(), exprToContainsVar)
                );
            } else if (expr instanceof GroupPowExpr) {
                GroupPowExpr powExpr = (GroupPowExpr) expr;
                // We don't go deeper into exponent
                return new GroupPowExpr(
                        evalWithoutVars(powExpr.getBase(), exprToContainsVar), powExpr.getExponent()
                );
            } else if (expr instanceof PairingExpr) {
                PairingExpr pairingExpr = (PairingExpr) expr;
                return new PairingExpr(
                        pairingExpr.getMap(),
                        evalWithoutVars(pairingExpr.getLhs(), exprToContainsVar),
                        evalWithoutVars(pairingExpr.getRhs(), exprToContainsVar)
                );
            } else if (expr instanceof GroupVariableExpr) {
                return expr;
            } else if (expr instanceof GroupElementConstantExpr || expr instanceof GroupEmptyExpr) {
                throw new IllegalStateException("Expression contains variable although it cannot.");
            } else {
                throw new IllegalArgumentException("Found something in expression tree that" +
                        "is not a proper group expression.");
            }
        }
    }

    /**
     * Finds all bases contained in the expr and stores it as information.
     * @param expr Expression to find bases in.
     * @param exprToContBases Maps expressions to bases contained within them.
     */
    public void findContainedBases(GroupElementExpression expr, boolean inInversion,
                                    Map<GroupElementExpression, List<GroupElement>> exprToContBases) {
        // TODO: Would be nice to refactor this pattern,
        //  but recursion is required for inversion handling
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) expr;
            // group not necessarily commutative, so if we are in inversion, switch order
            if (inInversion) {
                findContainedBases(opExpr.getRhs(), inInversion, exprToContBases);
                findContainedBases(opExpr.getLhs(), inInversion, exprToContBases);
                List<GroupElement> containedBasesRight = exprToContBases.computeIfAbsent(opExpr.getRhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBasesLeft = exprToContBases.computeIfAbsent(opExpr.getLhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
                containedBases.addAll(containedBasesRight);
                containedBases.addAll(containedBasesLeft);
            } else {
                findContainedBases(opExpr.getLhs(), inInversion, exprToContBases);
                findContainedBases(opExpr.getRhs(), inInversion, exprToContBases);
                List<GroupElement> containedBasesLeft = exprToContBases.computeIfAbsent(opExpr.getLhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBasesRight = exprToContBases.computeIfAbsent(opExpr.getRhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
                containedBases.addAll(containedBasesLeft);
                containedBases.addAll(containedBasesRight);
            }
        } else if (expr instanceof GroupInvExpr) {
            GroupElementExpression invBase = ((GroupInvExpr) expr).getBase();
            findContainedBases(invBase, !inInversion, exprToContBases);
            // propagate found bases from base to inv expression
            List<GroupElement> invBaseContBases = exprToContBases.computeIfAbsent(invBase, k -> new LinkedList<>());
            List<GroupElement> invContBases = exprToContBases.computeIfAbsent(expr, k ->  new LinkedList<>());
            invContBases.addAll(invBaseContBases);
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) expr;
            // for now, just use evaluate naive on base and exponent
            List<GroupElement> containedBases = exprToContBases.computeIfAbsent(powExpr, k -> new LinkedList<>());
            // change this if we change multiexp algorithm to be smarter
            // if variable in there we cannot precompute easily
            if (!GroupElementExpressionAnalyzer.containsVariableExpr(powExpr.getBase()))
                containedBases.add(powExpr.getBase().evaluateNaive());
        } else if (expr instanceof GroupElementConstantExpr) {
            // count this as basis too for now since multiexp does it too
            List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
            containedBases.add(expr.evaluateNaive());
        } else if (expr instanceof GroupEmptyExpr) {
            // count this as basis too for now since multiexp does it too
            List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
            containedBases.add(expr.evaluateNaive());
        } else if (expr instanceof PairingExpr) {
            // Dont handle pairing here, need to call precompute on both sides but not here
        } else if (expr instanceof GroupVariableExpr) {
            // Dont handle variable here
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    /**
     * Precomputes and caches odd powers/power products for later multi-exponentiation. Which is
     * done depends on the algorithm that would be selected for evaluation with the current
     * settings.
     * @param expr Expression to cache powers for.
     * @param exprToContainedBases Maps expressions to bases contained within them.
     */
    public void cacheWindows(GroupElementExpression expr,
                              Map<GroupElementExpression, List<GroupElement>> exprToContainedBases) {
        // Find out which algorithm would be used with current settings and do precomputation
        // for that algorithm.
        List<GroupElement> rootBases = exprToContainedBases.get(expr);
        if (rootBases == null || rootBases.size() == 0)
            return;
        int rootNumBases = rootBases.size();
        int costOfInversion = rootBases.get(0).getStructure().estimateCostOfInvert();
        MultiExpAlgorithm alg = config.getCurrentlyChosenMultiExpAlgorithm(rootNumBases, costOfInversion);
        // Precompute powers, we always use caching window size.
        switch (alg) {
            case INTERLEAVED_WNAF:
                // precompute odd powers
                int maxExp = (1 << config.getWindowSizeInterleavedWnafCaching()) - 1;
                GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                        GroupPrecomputationsFactory.get(rootBases.get(0).getStructure());
                for (GroupElement base : rootBases) {
                    groupPrecomputations.addOddPowers(base, maxExp);
                }
                return;
            case INTERLEAVED_SLIDING:
                // precompute odd powers
                maxExp = (1 << config.getWindowSizeInterleavedSlidingCaching()) - 1;
                groupPrecomputations = GroupPrecomputationsFactory
                        .get(rootBases.get(0).getStructure());
                for (GroupElement base : rootBases) {
                    groupPrecomputations.addOddPowers(base, maxExp);
                }
                return;
            case SIMULTANEOUS:
                // precompute power products
                // power products depends on order of bases, so they should be left to right as
                // in multiexp finding alg (sorting does not work if commutativity not given)
                groupPrecomputations = GroupPrecomputationsFactory
                        .get(rootBases.iterator().next().getStructure());
                groupPrecomputations.addPowerProducts(rootBases, config.getWindowSizeSimultaneousCaching());
        }
    }
}
