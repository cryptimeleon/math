package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
//import de.upb.crypto.math.pairings.mcl.MclGroup1;
//import de.upb.crypto.math.pairings.mcl.MclGroupT;

public class OpVsInv {
    public static void main(String[] args) {
        Group group = null;
        //Group group = new MclGroup1();
        for (int k = 0; k < 10; ++k) {
            System.out.println("Benchmark " + k);
            GroupElement[] testElements = new GroupElement[1000];
            for (int i = 0; i < testElements.length; ++i) {
                testElements[i] = group.getUniformlyRandomNonNeutral();
            }

            GroupElement tmp = group.getNeutralElement();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; ++i) {
                for (GroupElement testElement : testElements) {
                    tmp = tmp.op(testElement);
                }
            }
            System.out.println("Time for op (in ms): " + (System.currentTimeMillis() - startTime));


            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; ++i) {
                for (GroupElement testElement : testElements) {
                    tmp = testElement.inv();
                }
            }
            System.out.println("Time for inv (in ms): " + (System.currentTimeMillis() - startTime));
        }
    }
}
