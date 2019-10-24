package de.upb.crypto.math.raphael;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class GroupPrecomputationsTest {

    private static final Zp zp = new Zp(BigInteger.valueOf(101));

    private static final RingAdditiveGroup addZp = zp.asAdditiveGroup();
    private static final RingUnitGroup mulZp = zp.asUnitGroup();

    private static GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations;
    private static GroupPrecomputationsFactory.GroupPrecomputations mulPrecomputations;

    @Before
    public void setup() {
        addPrecomputations = GroupPrecomputationsFactory.get(addZp);
        mulPrecomputations = GroupPrecomputationsFactory.get(mulZp);
    }

    @After
    public void teardown() {
        addPrecomputations.reset();
        mulPrecomputations.reset();
    }

    @Test
    public void testAddRetrieve() {
        // Test for additive subgroup of Zp
        addPrecomputations.addPower(
                addZp.getNeutralElement(),
                BigInteger.valueOf(2),
                zp.createZnElement(BigInteger.valueOf(2)).toAdditiveGroupElement()
        );

        assertEquals(
                addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(2)),
                zp.createZnElement(BigInteger.valueOf(2)).toAdditiveGroupElement()
        );

        // Test for multiplicative subgroup of Zp
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
    public void testRetrieveNoAddComputeIfMissing() {
        // Test for additive subgroup of Zp
        assertEquals(
                addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(2)),
                zp.createZnElement(BigInteger.valueOf(0)).toAdditiveGroupElement()
        );

        // Test for multiplicative subgroup of Zp
        GroupElement elem = mulZp.getUniformlyRandomNonNeutral();

        assertEquals(
                mulPrecomputations.getPower(elem, BigInteger.valueOf(10)),
                elem.pow(10)
        );
    }

    public void testRetrieveNoAddNoComputeIfMissing() {
        assertNull(addPrecomputations.getPower(
                addZp.getNeutralElement(), BigInteger.valueOf(2), false
        ));
    }

    @Test
    public void testDeserializedSame() {
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
        for (int i = 0; i < 8; ++i) {
            addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(i));
        }

        Representation repr = addPrecomputations.getRepresentation();

        GroupPrecomputationsFactory.GroupPrecomputations addPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, addZp);

        // now add new element
        addPrecomputations.getPower(addZp.getNeutralElement(), BigInteger.valueOf(9));

        // only first one should have new element
        assertNull(addPrecomputations2.getPower(
                addZp.getNeutralElement(), BigInteger.valueOf(9), false
        ));
    }

    @Test
    public void testAddDeserializedGroupPrecomputationsToFactory() {
        for (int i = 0; i < 8; ++i) {
            addPrecomputations.getPower(addZp.getUniformlyRandomNonNeutral(),
                    BigInteger.valueOf(i));
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

    @Test
    public void testAddOddPowers() {
        GroupElement base = zp.createZnElement(BigInteger.valueOf(2)).toAdditiveGroupElement();
        // even max exponent
        GroupElement[] oddPowers = base.precomputeSmallOddPowers(8);
        addPrecomputations.addOddPowers(base, oddPowers);

        for (int i = 1; i <= 8; i+=2) {
            assertEquals(addPrecomputations.getPower(base, BigInteger.valueOf(i)), oddPowers[i/2]);
        }

        // uneven max exponent
        oddPowers = base.precomputeSmallOddPowers(9);
        addPrecomputations.addOddPowers(base, oddPowers);

        for (int i = 1; i <= 9; i+=2) {
            assertEquals(addPrecomputations.getPower(base, BigInteger.valueOf(i)), oddPowers[i/2]);
        }
    }

    @Test
    public void testGetOddPowers() {
        GroupElement base = zp.createZnElement(BigInteger.valueOf(2)).toAdditiveGroupElement();
        // even max exponent
        GroupElement[] oddPowers = base.precomputeSmallOddPowers(8);
        addPrecomputations.addOddPowers(base, oddPowers);
        GroupElement[] retrievedOddPowers = addPrecomputations.getOddPowers(base, 8);
        assertArrayEquals(oddPowers, retrievedOddPowers);
        retrievedOddPowers = addPrecomputations.getOddPowers(base, 7);
        assertArrayEquals(oddPowers, retrievedOddPowers);
    }
}
