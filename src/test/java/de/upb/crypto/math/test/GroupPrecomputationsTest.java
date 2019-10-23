package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.raphael.GroupPrecomputationsFactory;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupPrecomputationsTest {

    @Test
    public void testAddRetrieve() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        // Test for additive subgroup of Zp
        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);


        addPrecomputations.addPower(
                addZp.getNeutralElement(),
                BigInteger.valueOf(2),
                addZp.getNeutralElement().op(addZp.getNeutralElement())
        );

        assertEquals(
                addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(2)),
                addZp.getNeutralElement().op(addZp.getNeutralElement())
        );

        // Test for multiplicative subgroup of Zp
        RingUnitGroup mulZp = zp.asUnitGroup();

        GroupPrecomputationsFactory.GroupPrecomputations mulPrecomputations =
                GroupPrecomputationsFactory.get(mulZp);

        GroupElement elem = mulZp.getUniformlyRandomNonNeutral();
        mulPrecomputations.addPower(
                elem,
                BigInteger.valueOf(10),
                elem.pow(10)
        );

        assertEquals(
                mulPrecomputations.getPower(elem, BigInteger.valueOf(10)),
                elem.pow(10)
        );
    }

    @Test
    public void testRetrieveNoAddPrecomputeIfMissing() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        // Test for additive subgroup of Zp
        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);

        assertEquals(
                addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(2)),
                addZp.getNeutralElement().op(addZp.getNeutralElement())
        );

        // Test for multiplicative subgroup of Zp
        RingUnitGroup mulZp = zp.asUnitGroup();

        GroupPrecomputationsFactory.GroupPrecomputations mulPrecomputations =
                GroupPrecomputationsFactory.get(mulZp);

        GroupElement elem = mulZp.getUniformlyRandomNonNeutral();

        assertEquals(
                mulPrecomputations.getPower(elem, BigInteger.valueOf(10)),
                elem.pow(10)
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testRetrieveNoAddNoPrecomputeIfMissing() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        // Test for additive subgroup of Zp
        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);

        addPrecomputations.getPower(
                addZp.getNeutralElement(), BigInteger.valueOf(2), false
        );
    }

    @Test
    public void testRepresentation() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        // Test for additive subgroup of Zp
        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);

        Representation repr = addPrecomputations.getRepresentation();

        GroupPrecomputationsFactory.addFromRepresentation(repr);
    }
}
