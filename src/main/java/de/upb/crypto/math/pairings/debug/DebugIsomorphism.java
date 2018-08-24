package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.util.Objects;

public class DebugIsomorphism implements GroupHomomorphism {
    @Represented
    private DebugGroup src;
    @Represented
    private DebugGroup target;

    public DebugIsomorphism(DebugGroup src, DebugGroup target) {
        this.src = src;
        this.target = target;
    }

    public DebugIsomorphism(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
    }

    @Override
    public GroupElement apply(GroupElement groupElement) {
        if (!groupElement.getStructure().equals(src))
            throw new IllegalArgumentException("Tried to apply isomorphism on wrong group (argument was from " + groupElement.getStructure() + ")");
        DebugGroupLogger.log("G2", "hom");
        return new DebugGroupElement(target, ((DebugGroupElement) groupElement).elem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugIsomorphism that = (DebugIsomorphism) o;
        return Objects.equals(src, that.src) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, target);
    }
}
