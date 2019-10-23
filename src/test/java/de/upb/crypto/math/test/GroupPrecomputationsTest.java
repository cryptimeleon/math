package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.performance.group.GroupPerformanceTest;
import de.upb.crypto.math.raphael.GroupPrecomputationsFactory;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupPrecomputationsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
    public void testDeserializedSame() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);

        for (int i = 0; i < 8; ++i) {
            addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(i));
        }

        Representation repr = addPrecomputations.getRepresentation();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, addZp);

        // first check that deserialized precomputations contains same elements as before
        for (int i = 0; i < 8; ++i) {
            assertEquals(
                    addPrecomputations2.getPower(
                            addZp.getNeutralElement(), BigInteger.valueOf(i),false
                    ),
                    addPrecomputations.getPower(
                            addZp.getNeutralElement(), BigInteger.valueOf(i),false
                    )
            );
        }
    }

    @Test
    public void testDeserializeDifferentObjects() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);

        for (int i = 0; i < 8; ++i) {
            addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(i));
        }

        Representation repr = addPrecomputations.getRepresentation();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, addZp);

        // now add new element
        addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(9));

        // only first one should have new element
        exception.expect(IllegalStateException.class);
        addPrecomputations2.getPower(
                addZp.getNeutralElement(), BigInteger.valueOf(9), false
        );
    }

    @Test
    public void testAddDeserializedGroupPrecomputationsToFactory() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        RingAdditiveGroup addZp = zp.asAdditiveGroup();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations =
                GroupPrecomputationsFactory.get(addZp);

        for (int i = 0; i < 8; ++i) {
            addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(i));
        }

        Representation repr = addPrecomputations.getRepresentation();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, addZp);

        // now add new element and insert into store
        addPrecomputations2.getPower(addZp.getNeutralElement(), BigInteger.valueOf(9));

        GroupPrecomputationsFactory.addGroupPrecomputations(addPrecomputations2);

        // new element should now be added to permutations we obtained from store
        addPrecomputations.getPower(
                addZp.getNeutralElement(), BigInteger.valueOf(9), false
        );
    }

}
