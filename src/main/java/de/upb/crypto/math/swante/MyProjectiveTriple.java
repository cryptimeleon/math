package de.upb.crypto.math.swante;

import java.math.BigInteger;

public class MyProjectiveTriple {
    public static final BigInteger zero = BigInteger.ZERO;
    public static final BigInteger one = BigInteger.ONE;
    public static final BigInteger two = BigInteger.valueOf(2);
    public static final BigInteger three = BigInteger.valueOf(3);
    public final BigInteger p;
    public final BigInteger curveParameterA;
    public final BigInteger x;
    public final BigInteger y;
    public final BigInteger z;
    
    public MyProjectiveTriple(BigInteger p, BigInteger curveParameterA, BigInteger x, BigInteger y, BigInteger z) {
        this.p = p;
        this.curveParameterA = curveParameterA;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public MyProjectiveTriple(BigInteger p, BigInteger curveParameterA, BigInteger x, BigInteger y) {
        this(p, curveParameterA, x, y, one);
    }
    
    public MyProjectiveTriple(BigInteger p, BigInteger curveParameterA) {
        this(p, curveParameterA, zero, one, zero); // neutral element
    }
    
    private BigInteger modp(BigInteger x) {
        if (x.compareTo(p) < 0 && x.signum() >= 0) {
            return x;
        }
        return x.mod(p);
    }
    
    public MyProjectiveTriple add(MyProjectiveTriple q) {
        if (q == this) {
            return times2();
        }
        if (q.z.signum() == 0) {
            return this;
        }
        if (z.signum() == 0) {
            return q;
        }
        BigInteger x1z2 = modp(x.multiply(q.z));
        BigInteger v = modp(q.x.multiply(z)).subtract(x1z2);
        BigInteger y1z2 = modp(y.multiply(q.z));
        BigInteger u = modp(q.y.multiply(z)).subtract(y1z2);
        if (v.signum() == 0) {
            if (u.signum() == 0) {
                return this.times2();
            }
            return new MyProjectiveTriple(p, curveParameterA);
        }
        BigInteger uu = modp(u.multiply(u));
        BigInteger vv = modp(v.multiply(v));
        BigInteger vvv = modp(v.multiply(vv));
        BigInteger r = modp(vv.multiply(x1z2));
        BigInteger z1z2 = modp(z.multiply(q.z));
        BigInteger a = modp(uu.multiply(z1z2)).subtract(vvv).subtract(r.shiftLeft(1));
        BigInteger rx = modp(v.multiply(a));
        BigInteger ry = modp(u.multiply(r.subtract(a)).subtract(vvv.multiply(y1z2)));
        BigInteger rz = modp(vvv.multiply(z1z2));
        return new MyProjectiveTriple(p, curveParameterA, rx, ry, rz);
    }
    
    // returns this+this
    public MyProjectiveTriple times2() {
        if (z.signum() == 0 || y.signum() == 0) {
            return new MyProjectiveTriple(p, curveParameterA, zero, one, zero);
        }
        BigInteger xx = modp(x.multiply(x));
        BigInteger zz = modp(z.multiply(z));
        BigInteger w = modp(curveParameterA.multiply(zz)).add(xx.multiply(three));
        BigInteger s = modp(y.multiply(z)).shiftLeft(1);
        BigInteger ss = modp(s.multiply(s));
        BigInteger r = modp(y.multiply(s));
        BigInteger rr = modp(r.multiply(r));
        BigInteger xPlusR = x.add(r);
        BigInteger B = modp(xPlusR.multiply(xPlusR)).subtract(xx).subtract(rr);
        BigInteger h = modp(w.multiply(w)).subtract(B.shiftLeft(1));
        BigInteger rx = modp(h.multiply(s));
        BigInteger ry = modp(w.multiply(B.subtract(h)).subtract(rr.shiftLeft(1)));
        BigInteger rz = modp(s.multiply(ss));
        return new MyProjectiveTriple(p, curveParameterA, rx, ry, rz);
    }
    
    public MyProjectiveTriple normalize(BigInteger p) {
        if (z.signum() == 0) {
            return this;
        }
        BigInteger div = z.modInverse(p);
        return new MyProjectiveTriple(p, curveParameterA, x.multiply(div).mod(p), y.multiply(div).mod(p), one);
    }
    
    public MyProjectiveTriple pow(BigInteger power) {
        if (power.signum() < 0)
            return pow(power.negate()).invert();
        MyProjectiveTriple operand = this;
        
        MyProjectiveTriple result = new MyProjectiveTriple(p, curveParameterA);
        for (int i = power.bitLength() - 1; i >= 0; i--) {
            result = result.times2();
            if (power.testBit(i))
                result = result.add(operand);
        }
        return result;
    }
    
    private MyProjectiveTriple invert() {
        return new MyProjectiveTriple(p, curveParameterA, x, y.negate(), z);
    }
}

