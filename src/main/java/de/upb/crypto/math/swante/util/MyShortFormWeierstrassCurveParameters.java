package de.upb.crypto.math.swante.util;

import java.math.BigInteger;

public class MyShortFormWeierstrassCurveParameters {
    
    public final BigInteger p;
    public final BigInteger a;
    public final BigInteger b;
    public final BigInteger gx;
    public final BigInteger gy;
    public final BigInteger n;
    public final BigInteger h;
    
    public MyShortFormWeierstrassCurveParameters(BigInteger p, BigInteger a, BigInteger b, BigInteger gx, BigInteger gy, BigInteger n, BigInteger h) {
        
        this.p = p;
        this.a = a;
        this.b = b;
        this.gx = gx;
        this.gy = gy;
        this.n = n;
        this.h = h;
    }
    
    /**
     * creates parameters for a secure 192bit weierstrass short form curve
     */
    public static MyShortFormWeierstrassCurveParameters createSecp192r1CurveParameters() {
        BigInteger p=new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFF", 16);
        BigInteger a=new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFC", 16);
        BigInteger b=new BigInteger("64210519E59C80E70FA7E9AB72243049FEB8DEECC146B9B1", 16);
        BigInteger Gx=new BigInteger("188DA80EB03090F67CBF20EB43A18800F4FF0AFD82FF1012", 16);
        BigInteger Gy=new BigInteger("07192B95FFC8DA78631011ED6B24CDD573F977A11E794811", 16);
        BigInteger n=new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFF99DEF836146BC9B1B4D22831", 16);
        BigInteger h=new BigInteger("01", 16);
        return new MyShortFormWeierstrassCurveParameters(p,a,b,Gx,Gy,n,h);
    }
    /**
     * creates parameters for a secure 256bit weierstrass short form curve
     */
    public static MyShortFormWeierstrassCurveParameters createSecp256r1CurveParameters() {
        BigInteger p=new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
        BigInteger a=new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16);
        BigInteger b=new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);
        BigInteger Gx=new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16);
        BigInteger Gy=new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16);
        BigInteger n=new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);
        BigInteger h=new BigInteger("01", 16);
        return new MyShortFormWeierstrassCurveParameters(p,a,b,Gx,Gy,n,h);
    }
}
