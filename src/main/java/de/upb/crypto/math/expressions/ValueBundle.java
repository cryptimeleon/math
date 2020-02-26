package de.upb.crypto.math.expressions;

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
public class ValueBundle {
    protected HashMap<String, GroupElement> groupElems;
    protected HashMap<String, BigInteger> ints;
    protected HashMap<String, RingElement> ringElems;
    protected HashMap<String, Boolean> bools;
    //protected HashMap<String, ValueList> lists = new HashMap<>(); //Not yet implemented

    public ValueBundle() {
        groupElems = new HashMap<>();
        ints = new HashMap<>();
        ringElems = new HashMap<>();
        bools = new HashMap<>();
    }

    /**
     * Creates a copy of the given ValueBundle
     */
    public ValueBundle(ValueBundle other) {
        groupElems.putAll(other.groupElems);
        ints.putAll(other.ints);
        ringElems.putAll(other.ringElems);
        bools.putAll(other.bools);
    }

    public ValueBundle copy() {
        return new ValueBundle(this);
    }

    public GroupElement getGroupElement(String key) {
        return groupElems.get(key);
    }

    public RingElement getRingElement(String key) {
        return ringElems.get(key);
    }

    public Zn.ZnElement getZnElement(String key) {
        return (Zn.ZnElement) ringElems.get(key);
    }

    public Zp.ZpElement getZpElement(String key) {
        return (Zp.ZpElement) ringElems.get(key);
    }

    public Boolean getBoolean(String key) { return bools.get(key); }

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

    public void put(String key, GroupElement value) {
        groupElems.put(key, value);
        ints.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        bools.remove(key);
    }

    public void put(String key, RingElement value) {
        ringElems.put(key, value);
        ints.remove(key); //enforce unique keys between the types
        groupElems.remove(key);
        bools.remove(key);
    }

    public void put(String key, BigInteger value) {
        ints.put(key, value);
        groupElems.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        bools.remove(key);
    }

    public void put(String key, boolean value) {
        bools.put(key, value);
        groupElems.remove(key); //enforce unique keys between the types
        ringElems.remove(key);
        ints.remove(key);
    }
}
