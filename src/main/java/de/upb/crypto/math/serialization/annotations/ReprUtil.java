package de.upb.crypto.math.serialization.annotations;

import de.upb.crypto.math.structures.groups.elliptic.BilinearGroup;
import de.upb.crypto.math.structures.groups.elliptic.BilinearMap;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.internal.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A class for almost automatic creation of and restoration from representations.
 * <p>
 * Typical usage example:
 * <ol>
 * <li> Your class is StandaloneRepresentable, i.e. you need to give a constructor with {@link Representation}
 *      argument and a {@code getRepresentation()} method.
 * <li> In your class, annotate all fields that need to be part of the representation with {@code @Represented}.
 *      For example, {@code @Represented private Group group}.
 * <li> If some objects need help being deserialized (e.g., {@code GroupElement} instances are deserialized
 *      by their group), define a restorer in the {@code @Represented} annotation.
 *      For example, {@code @Represented(restorer="group") private GroupElement x;}.
 *      The restorer can either be another field of your class (like {@code group} in the example above),
 *      or another handler, which you'd have to register using
 *      {@link ReprUtil#register(RepresentationRestorer, String)}.
 * <li> In the constructor with argument {@code }Representation repr},
 *      call {@code new ReprUtil(this).deserialize(repr);}.
 * <li> In {@code getRepresentation()}, call {@code ReprUtil.serialize(this);}
 *</ol>
 * You can also refer to a restorer as, for example, {@code "bilGroup::getG1"}, where {@code bilGroup} is as above
 * (another field of the class or something registered) and {@code getG1} is a method that returns a
 * {@code RepresentationRestorer}.
 * <p>
 * The framework can handle lists, sets, and maps as well.
 * For example, {@code @Represented(restorer="[group]")} handles a list or set of group elements
 * and {@code @Represented(restorer="String -> [group]")} handles a map of strings to lists of group elements.
 * Check {@link Represented#restorer()} for details.
 * <p>
 * An example including a bilinear pairing:
 * <p>
 * Suppose your object depends on public parameters (e.g., a publicly known bilinear group bilGroup,
 * consisting of groups G1, G2, GT).
 * You'd annotate GroupElement members with {@code @Represented(restorer="G1")}, {@code @Represented(restorer="G2")},
 * {@code @Represented(restorer="GT")}, depending on which of the three groups each of them belongs to.
 * Your constructor would take these public parameters {@code pp} and a {@code Representation repr} as input
 * and then call {@code new ReprUtil(this).register(bilGroup).deserialize(repr);}.
 * {@code getRepresentation()} would still just call {@code ReprUtil.serialize(this);}.
 * <p>
 * For more information, consult the <a href="https://upbcuk.github.io/docs/representations.html">documentation</a>.
 */
public class ReprUtil {
    static Pattern methodCallSeparator = Pattern.compile("::");
    /**
     * Maps representation restorer identifiers to the corresponding {@code RepresentationRestorer} instances.
     * <p>
     * Used to help with deserialization of fields that need a representation restorer to be deserialized.
     */
    protected HashMap<String, RepresentationRestorer> restorers = new HashMap<>();

    /**
     * Either stores the recreated object during deserialization, or the object to serialize.
     */
    protected Object instance;

    /**
     * Create {@code ReprUtil} for a specific target instance.
     * <p>
     * For deserialization, the target instance will be filled with field values from the deserialized representation.
     * For serialization, the target instance will be used to create the representation.
     */
    public ReprUtil(Object instance) {
        this.instance = instance;
    }

    /**
     * Register a representation restorer using the given name.
     * <p>
     * Annotate the fields that should use this restorer with {@code @Represented(restorer = "name")}.
     *
     * @param restorer a class that knows how to restore values from Representation
     *                 (e.g., a Group knows how to restore its GroupElements)
     * @param name the name for this restorer (same as in the {@code @Represented} annotation)
     * @return this (for chaining)
     */
    public ReprUtil register(RepresentationRestorer restorer, String name) {
        if (restorers.containsKey(name))
            throw new IllegalArgumentException("Already used name "+name);

        if (Stream.of("->", ",", "[", "]", " ").anyMatch(name::contains))
            throw new IllegalArgumentException("Restorer name "+name+" contains reserved chars");

        restorers.put(name, restorer);
        return this;
    }

    /**
     * Registers \(\mathbb{G}_1\), \(\mathbb{G}_2\) and \(\mathbb{G}_T\) from the given bilinear group
     * as restorers with names {@code "G1"}, {@code "G2"} and {@code "GT"}, respectively.
     *
     * @param bilinearGroup the bilinear group to register restorers for
     * @return this (for chaining)
     */
    public ReprUtil register(BilinearGroup bilinearGroup) {
        register(bilinearGroup.getBilinearMap());
        return this;
    }

    /**
     * Registers \(\mathbb{G}_1\), \(\mathbb{G}_2\) and \(\mathbb{G}_T\) from the given bilinear map
     * as restorers with names {@code "G1"}, {@code "G2"} and {@code "GT"}, respectively.
     *
     * @param bilinearMap the bilinear map to register restorers for
     * @return this (for chaining)
     */
    public ReprUtil register(BilinearMap bilinearMap) {
        register(bilinearMap.getG1(), "G1");
        register(bilinearMap.getG2(), "G2");
        register(bilinearMap.getGT(), "GT");
        return this;
    }

    /**
     * Register a custom restorer function, which takes a {@code Representation} and recreates the object.
     * <p>
     * Annotate the field that should use this with {@code @Represented(restorer = "name")}.
     * <p>
     * You should usually not need this.
     * Instead, use {@link #register(RepresentationRestorer, String)} to register a {@code RepresentationRestorer}.
     *
     * @param restorer a function that takes in a {@code Representation} and restores the corresponding object.
     * @param name the name for this restorer function (same as in the {@code @Represented} annotation)
     * @return this (for chaining)
     */
    public ReprUtil register(Function<? super Representation, ?> restorer, String name)  {
        return this.register(new CustomRepresentationRestorer(restorer), name);
    }

    /**
     * Runs the given {@link Consumer<Field>} on each field of the target instance's class and superclasses.
     * <p>
     * To enable this, each field is made accessible (readable, writeable) via {@code setAccessible}.
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
     * Retrieves corresponding field of the target instance's class given its name.
     *
     * @param name name of the field
     * @return corresponding field
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
     * Represents the the instance given when initializing this {@code ReprUtil} as a {@code Representation}.
     *
     * @return the representation
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
     * Represents the given instance as a {@code Representation}.
     *
     * @param instance the object to serialize
     * @return a Representation
     */
    public static Representation serialize(Object instance) {
        return new ReprUtil(instance).serialize();
    }

    /**
     * Restores the instance from the given representation, storing the result in the given object.
     * <p>
     * For more control over the deserialization process, e.g. if using custom representation restorers,
     * use {@code new ReprUtil(this).register(...).deserialize(repr)} instead.
     *
     * @param instance the object whose attribute values to restore
     * @param repr the representation from which to restore the object
     */
    public static void deserialize(Object instance, Representation repr) {
        new ReprUtil(instance).deserialize(repr);
    }

    /**
     * Deserializes the given representation, storing the result in the instance given when
     * initializing this {@code ReprUtil}.
     *
     * @param repr the representation to deserialize
     */
    public void deserialize(Representation repr) {
        forEachField(field -> restoreField(field, repr));
    }

    /**
     * Restores the value of the given field using the given representation.
     * <p>
     * Does not overwrite field values that are already set, i.e. not equal null.
     *
     * @param field determines the field of instance to restore
     * @param topLevelRepr the representation to restore the field from
     * @return the value assigned to the field
     */
    Object restoreField(Field field, Representation topLevelRepr) {
        try {
            // Make sure we can access the field
            field.setAccessible(true);
            Object value = field.get(instance);
            // If the field already has a value, do not overwrite it
            if (value != null) {
                return value;
            }
            // Retrieve the correct handler for the given field and restore the value from the representation entry
            value = getHandlerForField(field).deserializeFromRepresentation(topLevelRepr.obj().get(field.getName()), name -> getOrRecreateRestorer(name, topLevelRepr));
            field.set(instance, value);
            return value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the restorer based on the given restorer name.
     * <p>
     * First tries to retrieve the restorer from the map of registered restorers.
     * If it is not there, it will check the representation itself.
     * <p>
     * For example, a {@code Group} acts as the restorer for a {@code GroupElement}.
     * It can either be register as a restorer - in which case it will be in the list of registered restorers -
     * or stored in the instance itself (and annotated with {@code @Represented},
     * in which case it will be contained in the representation itself.
     *
     * @param restorerString restorer string to retrieve restorer for
     * @param topLevelRepr representation to get restorer from if not in the list of registered restorers
     */
    RepresentationRestorer getOrRecreateRestorer(String restorerString, Representation topLevelRepr) {
        //Parse restorerString of form "baseName::methodToCall::methodToCall::..."
        String[] parsed = methodCallSeparator.split(restorerString);
        String baseName = parsed[0];

        //Look for base name
        if (restorers.containsKey(baseName))
            return restorers.get(baseName);

        //Base is some field
        Field field = getFieldByName(baseName);
        if (field == null)
            throw new IllegalArgumentException("\""+baseName+"\" is neither the name of a restorer given through ReprUtil.register, nor is it a member of the class being recreated.");

        Object restoredBase = restoreField(field, topLevelRepr);

        if (restoredBase == null)
            throw new NullPointerException("The member \""+baseName+"\" is null and hence cannot be used to recreate further objects from representation");

        return callMethods(restoredBase, parsed);
    }

    /**
     * Retrieves a representation restorer only obtainable via sequentially calling methods on a given object.
     * <p>
     * For example, a restorer string such as {@code bilGroup::getG1} allows to specify the return value of
     * {@code bilGroup.getG1()} (a {@code Group}) as a restorer.
     * In that case, the {@code baseObject} would be the {@code BilinearGroup} instance, and the
     * {@code parsedRestorerString} would be given by {@code ["getG1"]}.
     *
     * @param baseObject the object on which to execute the methods
     * @param parsedRestorerString the list of methods to call in order of execution
     * @return the retrieved representation restorer
     */
    private static RepresentationRestorer callMethods(Object baseObject, String[] parsedRestorerString) {
        Object currentObject = baseObject;
        for (int i=1;i<parsedRestorerString.length;i++) {
            String methodToCall = parsedRestorerString[i];
            try {
                currentObject = currentObject.getClass().getMethod(methodToCall).invoke(currentObject);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Cannot call desired method "+methodToCall+" on "+currentObject.getClass().getName(), e);
            }
        }

        if (!(currentObject instanceof RepresentationRestorer))
            throw new IllegalArgumentException("\""+baseObject.getClass().getName()+"\" is not a RepresentationRestorer.");

        return (RepresentationRestorer) currentObject;
    }

    /**
     * Checks whether given field is annotated with {@code @Represented}.
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
     * If field is annotated with {@code @Represented} annotation, returns the annotation's restorer string.
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

    /**
     * Retrieves the representation handler for the given field.
     */
    protected static RepresentationHandler getHandlerForField(Field field) {
        return getHandler(field.getGenericType(), getRestorerStringOfField(field));
    }

    /**
     * Derives the representation handler for a certain type and restorer string.
     * This is done statically, i.e. with static type information.
     */
    protected static RepresentationHandler getHandler(Type type, String restorerString) {
        if (restorerString == null || restorerString.trim().length() == 0)
            // the distinction is basically made because each method recursively calls itself
            // and we want consistent behavior depending on whether or not a restorer String was originally given
            // (as opposed to a given String like "[] -> ", which eventually is broken down into an empty String
            // but should be handled as a syntax error.)
            return getHandlerWithoutRestorerString(type);
        else
            return getHandlerWithRestorerString(type, restorerString);
    }

    /**
     * Derives the representation handler for a certain type and non-empty restorer string.
     * This is done statically, i.e. with static type information.
     */
    protected static RepresentationHandler getHandlerWithRestorerString(Type type, String restorerString) {
        if (StandaloneRepresentationHandler.canHandle(type))
            return new StandaloneRepresentationHandler((Class) type);

        //Get rid of spaces and enclosing parentheses (if restorerString = "(FOO)")
        String trimmedString = stripEnclosingParentheses(restorerString);

        if (DependentRepresentationHandler.canHandle(type))
            return new DependentRepresentationHandler(trimmedString, type);

        if (ListAndSetRepresentationHandler.canHandle(type) && trimmedString.startsWith("[")
                && trimmedString.endsWith("]")) {
            Type elementType = ListAndSetRepresentationHandler.getElementType(type);
            return new ListAndSetRepresentationHandler(
                    getHandlerWithRestorerString(elementType, trimmedString.substring(1, trimmedString.length()-1)),
                    type
            );
        }

        if (ArrayRepresentationHandler.canHandle(type) && trimmedString.startsWith("[")
                && trimmedString.endsWith("]")) {
            Type elementType = ArrayRepresentationHandler.getTypeOfElements(type);
            return new ArrayRepresentationHandler(
                    getHandlerWithRestorerString(elementType, trimmedString.substring(1, trimmedString.length()-1)),
                    type
            );
        }

        int mapArrowIndex = findMapArrow(trimmedString);
        if (MapRepresentationHandler.canHandle(type) && mapArrowIndex != -1) {
            Type keyType = MapRepresentationHandler.getKeyType(type);
            Type valueType = MapRepresentationHandler.getValueType(type);
            return new MapRepresentationHandler(
                    getHandlerWithRestorerString(keyType, trimmedString.substring(0, mapArrowIndex)),
                    getHandlerWithRestorerString(valueType, trimmedString.substring(mapArrowIndex+2)),
                    type
            );
        }

        throw new IllegalArgumentException("Don't know how to handle type " + type.getTypeName()
                + " using restorer String \"" + restorerString + "\"");
    }



    /**
     * Derives the representation handler for a certain type and empty restorer string.
     * This is done statically, i.e. with static type information.
     */
    protected static RepresentationHandler getHandlerWithoutRestorerString(Type type) {
        // For generic type we need to extract the raw type. Only for StandaloneRepresentable though, as stuff
        // like list and map handling can handle generic types by themselves.
        // TODO: What about DependentRepresentations?
        Type rawType = type;
        if (type instanceof ParameterizedType) {
            rawType = ((ParameterizedType) type).getRawType();
        }
        if (StandaloneRepresentationHandler.canHandle(rawType))
            return new StandaloneRepresentationHandler((Class) rawType);

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

        throw new IllegalArgumentException("Don't know how to handle type " + type.getTypeName()
                + " using empty restorer String (you can add one within the @Represented annotation)");
    }

    /**
     * Returns the index of the top-level "->" within the given String (the index of the '-' char, to be precise).
     * If none exist, returns -1.
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
