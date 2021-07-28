package org.cryptimeleon.math;

import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.debug.DebugGroup;

public class ManualTest {
    public static void main(String[] args) {
        DebugGroup debugGroup = new DebugGroup("DG1", 1000000);
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();

        elem.op(elem).compute();
        debugGroup.setBucket("bucket1");
        elem.op(elem).compute();

        System.out.println("Default bucket: " + debugGroup.getNumSquaringsTotal());
        System.out.println("bucket1 bucket: " + debugGroup.getNumSquaringsTotal("bucket1"));
        }
}
