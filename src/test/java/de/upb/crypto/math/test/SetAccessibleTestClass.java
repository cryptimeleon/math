package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

public class SetAccessibleTestClass implements Representable {

    @Represented(restorer = "groupG1")
    private GroupElement g;

    @Represented
    private Group groupG1;

    public SetAccessibleTestClass(Group groupG1, GroupElement g) {
        this.groupG1 = groupG1;
        this.g = g;
    }

    public SetAccessibleTestClass(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
