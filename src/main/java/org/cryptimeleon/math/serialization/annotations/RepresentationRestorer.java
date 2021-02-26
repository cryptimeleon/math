package org.cryptimeleon.math.serialization.annotations;

import org.cryptimeleon.math.serialization.Representation;

import java.lang.reflect.Type;

/**
 * This class handles restoration from representation for classes which cannot do this themselves due to lack of
 * information.
 * <p>
 * For example, a {@code Group} handles restoration of its group elements.
 * This way the group does not have to explicitly be stored in each group element instance.
 */
public interface RepresentationRestorer {
    /**
     * Takes a representation and creates an object of the given type from it if the type is supported by the restorer.
     *
     * @param type tells the restorer which type the restored object should have
     * @param repr the representation to restore the object from
     * @return the restored object
     * @throws IllegalArgumentException if the restorer is unable to handle the given type
     */
    Object recreateFromRepresentation(Type type, Representation repr) throws IllegalArgumentException;
}
