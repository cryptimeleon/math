package de.upb.crypto.math.serialization;

import de.upb.crypto.math.serialization.converter.JSONConverter;

import java.util.*;
import java.util.stream.Stream;

/**
 * Representation of an ordered list of Representations
 */
public class ListRepresentation extends Representation implements Iterable<Representation> {
    private static final long serialVersionUID = -7782043563152429201L;
    protected List<Representation> list = new ArrayList<>();

    /**
     * Creates an empty list representation
     */
    public ListRepresentation() {

    }

    public ListRepresentation(List<Representation> list) {
        this.list.addAll(list);
    }

    public ListRepresentation(Representation... list) {
        this.list.addAll(Arrays.asList(list));
    }

    public Representation get(int i) {
        return list.get(i);
    }

    public int size() {
        return list.size();
    }

    public void put(Representation value) {
        list.add(value);
    }

    public void add(Representation value) {
        put(value);
    }

    public List<Representation> getList() {
        return Collections.unmodifiableList(list);
    }

    public Representation[] getArray() {
        return list.toArray(new Representation[list.size()]);
    }

    @Override
    public Iterator<Representation> iterator() {
        return list.iterator();
    }

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
