package de.upb.crypto.math.serialization;

import de.upb.crypto.math.serialization.converter.JSONConverter;

import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Representation of a {@code key -> value} map, mapping representations to representations.
 */
public class MapRepresentation extends Representation implements Iterable<Entry<Representation, Representation>> {
    private static final long serialVersionUID = -4276829583005855044L;
    private final HashMap<Representation, Representation> map = new HashMap<>();

    public MapRepresentation() {

    }

    public MapRepresentation(Map<Representation, Representation> map) {
        this.map.putAll(map);
    }

    /**
     * Adds {@code key -> value} to the mapping.
     * If key previously had another image, the old one is overwritten
     */
    public void put(Representation key, Representation value) {
        map.put(key, value);
    }

    /**
     * Returns an immutable view of this map.
     */
    public Map<Representation, Representation> getMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns a stream with the map's entry set as its source.
     */
    public Stream<Entry<Representation, Representation>> stream() {
        return map.entrySet().stream();
    }

    /**
     * Returns an iterator over this map's entry set.
     */
    @Override
    public Iterator<Entry<Representation, Representation>> iterator() {
        return map.entrySet().iterator();
    }

    /**
     * Applies the given consumer function to each key-value pair in this map.
     * @param consumer the consumer function to apply
     */
    public void forEach(BiConsumer<Representation, Representation> consumer) {
        for (Entry<Representation, Representation> e : getMap().entrySet())
            consumer.accept(e.getKey(), e.getValue());
    }

    /**
     * Applies the given consumer function to each key-value pair in a random order.
     * @param consumer the consumer function to apply
     */
    public void forEachRandomlyOrdered(BiConsumer<Representation, Representation> consumer) {
        ArrayList<Representation> keys = new ArrayList<>(map.keySet());
        Collections.shuffle(keys);

        for (Representation key : keys)
            consumer.accept(key, map.get(key));
    }

    /**
     * Returns the number of elements in this map.
     */
    public int size() {
        return map.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MapRepresentation other = (MapRepresentation) obj;
        if (map == null) {
            if (other.map != null)
                return false;
        } else if (!map.equals(other.map))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new JSONConverter().serialize(this);
    }
}
