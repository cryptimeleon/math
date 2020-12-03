package de.upb.crypto.math.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroup;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroupProvider;
import de.upb.crypto.math.pairings.debug.count.CountingGroup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManualTest {
    public static void main(String[] args) {
        /*Integer[] ints = new Integer[] {1, 2, 3};
        List<Integer> intList = Arrays.asList(ints);
        System.out.println(intList);
        ints[0] = 4;
        System.out.println(intList);*/

        Integer[] ints = new Integer[] {1, 2, 3};
        List<Integer> intList = new ArrayList<>(Arrays.asList(ints));
        System.out.println(intList);
        ints[0] = 4;
        System.out.println(intList);
    }
}
