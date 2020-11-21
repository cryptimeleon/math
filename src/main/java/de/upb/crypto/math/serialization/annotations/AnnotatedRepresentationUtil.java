package de.upb.crypto.math.serialization.annotations;

import de.upb.crypto.math.serialization.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

@Deprecated
public class AnnotatedRepresentationUtil {


    // list containing all annotations used to mark fields contained in representations
    // Add new newly introduced represented annotation classes to this list
    private static final List<Class<? extends Annotation>> annotationClassList = Arrays.asList(
            Represented.class,
            RepresentedArray.class,
            RepresentedMap.class,
            RepresentedList.class,
            RepresentedMapAndList.class,
            RepresentedMapAndMap.class,
            RepresentedSet.class);

    /**
     * DESERIALIZATION
     */

    public static void restoreAnnotatedRepresentation(ObjectRepresentation repr, Object instance) {

        for (Entry<String, Representation> entry : repr) {

            Class<?> clazz = instance.getClass();
            while (!clazz.equals(Object.class)) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    restoreField(field, repr, entry.getValue(), instance);
                    break;
                } catch (NoSuchFieldException e) {
                    // Not put by this -> move on
                } catch (InvocationTargetException | NoSuchMethodException | SecurityException |
                        IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                } finally {
                    clazz = clazz.getSuperclass();
                }

            }

        }
    }

    private static void restoreField(Field field, ObjectRepresentation value, Representation representation, Object
            instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        boolean access = field.isAccessible();
        field.setAccessible(true);

        //Don't try to recreate fields, which representation is null
        if (value.get(field.getName()) != null) {
            if (field.isAnnotationPresent(Represented.class)) {
                setRepresentedField(field, value, representation, instance);
            } else if (field.isAnnotationPresent(RepresentedList.class)) {
                setRepresentedList(field, value, representation, instance);
            } else if (field.isAnnotationPresent(RepresentedSet.class)) {
                setRepresentedSet(field, value, representation, instance);
            } else if (field.isAnnotationPresent(RepresentedMap.class)) {
                setRepresentedMap(field, value, representation, instance);
            } else if (field.isAnnotationPresent(RepresentedMapAndList.class)) {
                setRepresentedMapAndList(field, value, representation, instance);
            } else if (field.isAnnotationPresent(RepresentedMapAndMap.class)) {
                setRepresentedMapAndMap(field, value, representation, instance);
            } else if (field.isAnnotationPresent(RepresentedArray.class)) {
                setRepresentedArray(field, value, representation, instance);
            }
        }

        field.setAccessible(access);
    }

    private static void setRepresentedArray(Field field, ObjectRepresentation value, Representation representation,
                                            Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        ListRepresentation list = representation.list();
        RepresentedArray annotation = field.getAnnotation(RepresentedArray.class);
        Object[] toReturn = (Object[]) Array.newInstance(field.getType().getComponentType(), list.size());
        int i = 0;
        for (Representation r : list) {
            toReturn[i] = restoreRepresentedField(annotation.elementRestorer(), value, r, instance);
            i++;
        }
        field.set(instance, toReturn);
    }

    @SuppressWarnings("unchecked")
    private static void setRepresentedMapAndMap(Field field, ObjectRepresentation value, Representation
            representation, Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        MapRepresentation mapRepresentation = representation.map();
        RepresentedMapAndMap annotation = field.getAnnotation(RepresentedMapAndMap.class);
        Map<Object, Map<Object, Object>> map = null;
        try {
            map = (Map<Object, Map<Object, Object>>) field.getType().getConstructor().newInstance();
        } catch (NoSuchMethodException e2) {
            map = new LinkedHashMap<>();
        }

        //Use reflection magic to find out the type of the inner map
        Class innerMapClass = null;
        try {
            innerMapClass = (Class) ((ParameterizedType) ((ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[1]).getRawType();
        } catch (ClassCastException e2) {
            innerMapClass = LinkedHashMap.class;
        }

        //Restore objects
        for (Entry<Representation, Representation> e : mapRepresentation) {
            Object key = restoreRepresentedField(annotation.keyRestorer(), value, e.getKey(), instance);
            Map<Object, Object> innerMap = null;
            try {
                innerMap = (Map<Object, Object>) innerMapClass.getConstructor().newInstance();
            } catch (NoSuchMethodException | ClassCastException e2) {
                innerMap = new LinkedHashMap<>();
            }
            MapRepresentation innerMapRepresentation = e.getValue().map();
            RepresentedMap mapAnnotation = annotation.valueRestorer();
            for (Entry<Representation, Representation> innerEntry : innerMapRepresentation) {
                Object innerKey = restoreRepresentedField(mapAnnotation.keyRestorer(), value, innerEntry.getKey(),
                        instance);
                Object innerValue = restoreRepresentedField(mapAnnotation.valueRestorer(), value, innerEntry.getValue
                        (), instance);
                innerMap.put(innerKey, innerValue);
            }
            map.put(key, innerMap);
        }
        field.set(instance, map);
    }

    @SuppressWarnings("unchecked")
    private static void setRepresentedMapAndList(Field field, ObjectRepresentation value, Representation
            representation, Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        MapRepresentation mapRepresentation = representation.map();
        RepresentedMapAndList annotation = field.getAnnotation(RepresentedMapAndList.class);
        Map<Object, List<Object>> map = null;
        try {
            map = (Map<Object, List<Object>>) field.getType().getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            map = new LinkedHashMap<>();
        }

        //Use reflection magic to find out the type of the inner map
        Class innerListClass = null;
        try {
            innerListClass = (Class) ((ParameterizedType) ((ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[1]).getRawType();
        } catch (ClassCastException e2) {
            innerListClass = ArrayList.class;
        }

        //Restore inner lists
        for (Entry<Representation, Representation> e : mapRepresentation) {
            Object key = restoreRepresentedField(annotation.keyRestorer(), value, e.getKey(), instance);
            List<Object> list = null;
            try {
                list = (List<Object>) innerListClass.getConstructor().newInstance();
            } catch (NoSuchMethodException | ClassCastException e2) {
                list = new ArrayList<>();
            }
            ListRepresentation listRepresentation = e.getValue().list();
            RepresentedList listAnnotation = annotation.valueRestorer();
            for (Representation repr : listRepresentation) {
                Object obj = restoreRepresentedField(listAnnotation.elementRestorer(), value, repr, instance);
                list.add(obj);
            }
            map.put(key, list);
        }
        field.set(instance, map);

    }

    @SuppressWarnings("unchecked")
    private static void setRepresentedMap(Field field, ObjectRepresentation value, Representation representation,
                                          Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        MapRepresentation mapRepresentation = representation.map();
        RepresentedMap annotation = field.getAnnotation(RepresentedMap.class);
        Map<Object, Object> map = new HashMap<>();
        try {
            map = (Map<Object, Object>) field.getType().getConstructor().newInstance();
        } catch (NoSuchMethodException e) { //fall back to HashMap (e.g., if the field type is just Map<>)
            map = new HashMap<>();
        }
        for (Entry<Representation, Representation> entry : mapRepresentation) {
            Object key = restoreRepresentedField(annotation.keyRestorer(), value, entry.getKey(), instance);
            Object kValue = restoreRepresentedField(annotation.valueRestorer(), value, entry.getValue(), instance);
            map.put(key, kValue);
        }
        field.set(instance, map);

    }

    @SuppressWarnings("unchecked")
    private static void setRepresentedList(Field field, ObjectRepresentation value, Representation representation,
                                           Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        ListRepresentation listRepresentation = representation.list();
        RepresentedList annotation = field.getAnnotation(RepresentedList.class);
        List<Object> list = null;
        try {
            list = (List<Object>) field.getType().getConstructor().newInstance();
        } catch (NoSuchMethodException e) { //fall back to ArrayList (e.g., if the field type is just List<>)
            list = new ArrayList<>();
        }

        for (Representation repr : listRepresentation) {

            Object obj = restoreRepresentedField(annotation.elementRestorer(), value, repr, instance);
            list.add(obj);
        }
        field.set(instance, list);
    }

    @SuppressWarnings("unchecked")
    private static void setRepresentedSet(Field field, ObjectRepresentation value, Representation representation,
                                          Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        ListRepresentation setRepresentation = representation.list();
        RepresentedSet annotation = field.getAnnotation(RepresentedSet.class);
        Set<Object> set = null;
        try {
            set = (Set<Object>) field.getType().getConstructor().newInstance();
        } catch (NoSuchMethodException e) { //fall back to LinkedHashSet (e.g., if the field type is just Set<>)
            set = new LinkedHashSet<>();
        }

        for (Representation repr : setRepresentation) {

            Object obj = restoreRepresentedField(annotation.elementRestorer(), value, repr, instance);
            set.add(obj);
        }
        field.set(instance, set);
    }

    /**
     * Deserializes and stores a private member variable.
     *
     * @param field          the member variable
     * @param value          the ObjectRepresentation where a parent structure can be found
     * @param representation the representation of this' object value
     * @param instance       the object that contains the field
     * @throws IllegalArgumentException  -
     * @throws IllegalAccessException    -
     * @throws NoSuchFieldException      if the parents field cannot be found (i.e. the structure is wrong!)
     * @throws SecurityException         -
     * @throws InvocationTargetException if the parent was unable to recover this representation
     * @throws NoSuchMethodException     if this representation cannot be restored by the parent (i.e. the
     *                                   RepresentedType is wrong!)
     */
    private static void setRepresentedField(Field field, ObjectRepresentation value, Representation representation,
                                            Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        Object object = restoreRepresentedField(field.getAnnotation(Represented.class), value, representation,
                instance);
        field.set(instance, object);
    }

    /**
     * Restores the object specified by this representation & annotation and returns it
     *
     * @param annotation     the annotation of the field of this object
     * @param value          the ObjectRepresentation where a parent structure can be found
     * @param representation the representation of this object
     * @param instance       the instance of the object that contains the field which is restored here
     * @return the deserialized object
     * @throws IllegalArgumentException  -
     * @throws IllegalAccessException    -
     * @throws NoSuchFieldException      if the parents field cannot be found (i.e. the structure is wrong!)
     * @throws SecurityException         -
     * @throws InvocationTargetException if the parent was unable to recover this representation
     * @throws NoSuchMethodException     if this representation cannot be restored by the parent (i.e. the
     *                                   RepresentedType is wrong!)
     */
    private static Object restoreRepresentedField(Represented annotation, ObjectRepresentation value,
                                                  Representation representation, Object instance)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {

        String structure = annotation.structure();

        if (annotation.recoveryMethod().equals("")) {
            return restorePrimitive(representation);
        } else {
            return restoreEntity(value, representation, instance, structure, annotation.recoveryMethod());
        }
    }

    /**
     * Deserializes a representation of an object that does not need any information for it's deserialization.
     *
     * @param repr the representation
     * @return the deserialized object
     */
    private static Object restorePrimitive(Representation repr) {

        if (repr == null) {
            return null;
        }

        if (repr instanceof RepresentableRepresentation) {
            return repr.repr().recreateRepresentable();
        }

        if (repr instanceof BigIntegerRepresentation) {
            return repr.bigInt().get();
        }

        if (repr instanceof StringRepresentation) {
            return repr.str().get();
        }

        if (repr instanceof ByteArrayRepresentation) {
            return repr.bytes().get();
        }

        throw new InternalError("Don't know how to recreate " + repr.getClass().getName());
    }

    /**
     * Calls a method on a parent structure to restore an entity.
     *
     * @param value          the ObjectRepresentation where a parent structure can be found
     * @param representation the representation of this' object value
     * @param instance       the object that contains the field
     * @param structure      the name of the parents structure (field name)
     * @param methodName     the method to call on the parent structure to restore this element
     * @throws IllegalArgumentException  -
     * @throws IllegalAccessException    -
     * @throws NoSuchFieldException      if the parents field cannot be found (i.e. the name of the structure is wrong!)
     * @throws SecurityException         -
     * @throws InvocationTargetException if the parent was unable to recover this representation
     * @throws NoSuchMethodException     if this representation cannot be restored by the parent (i.e. the
     *                                   RepresentedType is wrong!)
     */
    private static Object restoreEntity(ObjectRepresentation value, Representation representation, Object instance,
                                        String structure, String methodName)
            throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException, InstantiationException {
        Field structureField = null;
        Class<?> clazz = instance.getClass();

        while (!clazz.equals(Object.class) && structureField == null) {
            try {
                structureField = clazz.getDeclaredField(structure);
            } catch (NoSuchFieldException e) {
                //No matching field found, continue search in super class
            } finally {
                clazz = clazz.getSuperclass();
            }
        }

        if (structureField == null) {
            throw new RuntimeException("Unable to find field \"" + structure + "\" in " + instance.getClass().getName
                    ());
        }

        boolean access = structureField.isAccessible();
        structureField.setAccessible(true);
        Object structureInstance = structureField.get(instance);

        if (structureInstance == null) {
            restoreField(structureField, value, value.get(structure), instance);
            structureInstance = structureField.get(instance);
            if (structureInstance == null) {
                throw new RuntimeException("Unable to load and/or recreate structure " + structure);
            }
        }
        structureField.setAccessible(access);
        return structureInstance.getClass().getMethod(methodName, Representation.class).invoke(structureInstance,
                representation);

    }

    /**
     * SERIALIZATION
     */

    public static ObjectRepresentation putAnnotatedRepresentation(ObjectRepresentation repr, Object instance) {
        try {
            Class<?> clazz = instance.getClass();
            while (!clazz.equals(Object.class)) {

                for (Field field : clazz.getDeclaredFields()) {
                    boolean access = field.isAccessible();
                    field.setAccessible(true);

                    Object fieldValue = field.get(instance);

                    if (hasRepresentedTypeAnnotation(field)) {
                        //Keep null values
                        if (fieldValue == null) {
                            repr.put(field.getName(), null);
                        } else {
                            if (field.isAnnotationPresent(Represented.class)) {
                                AnnotatedRepresentationUtil.putRepresentedField(field, fieldValue, repr);
                            } else if (field.isAnnotationPresent(RepresentedList.class)) {
                                AnnotatedRepresentationUtil.putRepresentedList(field, fieldValue, repr);
                            } else if (field.isAnnotationPresent(RepresentedSet.class)) {
                                AnnotatedRepresentationUtil.putRepresentedSet(field, fieldValue, repr);
                            } else if (field.isAnnotationPresent(RepresentedMap.class)) {
                                AnnotatedRepresentationUtil.putRepresentedMap(field, fieldValue, repr);
                            } else if (field.isAnnotationPresent(RepresentedMapAndList.class)) {
                                AnnotatedRepresentationUtil.putRepresentedMapAndList(field, fieldValue, repr);
                            } else if (field.isAnnotationPresent(RepresentedMapAndMap.class)) {
                                AnnotatedRepresentationUtil.putRepresentedMapAndMap(field, fieldValue, repr);
                            } else if (field.isAnnotationPresent(RepresentedArray.class)) {
                                putRepresentedArray(field, fieldValue, repr);
                            }
                        }
                    }
                    field.setAccessible(access);
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            throw new RuntimeException(e);
        }
        return repr;
    }

    private static void putRepresentedArray(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        ListRepresentation list = new ListRepresentation();
        Object[] array = (Object[]) fieldValue;
        for (Object element : array) {
            list.put(getRepresentationForField(element));
        }
        repr.put(field.getName(), list);
    }

    private static void putRepresentedMapAndMap(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        @SuppressWarnings("unchecked")
        Map<?, Map<?, ?>> mapAndList = (Map<?, Map<?, ?>>) fieldValue;
        MapRepresentation mapRepr = new MapRepresentation();
        for (Entry<?, Map<?, ?>> e : mapAndList.entrySet()) {
            MapRepresentation innerRepr = new MapRepresentation();
            for (Entry<?, ?> innerEntry : e.getValue().entrySet()) {
                innerRepr.put(getRepresentationForField(innerEntry.getKey()), getRepresentationForField(innerEntry
                        .getValue()));
            }
            mapRepr.put(getRepresentationForField(e.getKey()), innerRepr);
        }
        repr.put(field.getName(), mapRepr);
    }

    private static void putRepresentedMapAndList(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        @SuppressWarnings("unchecked")
        Map<?, List<?>> mapAndList = (Map<?, List<?>>) fieldValue;
        MapRepresentation mapRepr = new MapRepresentation();
        for (Entry<?, List<?>> e : mapAndList.entrySet()) {
            ListRepresentation listRepr = new ListRepresentation();
            for (Object o : e.getValue()) {
                listRepr.put(getRepresentationForField(o));
            }
            mapRepr.put(getRepresentationForField(e.getKey()), listRepr);
        }
        repr.put(field.getName(), mapRepr);

    }

    public static ObjectRepresentation putAnnotatedRepresentation(Object instance) {
        return putAnnotatedRepresentation(new ObjectRepresentation(), instance);
    }

    private static void putRepresentedList(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        List<?> list = (List<?>) fieldValue;
        ListRepresentation listRepresentation = new ListRepresentation();
        for (Object o : list) {
            listRepresentation.put(getRepresentationForField(o));
        }
        repr.put(field.getName(), listRepresentation);
    }


    private static void putRepresentedSet(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        Set<?> list = (Set<?>) fieldValue;
        ListRepresentation setRepresentation = new ListRepresentation();
        for (Object o : list) {
            setRepresentation.put(getRepresentationForField(o));
        }
        repr.put(field.getName(), setRepresentation);
    }

    private static void putRepresentedMap(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        Map<?, ?> map = (Map<?, ?>) fieldValue;
        MapRepresentation mapRepresentation = new MapRepresentation();
        for (Entry<?, ?> entry : map.entrySet()) {
            mapRepresentation.put(getRepresentationForField(entry.getKey()), getRepresentationForField(entry.getValue
                    ()));
        }
        repr.put(field.getName(), mapRepresentation);
    }

    private static void putRepresentedField(Field field, Object fieldValue, ObjectRepresentation repr) throws
            IllegalArgumentException {
        repr.put(field.getName(), getRepresentationForField(fieldValue));
    }

    private static Representation getRepresentationForField(Object fieldValue) throws IllegalArgumentException {

        if (fieldValue == null) {
            return null;
        }

        if (fieldValue instanceof Enum) {
            Enum enumValue = (Enum) fieldValue;
            return new RepresentableRepresentation(enumValue);
        }

        if (fieldValue instanceof StandaloneRepresentable) {
            Representable repr = (Representable) fieldValue;
            return new RepresentableRepresentation(repr);
        }

        if (fieldValue instanceof Representable) {
            Representable repr = (Representable) fieldValue;
            return repr.getRepresentation();
        }

        if (fieldValue instanceof BigInteger) {
            BigInteger bigInt = (BigInteger) fieldValue;
            return new BigIntegerRepresentation(bigInt);
        }

        if (fieldValue instanceof Integer) {
            Integer integer = (Integer) fieldValue;
            return new BigIntegerRepresentation(integer);
        }

        if (fieldValue instanceof String) {
            String string = (String) fieldValue;
            return new StringRepresentation(string);
        }

        if (fieldValue instanceof Boolean) {
            Boolean bool = (Boolean) fieldValue;
            return new BigIntegerRepresentation(bool ? 1 : 0);
        }

        if (fieldValue instanceof byte[]) {
            byte[] bytes = (byte[]) fieldValue;
            return new ByteArrayRepresentation(bytes);
        }

        throw new InternalError("Don't know how to represent " + fieldValue.getClass().getName());
    }

    public static void restoreAnnotatedRepresentation(Representation repr, Object instance) {
        restoreAnnotatedRepresentation(repr.obj(), instance);

    }

    private static boolean hasRepresentedTypeAnnotation(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        if (annotations == null || annotations.length == 0) {
            return false;
        }
        return Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .anyMatch(a -> annotationClassList.parallelStream().anyMatch(a::isAssignableFrom));
    }
}
