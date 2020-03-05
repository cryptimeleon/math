package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.bool.*;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.evaluator.trs.RuleApplicator;
import de.upb.crypto.math.expressions.evaluator.trs.bool.MoveEqTestToLeftSideRule;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.structures.zn.Zn;

import java.util.*;

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
     * @param boolRuleApplicator Contains the rules to apply to boolean expressions and how to apply them.
     * @param groupRuleApplicator Same as for boolean just for group elements.
     * @return Rewritten expression.
     */
    public Expression rewriteTerms(Expression expr, RuleApplicator boolRuleApplicator,
                                   RuleApplicator groupRuleApplicator) {
        // TODO: This can be improved by using repeated rewriting at every level in the tree, not just at the root
        //  since that wastes time traversing the tree each time.
        Expression newExpr = expr;
        if (expr instanceof GroupElementExpression) {
            do {
                newExpr = this.rewriteGroupTermsTopDown((GroupElementExpression) newExpr, groupRuleApplicator);
            } while (groupRuleApplicator.isAppliedAndReset());
        } else if (expr instanceof  BooleanExpression) {
            do {
                newExpr = this.rewriteBoolTermsTopDown((BooleanExpression) newExpr, boolRuleApplicator,
                        groupRuleApplicator);
            } while (boolRuleApplicator.isAppliedAndReset() | groupRuleApplicator.isAppliedAndReset());
        }
        return newExpr;
    }

    /**
     * Helper method if you don't want to apply any boolean rules, e.g. if this is used on a group expression.
     * @param expr The expression to rewrite.
     * @param groupRuleApplicator Contains the rules to apply to group expressions and how to apply them.
     * @return Rewritten expression.
     */
    public Expression rewriteTerms(Expression expr, RuleApplicator groupRuleApplicator) {
        return this.rewriteTerms(
                expr,
                new RuleApplicator(new LinkedList<>()),
                groupRuleApplicator
        );
    }

    public Expression rewriteTerms(Expression expr) {
        return this.rewriteTerms(
                expr,
                new RuleApplicator(this.config.getBoolRewritingRules()),
                new RuleApplicator(this.config.getGroupRewritingRules())
        );
    }

    public GroupElementExpression rewriteGroupTermsTopDown(GroupElementExpression expr, RuleApplicator ruleApplicator) {
        // apply as many rules on this expr as possible, constructing a new expression
        // once all rules have been applied, apply this method recursively on its children
        // also need to make sure rule application terminates, so use appropriate rules without infinite derivation
        GroupElementExpression newExpr = (GroupElementExpression) ruleApplicator.applyAllRules(expr);
        // now do recursive step, keep in mind this does not work for rules that could be applied multiple times
        // bottom up such as moving exponents into pairing, but that is what the parent method is for.
        if (newExpr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) newExpr;
            return new GroupOpExpr(
                    this.rewriteGroupTermsTopDown(opExpr.getLhs(), ruleApplicator),
                    this.rewriteGroupTermsTopDown(opExpr.getRhs(), ruleApplicator)
            );
        } else if (newExpr instanceof GroupInvExpr) {
            GroupInvExpr invExpr = (GroupInvExpr) newExpr;
            return new GroupInvExpr(
                    this.rewriteGroupTermsTopDown(invExpr.getBase(), ruleApplicator)
            );
        } else if (newExpr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) newExpr;
            return new GroupPowExpr(
                    this.rewriteGroupTermsTopDown(powExpr.getBase(), ruleApplicator), powExpr.getExponent()
            );
        } else if (newExpr instanceof GroupElementConstantExpr) {
            return newExpr;
        } else if (newExpr instanceof GroupEmptyExpr) {
            return newExpr;
        } else if (newExpr instanceof PairingExpr) {
            PairingExpr pairingExpr = (PairingExpr) newExpr;
            return new PairingExpr(
                    pairingExpr.getMap(),
                    this.rewriteGroupTermsTopDown(pairingExpr.getLhs(), ruleApplicator),
                    this.rewriteGroupTermsTopDown(pairingExpr.getRhs(), ruleApplicator)
            );
        } else if (newExpr instanceof GroupVariableExpr) {
            return newExpr;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper group expression: " + newExpr.getClass());
        }
    }


    public BooleanExpression rewriteBoolTermsTopDown(BooleanExpression expr, RuleApplicator boolRuleApplicator,
                                                     RuleApplicator groupRuleApplicator) {
        BooleanExpression newExpr = (BooleanExpression) boolRuleApplicator.applyAllRules(expr);

        if (newExpr instanceof BoolAndExpr) {
            BoolAndExpr andExpr = (BoolAndExpr) newExpr;
            return new BoolAndExpr(
                    this.rewriteBoolTermsTopDown(andExpr.getLhs(), boolRuleApplicator, groupRuleApplicator),
                    this.rewriteBoolTermsTopDown(andExpr.getRhs(), boolRuleApplicator, groupRuleApplicator)
            );
        } else if (newExpr instanceof BoolOrExpr) {
            BoolOrExpr orExpr = (BoolOrExpr) newExpr;
            return new BoolOrExpr(
                    this.rewriteBoolTermsTopDown(orExpr.getLhs(), boolRuleApplicator, groupRuleApplicator),
                    this.rewriteBoolTermsTopDown(orExpr.getRhs(), boolRuleApplicator, groupRuleApplicator)
            );
        } else if (newExpr instanceof BoolNotExpr) {
            BoolNotExpr notExpr = (BoolNotExpr) newExpr;
            return new BoolNotExpr(
                    this.rewriteBoolTermsTopDown(notExpr.getChild(), boolRuleApplicator, groupRuleApplicator)
            );
        } else if (newExpr instanceof BoolEmptyExpr || newExpr instanceof BoolVariableExpr
                || newExpr instanceof ExponentEqualityExpr || newExpr instanceof BoolConstantExpr) {
            return newExpr;
        } else if (newExpr instanceof GroupEqualityExpr) {
            GroupEqualityExpr equalityExpr = (GroupEqualityExpr) newExpr;
            return new GroupEqualityExpr(
                    this.rewriteGroupTermsTopDown(equalityExpr.getLhs(), groupRuleApplicator),
                    this.rewriteGroupTermsTopDown(equalityExpr.getRhs(), groupRuleApplicator)
            );
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper boolean expression: " + newExpr.getClass());
        }
    }

    public void markMergeableExprs(Expression expr, Map<Expression, Boolean> exprToMergeable) {
        if (expr instanceof BoolAndExpr) {
            BoolAndExpr andExpr = (BoolAndExpr) expr;
            markMergeableExprs(andExpr.getLhs(), exprToMergeable);
            markMergeableExprs(andExpr.getRhs(), exprToMergeable);
            exprToMergeable.put(
                    expr,
                    exprToMergeable.get(andExpr.getLhs()) && exprToMergeable.get(andExpr.getRhs())
            );
        } else if (expr instanceof BoolOrExpr) {
            BoolOrExpr orExpr = (BoolOrExpr) expr;
            exprToMergeable.put(expr, false);
            markMergeableExprs(orExpr.getLhs(), exprToMergeable);
            markMergeableExprs(orExpr.getRhs(), exprToMergeable);
        } else if (expr instanceof BoolNotExpr) {
            BoolNotExpr notExpr = (BoolNotExpr) expr;
            exprToMergeable.put(expr, false);
            markMergeableExprs(notExpr.getChild(), exprToMergeable);
        } else if (expr instanceof BoolEmptyExpr || expr instanceof BoolVariableExpr
                || expr instanceof ExponentEqualityExpr) {
            exprToMergeable.put(expr, false);
        } else if (expr instanceof GroupEqualityExpr) {
            exprToMergeable.put(expr, true);
        } else if (expr instanceof GroupElementExpression) {
            exprToMergeable.put(expr, false);
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper boolean or group expression: " + expr.getClass());
        }
    }

    public Expression traverseMergeANDs(BooleanExpression expr, Map<Expression, Boolean> exprToMergeable) {
        if (exprToMergeable.get(expr)) {
            return mergeANDs(expr);
        }
        if (expr instanceof BoolAndExpr) {
            BoolAndExpr andExpr = (BoolAndExpr) expr;
            return new BoolAndExpr(
                    (BooleanExpression) traverseMergeANDs(andExpr.getLhs(), exprToMergeable),
                    (BooleanExpression) traverseMergeANDs(andExpr.getRhs(), exprToMergeable)
            );
        } else if (expr instanceof BoolOrExpr) {
            BoolOrExpr orExpr = (BoolOrExpr) expr;
            return new BoolOrExpr(
                    (BooleanExpression) traverseMergeANDs(orExpr.getLhs(), exprToMergeable),
                    (BooleanExpression) traverseMergeANDs(orExpr.getRhs(), exprToMergeable)
            );
        } else if (expr instanceof BoolNotExpr) {
            BoolNotExpr notExpr = (BoolNotExpr) expr;
            return new BoolNotExpr(
                    (BooleanExpression) traverseMergeANDs(notExpr.getChild(), exprToMergeable)
            );
        } else if (expr instanceof BoolEmptyExpr || expr instanceof BoolVariableExpr
                || expr instanceof ExponentEqualityExpr || expr instanceof GroupEqualityExpr) {
            return expr;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper boolean expression: " + expr.getClass());
        }
    }

    /**
     * Takes an expression with ANDs of GroupEqualityExprs and merges them into one GroupEqualityExpr with a
     * multi-exponentiation. E.g. x_1 = 1 && x_2 = 1 get merged to x_1^a * x_2^b = 1 where a and b are randomly chosen
     * exponents.
     * @param expr The expression to merge.
     * @return Merged expression if it works, else the original expression.
     */
    private BooleanExpression mergeANDs(BooleanExpression expr) {
        List<GroupEqualityExpr> equalityExprs = new ArrayList<>();
        ExprRule moveEqTestToLeftSideRule = new MoveEqTestToLeftSideRule();
        // find all contained GroupEqualityExprs
        Queue<Expression> searchQueue = new LinkedList<>();
        searchQueue.add(expr);
        while (!searchQueue.isEmpty()) {
            Expression currExpr = searchQueue.poll();
            if (currExpr instanceof BoolAndExpr) {
                searchQueue.add(((BoolAndExpr) currExpr).getLhs());
                searchQueue.add(((BoolAndExpr) currExpr).getRhs());
            } else if (currExpr instanceof GroupEqualityExpr) {
                // need to make sure the equality expr has the form x = 1, so move right side over if possible
                if (moveEqTestToLeftSideRule.isApplicable(currExpr)) {
                    equalityExprs.add((GroupEqualityExpr) moveEqTestToLeftSideRule.apply(currExpr));
                } else {
                    equalityExprs.add((GroupEqualityExpr) currExpr);
                }
            } else {
                throw new IllegalArgumentException("Found something in expression tree that" +
                        "is not a BoolAndExpr or GroupEqualityExpr: " + currExpr.getClass());
            }
        }
        // Makes no sense for just one element
        if (equalityExprs.size() < 2) {
            return expr;
        }
        // Check that they actually all use the same group, else this does not work
        // TODO: Would be better to check this earlier. Then we could also handle not matching groups.
        int firstNonNullGroupIndex = 0;
        Group group = equalityExprs.get(0).getGroup();
        while (group == null && firstNonNullGroupIndex < equalityExprs.size()) {
            ++firstNonNullGroupIndex;
            group = equalityExprs.get(firstNonNullGroupIndex).getGroup();
        }
        if (group == null) {
            // Only variables in group expressions, cannot proceed.
            return expr;
        }
        // TODO: Do we need to test for prime order here, too?
        for (int i = firstNonNullGroupIndex+1; i < equalityExprs.size(); ++i) {
            if (equalityExprs.get(i).getGroup() != group) {
                // If one of the groups doesnt match, we cannot proceed.
                return expr;
            }
        }
        List<Zn.ZnElement> exponents = new LinkedList<>();
        GroupElementExpression newLeftSide = new GroupEmptyExpr(group);
        // This requires all equalityExprs to be of form x = 1. Else this won't work.
        // Should be guaranteed by the MoveEqTestToOneSideRule, however.
        for (GroupEqualityExpr equalityExpr : equalityExprs) {
            newLeftSide = newLeftSide.opPow(equalityExpr.getLhs(), group.getUniformlyRandomUnitExponent());
        }
        return new GroupEqualityExpr(
                newLeftSide,
                new GroupEmptyExpr(group)
        );
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
                            || ExponentExpressionAnalyzer.containsTypeExpr(powExpr.getExponent(), ExponentVariableExpr.class)
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
            return expr.evaluate().expr(); // TODO: Do we exclude GroupEmptyExpr here so we don't pre-evaluate that?
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
                throw new IllegalStateException("Expression supposedly contains variable even though it cannot.");
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
            if (!GroupElementExpressionAnalyzer.containsTypeExpr(powExpr.getBase(), GroupVariableExpr.class,
                    ExponentVariableExpr.class))
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
