package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Random;

public class MyTestUtils {
    
    public static Random defaultRand = new Random();
    
    static {
        seedRand(0L);
    }
    
    public static void seedRand(long seed) {
        defaultRand.setSeed(seed);
    }
    
    
    public static BigInteger createPrimeWithGivenBitLength(int bitLength) {
        return BigInteger.probablePrime(bitLength, defaultRand);
    }
    
    public static Zp.ZpElement[] createRandomZpValues(Zp zp, int count) {
        Zp.ZpElement[] result = new Zp.ZpElement[count];
        for (int i = 0; i < count; i++) {
            result[i] = zp.getUniformlyRandomUnit();
        }
        return result;
    }
    
    public static AbstractEllipticCurvePoint[] createRandomCurvePoints(MyShortFormWeierstrassCurve curve, int numPoints) {
        AbstractEllipticCurvePoint[] result = new AbstractEllipticCurvePoint[numPoints];
        for (int i = 0; i < numPoints; i++) {
            result[i] = curve.getUniformlyRandomElement();
        }
        return result;
    }
}
