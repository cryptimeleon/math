package de.upb.crypto.math.swante;

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
    
    public MyProjectiveTriple add(BigInteger p, BigInteger a, MyProjectiveTriple q) {
        if (q == this) {
            return times2(p, a);
        }
        if (q.z.signum() == 0) {
            return this;
        }
        if (z.signum() == 0) {
            return q;
        }
        BigInteger t0 = y.multiply(q.z).mod(p);
        BigInteger t1 = q.y.multiply(z).mod(p);
        BigInteger u0 = x.multiply(q.z).mod(p);
        BigInteger u1 = q.x.multiply(z).mod(p);
        if (u0.equals(u1)) {
            if (t0.equals(t1)) {
                return times2(p, a);
            }
            return new MyProjectiveTriple(zero, one, zero);
        }
        BigInteger t = t0.subtract(t1).mod(p);
        BigInteger u = u0.subtract(u1).mod(p);
        BigInteger u2 = u.multiply(u).mod(p);
        BigInteger v = z.multiply(q.z).mod(p);
        BigInteger w = t.multiply(t).mod(p).multiply(v).mod(p).subtract(u2.multiply(u0.add(u1)).mod(p));
        BigInteger u3 = u.multiply(u2).mod(p);
        BigInteger rx = u.multiply(w).mod(p);
        BigInteger ry = t.multiply(u0.multiply(u2).mod(p).subtract(w)).mod(p).subtract(t0.multiply(u3).mod(p)).mod(p);
        BigInteger rz = u3.multiply(v).mod(p);
        return new MyProjectiveTriple(rx, ry, rz);
    }
    
    // returns this+this
    public MyProjectiveTriple times2(BigInteger p, BigInteger curveParameterA) {
        if (z.signum() == 0 || y.signum() == 0) {
            return new MyProjectiveTriple(zero, one, zero);
        }
        BigInteger t = x.multiply(x).multiply(three).mod(p).add(z.multiply(z).mod(p).multiply(curveParameterA).mod(p));
        BigInteger u = y.multiply(z).shiftLeft(1).mod(p);
        BigInteger v = u.multiply(x).mod(p).multiply(y).shiftLeft(1).mod(p);
        BigInteger w = t.multiply(t).subtract(v.shiftLeft(1)).mod(p);
        BigInteger rx = u.multiply(w).mod(p);
        BigInteger u2 = u.multiply(u).mod(p);
        BigInteger ry = t.multiply(v.subtract(w)).mod(p).subtract(u2.multiply(y.multiply(y).shiftLeft(1).mod(p)).mod(p)).mod(p);
        BigInteger rz = u2.multiply(u).mod(p);
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

