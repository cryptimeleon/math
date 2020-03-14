package de.upb.crypto.math.serialization.annotations.v2;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.internal.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Allows for easy Representation handling.
 *
 * Typical usage example:
 * - Your class is StandaloneRepresentable, i.e. you need to give a constructor with Representation argument and a getRepresentation() method.
 * - In your class, annotate all fields that need to be part of the Representation with @Represented. (e.g., @Represented private Group group)
 * - If some objects need help being deserialized (e.g., GroupElements are deserialized by their group), define a restorer in the @Represented annotation,
 *   e.g., @Represented(restorer="group") private GroupElement x;
 *   The restorer can either be another field of your class (like group in the example above), or another handler, which you'd have to register using {@link ReprUtil#register(RepresentationRestorer, String)}.
 * - In the constructor(Representation repr), call: new ReprUtil(this).deserialize(repr);
 * - In getRepresentation(), call ReprUtil.serialize(this);
 *
 * - The framework can handle Lists, Sets, and Maps as well. For example, @Represented(restorer="[group]") handles a list or set of group elements
 *   and @Represented(restorer="String -> [group]") handles a map of Strings to lists of group elements.
 *   Check {@link Represented#restorer()} for details.
 * - Example with additional information: Suppose your object depends on public parameters (e.g., a publicly known bilinear group bilGroup, consisting of groups G1, G2, GT).
 *   You'd annotate GroupElement members
 *   with @Annotated(restorer="G1"), @Annotated(restorer="G2"), @Annotated(restorer="GT"), depending on which of the three groups each of them belongs to.
 *   Your constructor would take these public parameters pp and a Representation repr as input and then call new ReprUtil(this).register(bilGroup).deserialize(repr);
 *   getRepresentation() would still just call ReprUtil.serialize(this);
 */
public class ReprUtil {
    /**
     * RepresentationRestorer that can help during recreation of the instance.
     */
    protected HashMap<String, RepresentationRestorer> restorers = new HashMap<>();

    /**
     * Object that's subject to represent or be recreated from Representation.
     */
    protected Object instance;

    /**
     * Create ReprUtil for a specific instance.
     */
    public ReprUtil(Object instance) {
        this.instance = instance;
    }

    /**
     * Register a restorer for a Representation (usually, interfaces with a "recreateFoo(Representation)" method should be
     * valid arguments).
     * Annotate the field that should use this with @Represented(restorer = "name")
     *
     * @param restorer a class that knows how to restore values from Representation (e.g., a Group knows how to restore its GroupElements)
     * @param name the name for this restorer (same as in the @Represented annotation)
     * @return this. For chaining.
     */
    public ReprUtil register(RepresentationRestorer restorer, String name) {
        if (restorers.containsKey(name))
            throw new IllegalArgumentException("Already used name "+name);

        if (Stream.of("->", ",", "[", "]", " ").anyMatch(name::contains))
            throw new IllegalArgumentException("Restorer name "+name+" contains reserved chars");

        restorers.put(name, restorer);
        return this;
    }

    public ReprUtil register(BilinearGroup bilinearGroup) {
        register(bilinearGroup.getG1(), "G1");
        register(bilinearGroup.getG2(), "G2");
        register(bilinearGroup.getGT(), "GT");
        return this;
    }

    /**
     * Register a custom restorer function, which takes a Representation and recreates the object.
     * Annotate the field that should use this with @Represented(restorer = "name")
     * You should usually not need this (prefer register(RepresentationRestorer))
     *
     * @param restorer a function taking a Representation and restoring the corresponding object.
     * @param name the name for this restorer function (same as in the @Represented annotation)
     * @return this. For chaining.
     */
    public ReprUtil register(Function<? super Representation, ?> restorer, String name)  {
        return this.register(new CustomRepresentationRestorer(restorer), name);
    }

    /**
     * Iterates over all fields of instance's class and its superclasses, making them accessible (readable, writable) for the fieldConsumer call.
     */
    private void forEachField(Consumer<Field> fieldConsumer) {
        Class<?> clazz = instance.getClass();
        while (!clazz.equals(Object.class)) {
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (hasRepresentedTypeAnnotation(field)) {
                        field.setAccessible(true); //TODO need to reset this to false or it was before?!
                        fieldConsumer.accept(field);
                    }
                }
            } catch (SecurityException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            } finally {
                clazz = clazz.getSuperclass();
            }
        }
    }

    /**
     * Private helper method: given field name, outputs corresponding Field for instance.
     */
    private Field getFieldByName(String name) {
        Class<?> clazz = instance.getClass();
        while (!clazz.equals(Object.class)) {
            try {
                Field field = clazz.getDeclaredField(name);
                return field;
            } catch (SecurityException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                //That's expected. Just use the superclass then
            } finally {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Takes the instance and represents it as a Representation.
     * @return a Representation
     */
    public Representation serialize() {
        ObjectRepresentation result = new ObjectRepresentation();
        forEachField(field -> {
            try {
                field.setAccessible(true);
                result.put(field.getName(), getHandler(field.getGenericType(), getRestorerStringOfField(field)).serializeToRepresentation(field.get(instance)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    /**
     * Takes the instance and represents it as a Representation.
     * @param instance the object to serialize
     * @return a Representation
     */
    public static Representation serialize(Object instance) {
        return new ReprUtil(instance).serialize();
    }

    /**
     * Recreates the instance from representation.
     * @param repr a Representation you got from serialize()
     */
    public void deserialize(Representation repr) {
        forEachField(field -> restoreField(field, repr));
    }

    /**
     * Handles a single field of instance.
     * @param field what field of instance to handle
     * @param topLevelRepr the representation for instance
     * @return the value assigned to the field.
     */
    Object restoreField(Field field, Representation topLevelRepr) {
        try {
            System.out.println(field);
            System.out.println("1. Is accessible: " + field.isAccessible());
            field.setAccessible(true);
            Object value = field.get(instance);
            if (value != null) {
                System.out.println("Not null value.");
                return value;
            }
            System.out.println("2. Is accessible: " + field.isAccessible());
            value = getHandlerForField(field).deserializeFromRepresentation(topLevelRepr.obj().get(field.getName()), name -> getOrRecreateRestorer(name, topLevelRepr));
            System.out.println("3. Is accessible: " + field.isAccessible());
            field.set(instance, value);
            return value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets restorer from registered restorers or - if it's not there - look for it within the fields instance's class
     * (and restore it if annotated).
     */
    RepresentationRestorer getOrRecreateRestorer(String name, Representation topLevelRepr) {
        if (restorers.containsKey(name))
            return restorers.get(name);

        //Restorer is some field
        Field field = getFieldByName(name);
        if (field == null)
            throw new IllegalArgumentException("\""+name+"\" is neither the name of a restorer given through ReprUtil.register, nor is it a member of the class being recreated.");

        Object result = restoreField(field, topLevelRepr);

        if (result == null)
            throw new NullPointerException("The member \""+name+"\" is null and hence cannot be used to recreate further objects from representation");

        if (!(result instanceof RepresentationRestorer))
            throw new IllegalArgumentException("\""+name+"\" is the name of a member of your class, but its value does not seem to implement the RepresentationRestorer interface.");

        return (RepresentationRestorer) result;
    }

    /**
     * Helper method. Checks whether field is annotated.
     */
    private static boolean hasRepresentedTypeAnnotation(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        if (annotations == null || annotations.length == 0) {
            return false;
        }
        return Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .anyMatch(a -> a.getSimpleName().startsWith("Represented"));
    }

    /**
     * Helper method.
     * If field is annotated with @Represented annotation, returns the annotation's "restorer" value.
     */
    private static String getRestorerStringOfField(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        if (annotations == null || annotations.length == 0)
            return null;

        for (Annotation annotation : annotations)
            if (annotation.annotationType().equals(Represented.class))
                return ((Represented) annotation).restorer();

        return null;
    }

    protected static RepresentationHandler getHandlerForField(Field field) {
        return getHandler(field.getGenericType(), getRestorerStringOfField(field));
    }

    /**
     * Derives the ReputationHandler for a certain Type (and restorerString).
     * This is done statically, i.e. with static type information.
     */
    protected static RepresentationHandler getHandler(Type type, String restorerString) {
        if (restorerString == null || restorerString.trim().length() == 0) //the distinction is basically made because each method recursively calls itself and we want consistent behavior depending on whether or not a restorer String was originally given (as opposed to a given String like "[] -> ", which eventually is broken down into an empty String but should be handled as a syntax error.)
            return getHandlerWithoutRestorerString(type);
        else
            return getHandlerWithRestorerString(type, restorerString);
    }

    /**
     * Derives the ReputationHandler for a certain Type (and restorerString).
     * This is done statically, i.e. with static type information.
     */
    protected static RepresentationHandler getHandlerWithRestorerString(Type type, String restorerString) {
        if (StandaloneRepresentationHandler.canHandle(type))
            return new StandaloneRepresentationHandler((Class) type);

        //Get rid of spaces and enclosing parentheses (if restorerString = "(FOO)")
        String trimmedString = stripEnclosingParentheses(restorerString);

        if (DependentRepresentationHandler.canHandle(type))
            return new DependentRepresentationHandler(trimmedString, type);

        if (ListAndSetRepresentationHandler.canHandle(type) && trimmedString.startsWith("[") && trimmedString.endsWith("]")) {
            Type elementType = ListAndSetRepresentationHandler.getElementType(type);
            return new ListAndSetRepresentationHandler(getHandlerWithRestorerString(elementType, trimmedString.substring(1, trimmedString.length()-1)), type);
        }

        if (ArrayRepresentationHandler.canHandle(type) && trimmedString.startsWith("[") && trimmedString.endsWith("]")) {
            Type elementType = ArrayRepresentationHandler.getTypeOfElements(type);
            return new ArrayRepresentationHandler(getHandlerWithRestorerString(elementType, trimmedString.substring(1, trimmedString.length()-1)), type);
        }

        int mapArrowIndex = findMapArrow(trimmedString);
        if (MapRepresentationHandler.canHandle(type) && mapArrowIndex != -1) {
            Type keyType = MapRepresentationHandler.getKeyType(type);
            Type valueType = MapRepresentationHandler.getValueType(type);
            return new MapRepresentationHandler(getHandlerWithRestorerString(keyType, trimmedString.substring(0, mapArrowIndex)),
                    getHandlerWithRestorerString(valueType, trimmedString.substring(mapArrowIndex+2)),
                    type);
        }

        throw new IllegalArgumentException("Don't know how to handle type "+type.getTypeName()+" using restorer String \""+restorerString+"\"");
    }



    /**
     * Derives the ReputationHandler for a certain Type (for empty restorer string).
     * This is done statically, i.e. with static type information.
     */
    protected static RepresentationHandler getHandlerWithoutRestorerString(Type type) {
        if (StandaloneRepresentationHandler.canHandle(type))
            return new StandaloneRepresentationHandler((Class) type);

        if (DependentRepresentationHandler.canHandle(type))
            return new DependentRepresentationHandler("", type);

        if (ListAndSetRepresentationHandler.canHandle(type)) {
            Type elementType = ListAndSetRepresentationHandler.getElementType(type);
            return new ListAndSetRepresentationHandler(getHandlerWithoutRestorerString(elementType), type);
        }

        if (ArrayRepresentationHandler.canHandle(type)) {
            Type elementType = ArrayRepresentationHandler.getTypeOfElements(type);
            return new ArrayRepresentationHandler(getHandlerWithoutRestorerString(elementType), type);
        }

        if (MapRepresentationHandler.canHandle(type)) {
            Type keyType = MapRepresentationHandler.getKeyType(type);
            Type valueType = MapRepresentationHandler.getValueType(type);
            return new MapRepresentationHandler(getHandlerWithoutRestorerString(keyType), getHandlerWithoutRestorerString(valueType), type);
        }

        throw new IllegalArgumentException("Don't know how to handle type "+type.getTypeName()+" using empty restorer String (you can add one within the @Represented annotation)");
    }

    /**
     * Helper method.
     * Returns the index of the top-level "->" within the given String (the index of '-' char, to be precise).
     * If none, returns -1.
     */
    private static int findMapArrow(String str) {
        int depth = 0; //number of "[" or "(" read minus number of "]" or ")" read.
        for (int i=0;i<str.length();i++) {
            switch (str.charAt(i)) {
                case '[':
                case '(':
                    depth++;
                    break;
                case ']':
                case ')':
                    depth--;
                    break;
                case '-':
                    if (depth == 0 && str.length() > i+1 && str.charAt(i+1) == '>') //detected map that's not nested in []
                        return i;
                    break;
            }
        }
        return -1;
    }

    /**
     * If the expression is of the form "( ... )" (where the two parentheses are matching), removes those parentheses.
     * For example, "(x)" would become "x", but "(x) -> (y)" would not be modified.
     */
    private static String stripEnclosingParentheses(String str) {
        str = str.trim();
        if (!str.startsWith("(") || !str.endsWith(")"))
            return str;

        int depth = 1; //number of  "(" read minus number of ")" read.
        for (int i=1;i<str.length()-1;i++) { //loop indices exclude first and last parentheses.
            switch (str.charAt(i)) {
                case '(':
                    depth++;
                    break;
                case ')':
                    depth--;
                    break;
            }
            if (depth == 0) //found parenthesis matching the str[0] one (which is not the str[len-1] one)
                return str;
        }

        return stripEnclosingParentheses(str.substring(1, str.length()-1)); //recursively call to remove more parentheses if necessary
    }
}
