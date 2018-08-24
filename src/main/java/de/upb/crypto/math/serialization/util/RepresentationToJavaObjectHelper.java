package de.upb.crypto.math.serialization.util;

import de.upb.crypto.math.serialization.Representation;

import java.lang.reflect.InvocationTargetException;

public class RepresentationToJavaObjectHelper {
    private static RepresentationToJavaObjectHelper instance = null;

    private RepresentationToJavaObjectHelper() {
    }

    public static RepresentationToJavaObjectHelper getInstance() {
        if (instance == null) {
            synchronized (RepresentationToJavaObjectHelper.class) {
                if (instance == null)
                    instance = new RepresentationToJavaObjectHelper();
            }
        }

        return instance;
    }

    /**
     * Tries to create the Representable object from its representation.
     */
    public Object getObject(String representedTypeName, Representation representation) {
        switch (representedTypeName) {
            default: //default case: try some reflection magic (i.e. try to interpret representedTypeName as fully qualified class name, call constructor with Representation argument)
                try {
                    Class<?> c = Class.forName(representedTypeName);
                    try {
                        if (c.isEnum()) {
                            return Enum.valueOf((Class<? extends Enum>) c, representation.str().get());
                        }
                        return c.getConstructor(Representation.class).newInstance(representation);
                    } catch (NoSuchMethodException e) { //no constructor with single Representation paramenter
                        if (representation == null) //no representation necessary. Try default constructor
                            return c.getConstructor(new Class<?>[]{}).newInstance();
                        else
                            throw e;
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Cannot find class " + representedTypeName + ". This may be because it is not in the upb_pbc plugin. In that case, add \"Eclipse-RegisterBuddy: de.upb.crypto.pbc\" to your manifest.", e);
                    //Eclipse-RegisterBuddy: de.upb.crypto.pbc (together with Eclipse-BuddyPolicy: registered in this plugin)
                    //allows the upb_pbc project to ask the class loader of the requiring project for the class.
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Don't know how to handle Representable type '" + representedTypeName + "'", e);
                }
        }
    }
}
