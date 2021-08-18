package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.serialization.BigIntegerRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.AbstractEllipticCurvePoint;
import org.cryptimeleon.math.structures.groups.elliptic.AffineEllipticCurvePoint;
import org.cryptimeleon.math.structures.groups.elliptic.PairingSourceGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.WeierstrassCurve;
import org.cryptimeleon.math.structures.groups.elliptic.nopairing.Secp256k1;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigParameterSpec;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CurveTest {

    @Test
    public void testIsOnCurve() {
        Secp256k1 curve = new Secp256k1();
        WeierstrassCurve curveImpl = ((WeierstrassCurve) curve.getImpl());
        FieldElement x = curveImpl.getFieldOfDefinition().restoreElement(new BigIntegerRepresentation(
                new BigInteger("67666341147119015517745455968511312184352216481177330889575508950261290521184")
        ));
        FieldElement y = curveImpl.getFieldOfDefinition().restoreElement(new BigIntegerRepresentation(
                new BigInteger("10255259455662915565452952018722490464546702313657804382412957617802230297684")
        ));
        assertTrue(curveImpl.isOnCurve(x, y));
        // now check invalid point
        assertFalse(curveImpl.isOnCurve(x, y.add(curveImpl.getFieldOfDefinition().getOneElement())));
    }
}
