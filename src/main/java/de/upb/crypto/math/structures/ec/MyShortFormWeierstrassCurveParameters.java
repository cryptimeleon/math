package de.upb.crypto.math.structures.ec;

import java.math.BigInteger;

public class MyShortFormWeierstrassCurveParameters {
    
    final BigInteger p;
    final BigInteger a;
    final BigInteger b;
    final BigInteger gx;
    final BigInteger gy;
    final BigInteger n;
    final BigInteger h;
    
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
     * creates parameters for a secure weierstrass short form curve
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
