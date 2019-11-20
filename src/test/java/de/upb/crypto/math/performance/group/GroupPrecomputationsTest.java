package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    public void testAddGetOddPowersUnevenMaxExp() {
        int maxExp = 5;
        GroupElement base = mulZp.getUniformlyRandomNonNeutral();
        System.out.println("Chose base: " + base.toString());

        mulPrecomputations.addOddPowers(base, maxExp);

        List<GroupElement> correctOddPowers = new LinkedList<>();
        correctOddPowers.add(base);
        correctOddPowers.add(base.pow(3));
        correctOddPowers.add(base.pow(5));

        System.out.println("Actual: " + Arrays.toString(
                mulPrecomputations.getOddPowers(base, maxExp).toArray()));
        System.out.println("Expected: " + Arrays.toString(
                correctOddPowers.toArray()));

        assertArrayEquals(
                correctOddPowers.toArray(),
                mulPrecomputations.getOddPowers(base, maxExp).toArray()
        );
    }

    @Test
    public void testAddGetOddPowersEvenMaxExp() {
        int maxExp = 6;
        GroupElement base = mulZp.getUniformlyRandomNonNeutral();
        System.out.println("Chose base: " + base.toString());

        mulPrecomputations.addOddPowers(base, maxExp);

        List<GroupElement> correctOddPowers = new LinkedList<>();
        correctOddPowers.add(base);
        correctOddPowers.add(base.pow(3));
        correctOddPowers.add(base.pow(5));

        System.out.println("Actual: " + Arrays.toString(
                mulPrecomputations.getOddPowers(base, maxExp).toArray()));
        System.out.println("Expected: " + Arrays.toString(
                correctOddPowers.toArray()));

        assertArrayEquals(
                correctOddPowers.toArray(),
                mulPrecomputations.getOddPowers(base, maxExp).toArray()
        );
    }

    @Test
    public void testNotCachedOddPowers() {
        GroupElement base = mulZp.getUniformlyRandomNonNeutral();
        System.out.println("Chose base: " + base.toString());

        List<GroupElement> correctOddPowers = new LinkedList<>();
        correctOddPowers.add(base);
        correctOddPowers.add(base.pow(3));
        correctOddPowers.add(base.pow(5));

        assertArrayEquals(correctOddPowers.toArray(),
                base.precomputeSmallOddPowers(6).toArray());
    }

    @Test
    public void testAddGetPowerProducts() {
        List<GroupElement> bases = new ArrayList<>();
        int numBases = 3;
        int windowSize = 2;
        for (int i = 0; i < numBases; ++i) {
            bases.add(mulZp.getUniformlyRandomNonNeutral());
        }
        List<GroupElement> powerProducts = mulPrecomputations.getPowerProducts(bases, windowSize);
        assertNotEquals(null, powerProducts);
    }
}
