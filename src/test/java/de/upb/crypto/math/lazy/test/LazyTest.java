package de.upb.crypto.math.lazy.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.lazy.LazyPairing;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class LazyTest {
    protected BilinearMap pairing;
    protected ArrayList<GroupElement> g1Elems = new ArrayList<>();
    protected ArrayList<GroupElement> g2Elems = new ArrayList<>();
    protected ArrayList<BigInteger> exponents = new ArrayList<>();
    long timerStart = 0;

    public LazyTest(BilinearMap e) {
        this.pairing = e;
    }

    @Before
    public void populateTestData() {
        Group g1 = pairing.getG1();
        Group g2 = pairing.getG2();
        Zn zn = new Zn(g1.size());

        g1Elems = new ArrayList<>();
        g2Elems = new ArrayList<>();
        exponents = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            g1Elems.add(g1.getUniformlyRandomElement());
            g2Elems.add(g2.getUniformlyRandomElement());
            exponents.add(zn.getUniformlyRandomElement().getInteger());
        }
    }

    @Test
    public void longProductG1() {
        measureTime();
        GroupElement result = pairing.getG1().getNeutralElement();
        for (int i = 0; i < g1Elems.size(); i++) {
            result = result.op(g1Elems.get(i).pow(exponents.get(i / 2)));
        }
        System.out.println();
        System.out.println(result.getRepresentation());
        measureTime("longProductG1");
    }

    @Test
    public void longProductG2() {
        measureTime();
        GroupElement result = pairing.getG2().getNeutralElement();
        for (int i = 0; i < g2Elems.size(); i++) {
            result = result.op(g2Elems.get(i).pow(exponents.get(i)));
        }
        System.out.println(result.getRepresentation());
        measureTime("longProductG2");
    }

    protected void measureTime() {
        measureTime("");
    }

    protected void measureTime(String str) {
        if (timerStart == 0) {
            timerStart = System.currentTimeMillis();
        } else {
            long end = System.currentTimeMillis();
            System.out.println(pairing.toString() + " " + str + ": " + ((end - timerStart) / 1000) + "s, " + ((end - timerStart) % 1000) + "ms");
            timerStart = 0;
        }
    }

    @Test
    public void pairings() {
        measureTime();
        GroupElement result = pairing.getGT().getNeutralElement();
        for (int i = 0; i < g1Elems.size(); i++) {
            result = pairing.apply(g1Elems.get(i).pow(exponents.get(i)), g2Elems.get(i));
        }
        System.out.println(result.getRepresentation());
        measureTime("pairings");
    }

    @Test
    public void pairingsWithSameG1() {
        measureTime();
        GroupElement result = pairing.getGT().getNeutralElement();
        for (int i = 0; i < g1Elems.size(); i++) {
            result = pairing.apply(g1Elems.get(0), g2Elems.get(i)).pow(exponents.get(i));
        }
        System.out.println(result.getRepresentation());
        measureTime("pairings");
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<BilinearMap> getParams() {
        BilinearGroupFactory fac = new BilinearGroupFactory(64);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup group = fac.createBilinearGroup();
        BilinearMap pairing = group.getBilinearMap();
        LazyPairing lazyPairing = new LazyPairing(pairing);

        ArrayList<BilinearMap> list = new ArrayList<BilinearMap>();
        list.add(pairing);
        list.add(lazyPairing);
        return list;
    }
}
