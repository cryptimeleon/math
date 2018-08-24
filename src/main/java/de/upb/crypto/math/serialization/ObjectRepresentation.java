package de.upb.crypto.math.serialization;

import de.upb.crypto.math.serialization.converter.JSONConverter;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * Representation based of a simple String -> Representation map.
 */
public class ObjectRepresentation extends Representation implements Iterable<Entry<String, Representation>> {
    private static final long serialVersionUID = -7955738062171729317L;
    protected Map<String, Representation> map = new HashMap<>();

    public ObjectRepresentation() {

    }

    /**
     * Put an attribute.
     */
    public void put(String key, Representation value) {
        map.put(key, value);
    }

    /**
     * Get an attribute.
     */
    public Representation get(String name) {
        return map.get(name);
    }

    /**
     * Get the backing map to this Representation
     */
    public Map<String, Representation> getMap() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Iterator<Entry<String, Representation>> iterator() {
        return map.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).iterator();
    }

    public Stream<Entry<String, Representation>> stream() {
        return map.entrySet().stream();
    }

    @Override
    public String toString() {
        return new JSONConverter().serialize(this);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) { //Eclipse generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectRepresentation other = (ObjectRepresentation) obj;
        return this.map.equals(other.map);
    }
}
