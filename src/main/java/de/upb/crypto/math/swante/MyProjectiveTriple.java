package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;

import java.math.BigInteger;

public class MyProjectiveTriple {
    public static final BigInteger zero = BigInteger.ZERO;
    public static final BigInteger one = BigInteger.ONE;
    public static final BigInteger two = BigInteger.valueOf(2);
    public static final BigInteger three = BigInteger.valueOf(3);
    public final BigInteger x;
    public final BigInteger y;
    public final BigInteger z;
    
    public MyProjectiveTriple(BigInteger x, BigInteger y, BigInteger z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public MyProjectiveTriple(BigInteger x, BigInteger y) {
        this(x, y, one);
    }
    
    public MyProjectiveTriple() {
        this(zero, one, zero); // neutral element
    }
    
    public MyProjectiveTriple add(BigInteger p, BigInteger curveParameterA, MyProjectiveTriple q) {
        if (q == this) {
            return times2(p, curveParameterA);
        }
        if (q.z.signum() == 0) {
            return this;
        }
        if (z.signum() == 0) {
            return q;
        }
        BigInteger x1z2 = x.multiply(q.z).mod(p);
        BigInteger v = q.x.multiply(z).mod(p).subtract(x1z2);
        BigInteger y1z2 = y.multiply(q.z).mod(p);
        BigInteger u = q.y.multiply(z).mod(p).subtract(y1z2);
        if (v.signum() == 0) {
            if (u.signum() == 0) {
                return this.times2(p, curveParameterA);
            }
            return new MyProjectiveTriple();
        }
        BigInteger uu = u.multiply(u).mod(p);
        BigInteger vv = v.multiply(v).mod(p);
        BigInteger vvv = v.multiply(vv).mod(p);
        BigInteger r = vv.multiply(x1z2).mod(p);
        BigInteger z1z2 = z.multiply(q.z).mod(p);
        BigInteger a = uu.multiply(z1z2).mod(p).subtract(vvv).subtract(r.shiftLeft(1));
        BigInteger rx = v.multiply(a).mod(p);
        BigInteger ry = u.multiply(r.subtract(a)).mod(p).subtract(vvv.multiply(y1z2).mod(p)).mod(p);
        BigInteger rz = vvv.multiply(z1z2).mod(p);
        return new MyProjectiveTriple(rx, ry, rz);
    }
    
    // returns this+this
    public MyProjectiveTriple times2(BigInteger p, BigInteger curveParameterA) {
        if (z.signum() == 0 || y.signum() == 0) {
            return new MyProjectiveTriple(zero, one, zero);
        }
        BigInteger xx = x.multiply(x).mod(p);
        BigInteger zz = z.multiply(z).mod(p);
        BigInteger w = curveParameterA.multiply(zz).mod(p).add(xx.multiply(three));
        BigInteger s = y.multiply(z).mod(p).shiftLeft(1);
        BigInteger ss = s.multiply(s).mod(p);
        BigInteger r = y.multiply(s).mod(p);
        BigInteger rr = r.multiply(r).mod(p);
        BigInteger xPlusR = x.add(r);
        BigInteger B = xPlusR.multiply(xPlusR).mod(p).subtract(xx).subtract(rr);
        BigInteger h = w.multiply(w).mod(p).subtract(B.shiftLeft(1));
        BigInteger rx = h.multiply(s).mod(p);
        BigInteger ry = w.multiply(B.subtract(h)).subtract(rr.shiftLeft(1)).mod(p);
        BigInteger rz = s.multiply(ss).mod(p);
        return new MyProjectiveTriple(rx, ry, rz);
    }
    
    public MyProjectiveTriple normalize(BigInteger p) {
        if (z.signum() == 0) {
            return this;
        }
        BigInteger div = z.modInverse(p);
        return new MyProjectiveTriple(x.multiply(div).mod(p), y.multiply(div).mod(p), one);
    }
    
    public MyProjectiveTriple pow(BigInteger p, BigInteger curveParameterA, BigInteger power) {
        if (power.signum() < 0)
            return pow(p, curveParameterA, power.negate()).invert();
        MyProjectiveTriple operand = this;
        
        MyProjectiveTriple result = new MyProjectiveTriple();
        for (int i = power.bitLength() - 1; i >= 0; i--) {
            result = result.times2(p, curveParameterA);
            if (power.testBit(i))
                result = result.add(p, curveParameterA, operand);
        }
        return result;
    }
    
    private MyProjectiveTriple invert() {
        return new MyProjectiveTriple(x, y.negate(), z);
    }
}

