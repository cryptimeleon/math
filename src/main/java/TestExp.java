import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class TestExp implements TestExpr {
    Zp.ZpElement exp;
    GroupElement g;

    public TestExp(GroupElement g, Zp.ZpElement exp) {
        this.exp = exp;
        this.g = g;
    }

    @Override
    public GroupElement evaluate() {
        return g.pow(exp);
    }
}
