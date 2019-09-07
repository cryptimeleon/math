package de.upb.crypto.math.serialization;

import de.upb.crypto.math.serialization.converter.JSONConverter;

import java.lang.reflect.InvocationTargetException;

/**
 * A Representation that saves a (getRepresentedTypeName(), getRepresentation()) tuple.
 * This is useful for storing a StandaloneRepresentable, as it can later be restored by simply calling
 * recreateRepresentable()
 */
public class RepresentableRepresentation extends Representation {
    private static final long serialVersionUID = 8718774055302751544L;
    protected final String representedTypeName;
    protected final Representation representation;

    public RepresentableRepresentation(Representable r) {
        representedTypeName = r.getClass().getName();
        representation = r.getRepresentation();
    }

    public RepresentableRepresentation(Enum enumValue) {
        representedTypeName = enumValue.getDeclaringClass().getName();
        representation = new StringRepresentation(enumValue.name());
    }

    public RepresentableRepresentation(String representedTypeName, Representation representation) {
        this.representedTypeName = representedTypeName;
        this.representation = representation;
    }

    public String getRepresentedTypeName() {
        return representedTypeName;
    }

    public Representation getRepresentation() {
        return representation;
    }

    /**
     * Uses the standard mechanism RepresentationToJavaObjectHelper
     * to try to recreate the Representable that is represented here.
     */
    public Object recreateRepresentable() {
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
                    throw new IllegalArgumentException("Cannot find class " + representedTypeName, e);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Don't know how to handle Representable type '" + representedTypeName + "'", e);
                }
        }
    }

    @Override
    public int hashCode() { //eclipse-generated
        final int prime = 31;
        int result = 1;
        result = prime * result + ((representation == null) ? 0 : representation.hashCode());
        result = prime * result + ((representedTypeName == null) ? 0 : representedTypeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) { //eclipse-generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RepresentableRepresentation other = (RepresentableRepresentation) obj;
        if (representation == null) {
            if (other.representation != null)
                return false;
        } else if (!representation.equals(other.representation))
            return false;
        if (representedTypeName == null) {
            return other.representedTypeName == null;
        } else return representedTypeName.equals(other.representedTypeName);
    }

    @Override
    public String toString() {
        return new JSONConverter().serialize(this);
    }
}
