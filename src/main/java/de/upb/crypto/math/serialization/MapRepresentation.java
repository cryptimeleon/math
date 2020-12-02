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
    private HashMap<Representation, Representation> map = new HashMap<>();

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
     * Returns the map backing this representation
     * (the returned map is not modifiable)
     */
    public Map<Representation, Representation> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public Stream<Entry<Representation, Representation>> stream() {
        return map.entrySet().stream();
    }

    @Override
    public Iterator<Entry<Representation, Representation>> iterator() {
        return map.entrySet().iterator();
    }

    public void forEach(BiConsumer<Representation, Representation> consumer) {
        for (Entry<Representation, Representation> e : getMap().entrySet())
            consumer.accept(e.getKey(), e.getValue());
    }

    public void forEachRandomlyOrdered(BiConsumer<Representation, Representation> consumer) {
        ArrayList<Representation> keys = new ArrayList<>(map.keySet());
        Collections.shuffle(keys);

        for (Representation key : keys)
            consumer.accept(key, map.get(key));
    }

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
