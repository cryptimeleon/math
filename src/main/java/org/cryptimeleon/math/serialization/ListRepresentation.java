package org.cryptimeleon.math.serialization;

import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.util.*;
import java.util.stream.Stream;

/**
 * Representation of an ordered list of Representations
 */
public class ListRepresentation extends Representation implements Iterable<Representation> {
    private static final long serialVersionUID = -7782043563152429201L;
    /**
     * The list represented by this representation.
     */
    protected final List<Representation> list = new ArrayList<>();

    /**
     * Creates an empty list representation
     */
    public ListRepresentation() {

    }

    public ListRepresentation(Vector<? extends Representation> vector) {
        this(vector.toList());
    }

    public ListRepresentation(List<? extends Representation> list) {
        this.list.addAll(list);
    }

    public ListRepresentation(Representation... list) {
        this.list.addAll(Arrays.asList(list));
    }

    /**
     * Retrieves the {@code i}-th entry in this list.
     * @param i the index of the entry to retrieve
     */
    public Representation get(int i) {
        return list.get(i);
    }

    /**
     * Retrieves the size of the represented list.
     */
    public int size() {
        return list.size();
    }

    /**
     * Adds a representation to the list (same as {@link #add(Representation)}).
     * @param value the representation to add
     */
    public void put(Representation value) {
        list.add(value);
    }

    /**
     * Adds a representation to the list (same as {@link #put(Representation)}).
     * @param value the representation to add
     */
    public void add(Representation value) {
        put(value);
    }

    /**
     * Retrieves an immutable view of the list.
     */
    public List<Representation> getList() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Retrieves an array version of the list.
     */
    public Representation[] getArray() {
        return list.toArray(new Representation[list.size()]);
    }

    /**
     * Constructs an {@link Iterator} over the list.
     */
    @Override
    public Iterator<Representation> iterator() {
        return list.iterator();
    }

    /**
     * Constructs a {@link Stream} over the list.
     */
    public Stream<Representation> stream() {
        return list.stream();
    }

    @Override
    public String toString() {
        return new JSONConverter().serialize(this);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object obj) { //Eclipse generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ListRepresentation other = (ListRepresentation) obj;
        return this.list.equals(other.list);
    }
}
