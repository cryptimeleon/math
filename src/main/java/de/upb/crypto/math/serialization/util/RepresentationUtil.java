package de.upb.crypto.math.serialization.util;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Structure;
import de.upb.crypto.math.serialization.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated //will be removed eventually. Superseded by ReprUtil and its annotation-based framwork.
public class RepresentationUtil {

    /**
     * Shorthand for repr.put(field, caller.getField().toRepresentation()),
     * where getField() returns a Structure or Mapping, or some other Representable where you don't need any further information to recreate.
     * Also works for BigInteger and Strings, ... (see putValue())
     *
     * @param caller typically "this". Object must declare getter and setter for field.
     * @param repr   the ObjectRepresentation to put the Representation into. May be null, then it's created.
     * @param field  the name of caller's object member with getter and setter getField() and setField(...)
     * @return repr
     */
    public static ObjectRepresentation putStandaloneRepresentable(Object caller, ObjectRepresentation repr, String field) {
        if (repr == null)
            repr = new ObjectRepresentation();

        try {
            Object value = caller.getClass().getMethod("get" + String.valueOf(field.charAt(0)).toUpperCase() + field.substring(1, field.length()), (Class<?>[]) null).invoke(caller, new Object[0]); //invoking getField()
            return putValue(repr, field, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Shorthand for repr.put(field, caller.getField().toRepresentation()), where caller.getField() returns an Element
     *
     * @param caller typically "this". Object must declare getter and setter for field.
     * @param repr   the ObjectRepresentation to put the Representation into. May be null, then it's created.
     * @param field  the name of caller's object member with getter and setter getField() and setField(...)
     * @return repr
     */
    public static ObjectRepresentation putElement(Object caller, ObjectRepresentation repr, String field) {
        if (repr == null)
            repr = new ObjectRepresentation();

        try {
            Object value = caller.getClass().getMethod("get" + String.valueOf(field.charAt(0)).toUpperCase() + field.substring(1, field.length()), (Class<?>[]) null).invoke(caller, new Object[0]); //invoking getField()
            return putValue(repr, field, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Shorthand for base.put(name, value.toRepresentation()).
     * Private for now, since I'm not sure that this handles all cases gracefully.
     */
    private static ObjectRepresentation putValue(ObjectRepresentation base, String name, Object value) {
        if (base == null)
            base = new ObjectRepresentation();

        if (value == null)
            base.put(name, null);
        else if (value instanceof BigInteger)
            base.put(name, new BigIntegerRepresentation((BigInteger) value));
        else if (value instanceof Element)
            base.put(name, ((Element) value).getRepresentation());
        else if (value instanceof Representable)
            base.put(name, new RepresentableRepresentation((Representable) value));
        else if (value instanceof String)
            base.put(name, new StringRepresentation((String) value));
        else if (value instanceof byte[])
            base.put(name, new ByteArrayRepresentation((byte[]) value));
		/*else if (value instanceof List<?>) { //this is commented out as in restoreStandaloneRepresentable, we cannot reverse this, as we don't know the concrete type of the list
			int i=0;
			ObjectRepresentation listDummy = new ObjectRepresentation(); //saving list values in this dummy first (for recursive putValue-call), then I'll make it a proper list.
			for (Object v : (List<?>) value)
				putValue(listDummy, (i++)+"", v);
			ListRepresentation representedList = new ListRepresentation();
			for (int j=0;j<i;j++)
				representedList.put(listDummy.get(j+""));
			base.put(name, representedList);
		}*/
        else
            throw new IllegalArgumentException("Don't know how represent " + value);

        return base;
    }

    /**
     * Shorthand for (pseudocode) caller.setField(repr.get(field).toJavaObject())
     * (where toJavaObject() is the default RepresentationToJavaObjectHelper mechanism)
     */
    public static void restoreStandaloneRepresentable(Object caller, Representation repr, String field) {
        //Get Representation of "field" from repr
        Representation r = ((ObjectRepresentation) repr).get(field);

        //Depending on the Representation type, choose a value
        Object value = null;
        if (r == null)
            value = null;
        else if (r instanceof BigIntegerRepresentation)
            value = r.bigInt().get();
        else if (r instanceof RepresentableRepresentation)
            value = r.repr().recreateRepresentable();
        else if (r instanceof StringRepresentation)
            value = r.str().get();
        else if (r instanceof ByteArrayRepresentation)
            value = r.bytes().get();
        else
            throw new IllegalArgumentException("Don't know how to restore " + r);

        //Call setter
        callSetter(caller, field, value);
    }

    /**
     * Shorthand for (pseudocode) caller.setField(structure.getElement(repr.get(field)))
     * (where toJavaObject() is the default RepresentationToJavaObjectHelper mechanism)
     */
    public static void restoreElement(Object caller, Representation repr, String field, Structure structure) {
        //Get value from repr
        Representation r = (Representation) ((ObjectRepresentation) repr).get(field);
        Object value = structure.getElement(r);

        callSetter(caller, field, value);
    }

    /**
     * Calls caller.setField(valueToSet) via reflection
     */
    private static void callSetter(Object caller, String field, Object valueToSet) {
        String setterName = "set" + String.valueOf(field.charAt(0)).toUpperCase() + field.substring(1, field.length());
        try {
            for (Method method : caller.getClass().getMethods()) {
                if (method.getName().equals(setterName)) {
                    method.invoke(caller, valueToSet);
                    return;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error calling setter " + setterName, e);
        }
        throw new IllegalArgumentException("Could not find setter for " + field);
    }


    public static <T extends StandaloneRepresentable, U extends Representable> MapRepresentation representMapOfLists(Map<T, U[]> map) {
        MapRepresentation mr = new MapRepresentation();
        for (Map.Entry<T, U[]> entry : map.entrySet()) {
            ListRepresentation list = new ListRepresentation();
            for (U e : entry.getValue()) {
                list.put(e.getRepresentation());
            }
            mr.put(new RepresentableRepresentation(entry.getKey()), list);
        }

        return mr;
    }


    public static <T extends StandaloneRepresentable, U extends Element> Map<T, List<U>> recreateMapOfLists(Representation r, Structure s) {
        MapRepresentation mr = (MapRepresentation) r;
        Map<T, List<U>> map = new HashMap<>();

        for (Map.Entry<Representation, Representation> entry : mr.getMap().entrySet()) {
            T a = (T) ((RepresentableRepresentation) entry.getKey()).recreateRepresentable();

            ListRepresentation lr = ((ListRepresentation) entry.getValue());
            ArrayList<U> list = new ArrayList<U>();

            for (Representation er : lr) {
                list.add((U) s.getElement(er));
            }


            map.put(a, list);
        }
        return map;
    }
}
