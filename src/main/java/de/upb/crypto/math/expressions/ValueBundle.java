package de.upb.crypto.math.expressions;

import de.upb.crypto.math.expressions.bool.BoolConstantExpr;
import de.upb.crypto.math.expressions.bool.BoolVariableExpr;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupElementConstantExpr;
import de.upb.crypto.math.expressions.group.GroupVariableExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * A key-value mapping used for passing around named algebraic values.
 */
public class ValueBundle implements Substitution {
    protected HashMap<VariableExpression, GroupElement> groupElems = new HashMap<>();
    protected HashMap<VariableExpression, BigInteger> ints = new HashMap<>();
    protected HashMap<VariableExpression, RingElement> ringElems = new HashMap<>();
    protected HashMap<VariableExpression, Boolean> bools = new HashMap<>();
    //protected HashMap<VariableExpression, ValueList> lists = new HashMap<>(); //Not yet implemented

    public ValueBundle() {
    }

    /**
     * Creates a copy of the given ValueBundle
     */
    public ValueBundle(ValueBundle other) {
        this();
        groupElems.putAll(other.groupElems);
        ints.putAll(other.ints);
        ringElems.putAll(other.ringElems);
        bools.putAll(other.bools);
    }

    public ValueBundle copy() {
        return new ValueBundle(this);
    }

    public GroupElement getGroupElement(VariableExpression key) {
        return groupElems.get(key);
    }

    public RingElement getRingElement(VariableExpression key) {
        return ringElems.get(key);
    }

    public Zn.ZnElement getZnElement(VariableExpression key) {
        return (Zn.ZnElement) ringElems.get(key);
    }

    public Zp.ZpElement getZpElement(VariableExpression key) {
        return (Zp.ZpElement) ringElems.get(key);
    }

    public Boolean getBoolean(VariableExpression key) { return bools.get(key); }

    public BigInteger getInteger(VariableExpression key) {
        if (ints.containsKey(key))
            return ints.get(key);

        //Fallback: if no integer is in this, try if there's an integer-like RingElement we can return
        RingElement alternative = ringElems.get(key);
        try {
            return alternative.asInteger();
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    public void put(VariableExpression key, GroupElement value) {
        groupElems.put(key, value);
        ints.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        bools.remove(key);
    }

    public void put(VariableExpression key, RingElement value) {
        ringElems.put(key, value);
        ints.remove(key); //enforce unique keys between the types
        groupElems.remove(key);
        bools.remove(key);
    }

    public void put(VariableExpression key, BigInteger value) {
        ints.put(key, value);
        groupElems.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        bools.remove(key);
    }

    public void put(VariableExpression key, boolean value) {
        bools.put(key, value);
        groupElems.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        ints.remove(key);
    }

    @Override
    public Expression getSubstitution(VariableExpression variable) {
        if (variable instanceof GroupVariableExpr) {
            GroupElement result = getGroupElement(variable);
            return result == null ? null : new GroupElementConstantExpr(result);
        }

        if (variable instanceof ExponentVariableExpr) {
            BigInteger result = getInteger(variable);
            return result == null ? null : new ExponentConstantExpr(result);
        }

        if (variable instanceof BoolVariableExpr) {
            Boolean result = getBoolean(variable);
            return result == null ? null : new BoolConstantExpr(result);
        }

        return null;
    }
}
