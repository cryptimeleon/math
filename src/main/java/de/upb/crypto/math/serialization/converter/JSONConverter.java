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
 * Allows converting between a {@code Representation} object and a JSON structure.
 * <p>
 * Note that the order of attributes in a JSON Object is meaningless. 
 * However, this {@code Converter} guarantees a consistent order between calls,
 * making the {@code Representation} -> {@code String} relation left-unique (i.e. a well-defined mapping).
 * This allows this {@code Converter} to be used for, e.g,. {@code HashRepresentationIntoStructure},
 * and similar tasks that require a unique and consistent output.
 */
public class JSONConverter extends Converter<String> {
    protected static final String BIG_INTEGER_PREFIX = "INT:";
    protected static final String BYTE_ARRAY_PREFIX = "BYTES:";
    protected static final String STRING_PREFIX = "STRING:";

    protected static final String OBJ_TYPE_KEY = "__type";
    protected static final String MAP_OBJ_TYPE = "MAP";
    protected static final String REPR_OBJ_TYPE = "REPR";
    protected static final String OBJ_OBJ_TYPE = "OBJ";

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
            return serializeObjectRepr((ObjectRepresentation) r);
        if (r instanceof StringRepresentation)
            return serializeString((StringRepresentation) r);
        if (r instanceof RepresentableRepresentation)
            return serializeRepresentable((RepresentableRepresentation) r);
        if (r instanceof MapRepresentation)
            return serializeMap((MapRepresentation) r);

        throw new IllegalArgumentException("Unknown type when serializing: " + r.getClass().getName());
    }

    private LinkedHashMap<String, Object> serializeObjectRepr(ObjectRepresentation r) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put(OBJ_TYPE_KEY, OBJ_OBJ_TYPE);
        r.forEachOrderedByKeys((key, value) -> {
                    if (key.startsWith(OBJ_TYPE_KEY))
                        key = OBJ_TYPE_KEY+key;
                    result.put(key, internalSerialize(value));
                }); //recursive call to serialize

        return result;
    }

    private LinkedHashMap<String, Object> serializeMap(MapRepresentation r) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put(OBJ_TYPE_KEY, MAP_OBJ_TYPE); //marker to differentiate between ObjectRepresentation, MapRepr, and RepresentableRepr (all use JSONObject)
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

        result.put("map", tuples);
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
        result.put(OBJ_TYPE_KEY, REPR_OBJ_TYPE);
        result.put("class", r.getRepresentedTypeName());
        result.put("repr", internalSerialize(r.getRepresentation()));

        return result;
    }

    private String serializeBigInteger(BigIntegerRepresentation r) {
        return BIG_INTEGER_PREFIX + r.get().toString(16);
    }

    private String serializeByteArray(ByteArrayRepresentation r) {
        return BYTE_ARRAY_PREFIX + Base64.getEncoder().encodeToString(r.get());
    }

    private String serializeString(StringRepresentation s) {
        return STRING_PREFIX+s.get();
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
            switch ((String) ((JSONObject) o).get(OBJ_TYPE_KEY)) {
                case REPR_OBJ_TYPE:
                    return deserializeRepresentable((JSONObject) o);
                case MAP_OBJ_TYPE:
                    return deserializeMap((JSONObject) o);
                case OBJ_OBJ_TYPE:
                    return deserializeObject((JSONObject) o);
            }
        }
        if (o instanceof JSONArray)
            return deserializeArray((JSONArray) o);
        if (o instanceof String) {
            if (((String) o).startsWith(BIG_INTEGER_PREFIX))
                return deserializeBigInteger((String) o);
            if (((String) o).startsWith(BYTE_ARRAY_PREFIX))
                return deserializeByteArray((String) o);
            if (((String) o).startsWith(STRING_PREFIX))
                return deserializeString((String) o);
            throw new IllegalArgumentException("Cannot handle type prefix of "+o);
        }

        throw new IllegalArgumentException("Unexpected JSON element");
    }

    private RepresentableRepresentation deserializeRepresentable(JSONObject o) {
        return new RepresentableRepresentation((String) o.get("class"), internalDeserialize(o.get("repr")));
    }

    private MapRepresentation deserializeMap(JSONObject o) {
        MapRepresentation result = new MapRepresentation();
        ((JSONArray) o.get("map")).forEach(
                x -> result.put(internalDeserialize(((JSONArray) x).get(0)), internalDeserialize(((JSONArray) x).get(1)))
        );
        return result;
    }

    private StringRepresentation deserializeString(String o) {
        return new StringRepresentation(o.substring(STRING_PREFIX.length()));
    }

    private BigIntegerRepresentation deserializeBigInteger(String o) {
        return new BigIntegerRepresentation(new BigInteger(o.substring(BIG_INTEGER_PREFIX.length()), 16));
    }

    private ByteArrayRepresentation deserializeByteArray(String o) {
        return new ByteArrayRepresentation(Base64.getDecoder().decode(o.substring(BYTE_ARRAY_PREFIX.length())));
    }

    private ListRepresentation deserializeArray(JSONArray o) {
        ListRepresentation result = new ListRepresentation();
        o.forEach(x -> result.put(internalDeserialize(x)));
        return result;
    }

    private ObjectRepresentation deserializeObject(JSONObject o) {
        ObjectRepresentation result = new ObjectRepresentation();
        o.forEach((k, v) -> {
            String key = (String) k;
            if (key.startsWith(OBJ_TYPE_KEY))
                key = key.substring(OBJ_TYPE_KEY.length()); //remove the (additional) OBJ_TYPE_KEY prefix that's added by serialization to all keys that start with OBJ_TYPE_KEY.
            if (!key.isEmpty()) //skip the OBJ_TYPE_KEY field added during serialization to distinguish different JSON objects' correspondence to Representation classes.
                result.put(key, internalDeserialize(v));
        });
        return result;
    }
}
