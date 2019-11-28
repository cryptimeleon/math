package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.serialization.Representation;
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
                UncachedGroupPrecomputations.precomputeSmallOddPowers(base, 6).toArray());
    }

    @Test
    public void testAddGetPowerProducts() {
        List<GroupElement> bases = new ArrayList<>();
        int windowSize = 2;
        bases.add(zp.createZnElement(BigInteger.valueOf(4)).toUnitGroupElement());
        bases.add(zp.createZnElement(BigInteger.valueOf(3)).toUnitGroupElement());
        List<GroupElement> powerProducts = mulPrecomputations.getPowerProducts(bases, windowSize);
        GroupElement[] expectedPowerProducts = new GroupElement[] {
                zp.createZnElement(BigInteger.valueOf(1)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(4)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(16)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(64)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(3)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(4 * 3)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(16 * 3)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(64 * 3)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(9)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(4 * 9)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(16 * 9)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(64 * 9)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(27)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(4 * 27)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(16 * 27)).toUnitGroupElement(),
                zp.createZnElement(BigInteger.valueOf(64 * 27)).toUnitGroupElement()
        };
        assertArrayEquals(expectedPowerProducts, powerProducts.toArray());
    }

    @Test
    public void testRepresentation() {
        List<GroupElement> bases = new ArrayList<>();
        int windowSize = 2;
        bases.add(zp.createZnElement(BigInteger.valueOf(4)).toUnitGroupElement());
        bases.add(zp.createZnElement(BigInteger.valueOf(3)).toUnitGroupElement());
        mulPrecomputations.addPowerProducts(bases, windowSize);
        mulPrecomputations.addOddPowers(bases.get(0), windowSize);
        mulPrecomputations.addOddPowers(bases.get(1), windowSize);

        Representation repr = mulPrecomputations.getRepresentation();
        GroupPrecomputationsFactory.GroupPrecomputations mulPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, mulZp);
        assertEquals(mulPrecomputations2, mulPrecomputations);
    }

    @Test
    public void testAddGroupPrecomputationsExisting() {
        List<GroupElement> bases = new ArrayList<>();
        int windowSize = 2;
        bases.add(zp.createZnElement(BigInteger.valueOf(4)).toUnitGroupElement());
        bases.add(zp.createZnElement(BigInteger.valueOf(3)).toUnitGroupElement());
        mulPrecomputations.addPowerProducts(bases, windowSize);
        mulPrecomputations.addOddPowers(bases.get(0), windowSize);

        Representation repr = mulPrecomputations.getRepresentation();
        GroupPrecomputationsFactory.GroupPrecomputations mulPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, mulZp);
        mulPrecomputations2.addOddPowers(bases.get(1), windowSize);
        GroupPrecomputationsFactory.addGroupPrecomputations(mulPrecomputations2);
        assertEquals(mulPrecomputations2, mulPrecomputations);
    }

    @Test
    public void testAddGroupPrecomputationsNotExisting() {
        Representation repr = mulPrecomputations.getRepresentation();
        GroupPrecomputationsFactory.GroupPrecomputations mulPrecomputations2 =
                new GroupPrecomputationsFactory.GroupPrecomputations(repr, mulZp);
        List<GroupElement> bases = new ArrayList<>();
        int windowSize = 2;
        bases.add(zp.createZnElement(BigInteger.valueOf(4)).toUnitGroupElement());
        bases.add(zp.createZnElement(BigInteger.valueOf(3)).toUnitGroupElement());
        mulPrecomputations2.addPowerProducts(bases, windowSize);
        mulPrecomputations2.addOddPowers(bases.get(0), windowSize);
        mulPrecomputations2.addOddPowers(bases.get(1), windowSize);
        GroupPrecomputationsFactory.addGroupPrecomputations(mulPrecomputations2);
        assertEquals(mulPrecomputations2, mulPrecomputations);
    }
}
