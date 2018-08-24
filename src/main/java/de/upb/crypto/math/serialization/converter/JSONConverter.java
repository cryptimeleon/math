package de.upb.crypto.math.serialization.converter;

import de.upb.crypto.math.serialization.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Allows converting between a Representation object and a JSON structure.
 * <p>
 * Note that the order of attributes in a JSON Object is meaningless. However, this converter guarantees a consistent order between calls,
 * making the Representation -> String relation left-unique (i.e. a well-defined mapping).
 * (This allows this Converter to be used for, e.g,. HashRepresentationIntoStructure, and similar tasks that require a unique and consistent output)
 */
public class JSONConverter extends Converter<String> {
    public static final String BIG_INTEGER_PREFIX = "<BigInteger>";
    public static final String BYTE_ARRAY_PREFIX = "<BinaryData>";

    @Override
    public String serialize(Representation r) { // Dispatch by type of Representation
        return JSONValue.toJSONString(internalSerialize(r));
    }

    protected Object internalSerialize(Representation r) {
        if (r == null)
            return null;
        if (r instanceof BigIntegerRepresentation)
            return serializeBigInteger((BigIntegerRepresentation) r);
        if (r instanceof ByteArrayRepresentation)
            return serializeByteArray((ByteArrayRepresentation) r);
        if (r instanceof ListRepresentation)
            return serializeList((ListRepresentation) r);
        if (r instanceof ObjectRepresentation)
            return serializeNamedAttributes((ObjectRepresentation) r);
        if (r instanceof StringRepresentation)
            return serializeString((StringRepresentation) r);
        if (r instanceof BooleanRepresentation)
            return serializeBoolean((BooleanRepresentation) r);
        if (r instanceof IntegerRepresentation)
            return serializeInteger((IntegerRepresentation) r);
        if (r instanceof RepresentableRepresentation)
            return serializeRepresentable((RepresentableRepresentation) r);
        if (r instanceof MapRepresentation)
            return serializeMap((MapRepresentation) r);

        throw new IllegalArgumentException("Unknown type when serializing: " + r.getClass().getName());
    }

    private Boolean serializeBoolean(BooleanRepresentation r) {
        return r.get();
    }

    private Long serializeInteger(IntegerRepresentation r) {
        return r.get();
    }

    private LinkedHashMap<String, Object> serializeNamedAttributes(ObjectRepresentation r) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        r.getMap().entrySet().stream()
                .sorted(Comparator.comparing(Entry::getKey))
                .forEachOrdered(x -> result.put(x.getKey(), internalSerialize(x.getValue()))); //recursive call to serialize

        return result;
    }

    private LinkedHashMap<String, Object> serializeMap(MapRepresentation r) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("thisIsMapRepresentation", true); //marker to differentiate between ObjectRepresentation, MapRepr, and RepresentableRepr (all use JSONObject)
        JSONArray tuples = new JSONArray();

        r.getMap().entrySet().stream()
                .sorted( //sorting for consistent mapping behavior (i.e. two equal Representations will always have the same JSON text)
                        Comparator.comparing((Entry<Representation, Representation> x) -> x.getKey().toString())
                                .thenComparing((Entry<Representation, Representation> x) -> x.getValue().toString())
                )
                .forEachOrdered(x -> {
                    JSONArray tuple = new JSONArray();
                    tuple.add(internalSerialize(x.getKey())); //recursive call to serialize
                    tuple.add(internalSerialize(x.getValue())); //recursive call to serialize
                    tuples.add(tuple);
                });

        result.put("tuples", tuples);
        return result;
    }

    private JSONArray serializeList(ListRepresentation r) {
        JSONArray result = new JSONArray();
        for (Representation x : r)
            result.add(internalSerialize(x));
        return result;
    }

    private LinkedHashMap<String, Object> serializeRepresentable(RepresentableRepresentation r) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("thisIsRepresentableRepresentation", true); //marker to differentiate between ObjectRepresentation and RepresentableRepr (both use JSONObject)
        result.put("representableTypeName", r.getRepresentedTypeName());
        result.put("representation", internalSerialize(r.getRepresentation()));

        return result;
    }

    private String serializeBigInteger(BigIntegerRepresentation r) {
        return BIG_INTEGER_PREFIX + r.get().toString(16);
    }

    private String serializeByteArray(ByteArrayRepresentation r) {
        return BYTE_ARRAY_PREFIX + Base64.getEncoder().encodeToString(r.get());
    }

    private String serializeString(StringRepresentation s) {
        return s.get();
    }

    @Override
    public Representation deserialize(String s) {
        try {
            return internalDeserialize(new JSONParser().parse(s));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error when parsing JSON: " + s);
        }
    }

    protected Representation internalDeserialize(Object o) {
        if (o == null)
            return null;
        if (o instanceof JSONObject) {
            if (((JSONObject) o).containsKey("thisIsRepresentableRepresentation"))
                return deserializeRepresentable((JSONObject) o);
            else if (((JSONObject) o).containsKey("thisIsMapRepresentation"))
                return deserializeMap((JSONObject) o);
            else
                return deserializeObject((JSONObject) o);
        }
        if (o instanceof JSONArray)
            return deserializeArray((JSONArray) o);
        if (o instanceof String && ((String) o).startsWith(BIG_INTEGER_PREFIX))
            return deserializeBigInteger((String) o);
        if (o instanceof String && ((String) o).startsWith(BYTE_ARRAY_PREFIX))
            return deserializeByteArray((String) o);
        if (o instanceof String)
            return deserializeString((String) o);
        if (o instanceof Boolean)
            return deserializeBoolean((Boolean) o);
        if (o instanceof Long)
            return deserializeInteger((Long) o);

        throw new IllegalArgumentException("Unexpected JSON element");
    }

    private RepresentableRepresentation deserializeRepresentable(JSONObject o) {
        return new RepresentableRepresentation((String) o.get("representableTypeName"), internalDeserialize(o.get("representation")));
    }

    private MapRepresentation deserializeMap(JSONObject o) {
        MapRepresentation result = new MapRepresentation();
        ((JSONArray) o.get("tuples")).forEach(
                x -> result.put(internalDeserialize(((JSONArray) x).get(0)), internalDeserialize(((JSONArray) x).get(1)))
        );
        return result;
    }

    private Representation deserializeBoolean(Boolean o) {
        return new BooleanRepresentation(o);
    }


    private Representation deserializeInteger(Long o) {
        return new IntegerRepresentation(o);
    }

    private StringRepresentation deserializeString(String o) {
        return new StringRepresentation(o);
    }

    private BigIntegerRepresentation deserializeBigInteger(String o) {
        return new BigIntegerRepresentation(new BigInteger(o.substring(BIG_INTEGER_PREFIX.length()), 16));
    }

    private ByteArrayRepresentation deserializeByteArray(String o) {
        return new ByteArrayRepresentation(Base64.getDecoder().decode(o.substring(BIG_INTEGER_PREFIX.length())));
    }

    private ListRepresentation deserializeArray(JSONArray o) {
        ListRepresentation result = new ListRepresentation();
        o.forEach(x -> result.put(internalDeserialize(x)));
        return result;
    }

    private ObjectRepresentation deserializeObject(JSONObject o) {
        ObjectRepresentation result = new ObjectRepresentation();
        o.forEach((k, v) -> result.put((String) k, internalDeserialize(v)));
        return result;
    }
}
