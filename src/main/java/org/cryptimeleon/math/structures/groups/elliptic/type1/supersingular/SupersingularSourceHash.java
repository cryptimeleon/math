package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;

public class SupersingularSourceHash implements HashIntoGroupImpl {
    private SupersingularSourceGroupImpl codomain;

    public SupersingularSourceHash(SupersingularSourceGroupImpl codomain) {
        this.codomain = codomain;
    }

    public SupersingularSourceHash(Representation r) {
        this((SupersingularSourceGroupImpl) r.obj().get("codomain").repr().recreateRepresentable());
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation or = new ObjectRepresentation();
        or.put("codomain", new RepresentableRepresentation(codomain));

        return or;
    }

    @Override
    public GroupElementImpl hashIntoGroupImpl(byte[] x) {
        HashIntoZn hash = new HashIntoZn(this.codomain.getFieldOfDefinition().size());
        ExtensionFieldElement z = ((ExtensionField) this.codomain.getFieldOfDefinition()).createElement(
                hash.hash(x).getInteger()
        );
        return this.codomain.mapToPoint(z);
    }

    @Override
    public int hashCode() {
        return codomain.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SupersingularSourceHash && codomain.equals(((SupersingularSourceHash) obj).codomain);
    }
}
