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
 * <p>
 * These values can be used to substitute variables with the same name in some {@link Expression}.
 */
public class ValueBundle {
    /**
     * Maps variable names to substitute {@code GroupElement}'s.
     */
    protected HashMap<String, GroupElement> groupElems;

    /**
     * Maps variable names to substitute {@code BigInteger}'s.
     */
    protected HashMap<String, BigInteger> ints;

    /**
     * Maps variable names to substitute {@code RingElement}'s.
     */
    protected HashMap<String, RingElement> ringElems;

    /**
     * Maps variable names to substitute {@code Boolean}'s.
     */
    protected HashMap<String, Boolean> bools;
    //protected HashMap<String, ValueList> lists = new HashMap<>(); //Not yet implemented

    /**
     * Initializes an empty {@code ValueBundle}.
     */
    public ValueBundle() {
        groupElems = new HashMap<>();
        ints = new HashMap<>();
        ringElems = new HashMap<>();
        bools = new HashMap<>();
    }

    /**
     * Creates a copy of the given {@code ValueBundle}.
     * <p>
     * The maps used to store the algebraic elements are recreated, the elements themselves are not cloned.
     */
    public ValueBundle(ValueBundle other) {
        this();
        groupElems.putAll(other.groupElems);
        ints.putAll(other.ints);
        ringElems.putAll(other.ringElems);
        bools.putAll(other.bools);
    }

    /**
     * Creates copy of this {@code ValueBundle}.
     * <p>
     * The maps used to store the algebraic elements are recreated, the elements themselves are not cloned.
     */
    public ValueBundle copy() {
        return new ValueBundle(this);
    }

    /**
     * Retrieves the {@code GroupElement} identified by the given key.
     *
     * @param key the key {@code String} identifying the desired group element
     * @return the corresponding group element
     */
    public GroupElement getGroupElement(String key) {
        return groupElems.get(key);
    }

    /**
     * Retrieves the {@code RingElement} identified by the given key.
     *
     * @param key the key {@code String} identifying the desired ring element
     * @return the corresponding ring element
     */
    public RingElement getRingElement(String key) {
        return ringElems.get(key);
    }

    /**
     * Retrieves the {@link Zn.ZnElement} identified by the given key.
     *
     * @param key the key {@code String} identifying the desired Zn element
     * @return the corresponding Zn element
     */
    public Zn.ZnElement getZnElement(String key) {
        return (Zn.ZnElement) ringElems.get(key);
    }

    /**
     * Retrieves the {@link Zp.ZpElement} identified by the given key.
     *
     * @param key the key {@code String} identifying the desired Zp element
     * @return the corresponding Zp element
     */
    public Zp.ZpElement getZpElement(String key) {
        return (Zp.ZpElement) ringElems.get(key);
    }

    /**
     * Retrieves the {@code Boolean} identified by the given key.
     *
     * @param key the key {@code String} identifying the desired {@code Boolean}
     * @return the corresponding {@code Boolean}
     */
    public Boolean getBoolean(String key) { return bools.get(key); }

    /**
     * Retrieves the {@link BigInteger} identified by the given key.
     * <p>
     * If the desired integer cannot be found, the method looks for an integer-like ring element
     * with the given key instead.
     *
     * @param key the key {@code String} identifying the desired {@code BigInteger}
     * @return the corresponding {@code BigInteger}
     */
    public BigInteger getInteger(String key) {
        if (ints.containsKey(key))
            return ints.get(key);

        //Fallback: if no integer is in this, try if there's an integer-like RingElement we can return
        RingElement alternative = ringElems.get(key);
        if (alternative instanceof Zn.ZnElement)
            return ((Zn.ZnElement) alternative).getInteger();
        if (alternative instanceof IntegerElement)
            return ((IntegerElement) alternative).getBigInt();
        return null;
    }

    /**
     * Adds a {@code GroupElement} with the given key to this value bundle.
     * <p>
     * If the key is used already by another element (no matter the type), that element gets removed.
     *
     * @param key the key to identify the group element with
     * @param value the group element to add
     */
    public void put(String key, GroupElement value) {
        groupElems.put(key, value);
        ints.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        bools.remove(key);
    }

    /**
     * Adds a {@code RingElement} with the given key to this value bundle.
     * <p>
     * If the key is used already by another element (no matter the type), that element gets removed.
     *
     * @param key the key to identify the ring element with
     * @param value the ring element to add
     */
    public void put(String key, RingElement value) {
        ringElems.put(key, value);
        ints.remove(key); //enforce unique keys between the types
        groupElems.remove(key);
        bools.remove(key);
    }

    /**
     * Adds a {@code BigInteger} with the given key to this value bundle.
     * <p>
     * If the key is used already by another element (no matter the type), that element gets removed.
     *
     * @param key the key to identify the {@code BigInteger} with
     * @param value the {@code BigInteger} to add
     */
    public void put(String key, BigInteger value) {
        ints.put(key, value);
        groupElems.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        bools.remove(key);
    }

    /**
     * Adds a boolean with the given key to this value bundle.
     * <p>
     * If the key is used already by another element (no matter the type), that element gets removed.
     *
     * @param key the key to identify the boolean with
     * @param value the boolean to add
     */
    public void put(String key, boolean value) {
        bools.put(key, value);
        groupElems.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        ints.remove(key);
    }

    /**
     * Constructs a new {@link Expression} by substituting variables in the given {@link VariableExpression}.
     * <p>
     * Works by, depending on the type of the {@code VariableExpression}, searching the corresponding key-value map
     * for the substitution with the key given by the variable's name and applying the substitution if possible.
     * If no such element exists, or the {@code VariableExpression} is of an unsupported subtype, null is returned.
     *
     * @param variable the {@code VariableExpression} to substitute
     * @return the substituted {@code Expression} if substitution was successful, else null
     */
    public Expression getSubstitution(VariableExpression variable) {
        if (variable instanceof GroupVariableExpr) {
            GroupElement result = getGroupElement(variable.getName());
            return result == null ? null : new GroupElementConstantExpr(result);
        }

        if (variable instanceof ExponentVariableExpr) {
            BigInteger result = getInteger(variable.getName());
            return result == null ? null : new ExponentConstantExpr(result);
        }

        if (variable instanceof BoolVariableExpr) {
            Boolean result = getBoolean(variable.getName());
            return result == null ? null : new BoolConstantExpr(result);
        }

        return null;
    }
}
