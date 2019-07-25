import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.ArrayList;

public class TestTreeVsList {
    public static void main(String[] args) {
        BilinearGroupFactory fac = new BilinearGroupFactory(80);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilgroup = fac.createBilinearGroup();
        Group group = bilgroup.getG1();
        Zp zn = new Zp(group.size());
        int n = 2000;

        for (int j=0;j<10;j++) {
            //Array
            long t = System.nanoTime();
            Zp.ZpElement currentExp = zn.getOneElement();
            GroupElement fixed = group.getUniformlyRandomElement();
            ArrayList<GroupElement> elems = new ArrayList<>();
            ArrayList<Zp.ZpElement> exps = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                //elems.add(group.getUniformlyRandomElement());
                elems.add(fixed);
                //exps.add(zn.getUniformlyRandomElement());
                exps.add(currentExp = currentExp.add(zn.getOneElement()));
            }

            GroupElement result = group.getNeutralElement();
            for (int i = 0; i < n; i++)
                result = result.op(elems.get(i).pow(exps.get(i)));

            long duration = System.nanoTime() - t;
            System.out.println("Array: "+ (duration / 1000000) + "ms");


            //Tree
            currentExp = zn.getOneElement();
            t = System.nanoTime();
            TestExpr expr = new TestExp(group.getNeutralElement(), zn.getZeroElement());

            for (int i = 0; i < n; i++) {
                //expr = new TestProd(new TestExp(group.getUniformlyRandomElement(), zn.getUniformlyRandomElement()), expr);
                //expr = new TestProd(new TestExp(group.getUniformlyRandomElement(), currentExp = currentExp.add(zn.getOneElement())), expr);
                expr = new TestProd(new TestExp(fixed, currentExp = currentExp.add(zn.getOneElement())), expr);
            }

            result = expr.evaluate();

            duration = System.nanoTime() - t;
            System.out.println("Tree: "+(duration / 1000000) + "ms");
        }
    }
}
