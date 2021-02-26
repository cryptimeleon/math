package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class StructureTests {
    @Test
    public void testStructureRepresentation() {
        Structure s = getStructureToTest();

        RepresentableRepresentation repr = new RepresentableRepresentation(s);
        Structure s2 = (Structure) repr.recreateRepresentable();
        assertEquals("Reserialized structure should be equal to original", s, s2);
        assertEquals("Reserialized structure's hashCode should be equal to original", s.hashCode(), s2.hashCode());
    }

    @Test
    public void testElementRepresentation() {
        Element elem = getElementToTest();
        Structure s = getStructureToTest();

        Representation repr = elem.getRepresentation();
        Element elem2 = s.restoreElement(repr);

        assertEquals("Reserialized element should be equal to original", elem, elem2);
        assertEquals("Reserialized element's hashCode should be equal to original", elem.hashCode(), elem2.hashCode());
    }

    public abstract Structure getStructureToTest();

    public abstract Element getElementToTest();
}
