import de.upb.crypto.math.interfaces.structures.GroupElement;

public class TestProd implements TestExpr {
    TestExpr lhs, rhs;

    public TestProd(TestExpr lhs, TestExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluate() {
        return lhs.evaluate().op(rhs.evaluate());
    }
}
