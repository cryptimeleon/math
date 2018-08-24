package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.HashIntoZn;

public class SupersingularSourceHash implements HashIntoStructure {
    private SupersingularSourceGroup codomain;

    public SupersingularSourceHash(SupersingularSourceGroup codomain) {
        this.codomain = codomain;
    }

    public SupersingularSourceHash(Representation r) {
        this((SupersingularSourceGroup) r.obj().get("codomain").repr().recreateRepresentable());
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation or = new ObjectRepresentation();
        or.put("codomain", new RepresentableRepresentation(codomain));

        return or;
    }

    @Override
    public Element hashIntoStructure(byte[] x) {
        HashIntoZn hash = new HashIntoZn(this.codomain.getFieldOfDefinition().size());
        ExtensionFieldElement z = ((ExtensionField) this.codomain.getFieldOfDefinition()).createElement(
                hash.hashIntoStructure(x).getInteger()
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
