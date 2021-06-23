package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import java.math.BigInteger;

/**
 * Represents a fixed parametrization of a Barreto-Naehrig bilinear group.
 * 
 * @see BarretoNaehrigBilinearGroup#BarretoNaehrigBilinearGroup(BarretoNaehrigParameterSpec) 
 * @see BarretoNaehrigBilinearGroup#BarretoNaehrigBilinearGroup(String) 
 * @see BarretoNaehrigBasicBilinearGroup#BarretoNaehrigBasicBilinearGroup(BarretoNaehrigParameterSpec)
 * @see BarretoNaehrigBasicBilinearGroup#BarretoNaehrigBasicBilinearGroup(String) 
 */
public class BarretoNaehrigParameterSpec {
    public final BigInteger u;
    public final BigInteger characteristic;
    public final BigInteger size;
    public final BigInteger alpha;
    public final BigInteger beta0;
    public final BigInteger beta1;
    public final BigInteger b;
    public final BigInteger x1;
    public final BigInteger y1;
    public final BigInteger x20;
    public final BigInteger x21;
    public final BigInteger y20;
    public final BigInteger y21;
    public final String pairing;
    public final String hash;

    public BarretoNaehrigParameterSpec(BigInteger u, BigInteger characteristic, BigInteger size, BigInteger alpha, BigInteger beta0, BigInteger beta1, BigInteger b, BigInteger x1, BigInteger y1, BigInteger x20, BigInteger x21, BigInteger y20, BigInteger y21,
                                       String pairing, String hash) {
        super();
        this.u = u;
        this.characteristic = characteristic;
        this.size = size;
        this.alpha = alpha;
        this.beta0 = beta0;
        this.beta1 = beta1;
        this.b = b;
        this.x1 = x1;
        this.y1 = y1;
        this.x20 = x20;
        this.x21 = x21;
        this.y20 = y20;
        this.y21 = y21;
        this.pairing = pairing;
        this.hash = hash;
    }

    /**
     * Returns parameters for a BN instantiation where the group order is 256 bits long, resulting in a security
     * parameter of roughly 100 bits.
     */
    public static BarretoNaehrigParameterSpec sfc256() {
        return new BarretoNaehrigParameterSpec(
                new BigInteger("36893488147419130051", 10),
                new BigInteger("2400000000001d76ea000000090b16017d00013bcce1b73032502782f6c062b4d9b", 16),
                new BigInteger("2400000000001d76ea000000090b16017b80013bcce1b6930dd02782f6b04f13265", 16),
                BigInteger.ONE,
                BigInteger.valueOf(2),
                BigInteger.ONE,
                new BigInteger("e9e8726d4b33cd66913d4f313376a585cb983ee62a48809f4e04d39792c527842e", 16),
                new BigInteger("194afbc2e081e6d4327c167b1fd3399d88b5aa73e570101f34cc9db540d64ecefd8", 16),
                BigInteger.ONE,
                new BigInteger("38216abeb4824dfaceaca25dfede4614e2c7577ff718277c7e9e246608b14e6ec7", 16),
                new BigInteger("1860c7978845fd8526d1f097096e8b8a0b0738785906bbf9aaf7bf5c4030ccf57c1", 16),
                new BigInteger("169b69fadcb34eb34f1abfc928660086714afb9b016bba98e866223bfe2d5bac2d2", 16),
                new BigInteger("bf44311e7048ad5827f3ade3dc4c86655735a4ab8dd0c60671a79ee2aaf1bf2207", 16),
                "Tate",
                "SHA-256"
        );
    }

    /**
     * Returns the {@code BarretoNaehrigParameterSpec} belonging to the given spec string.
     * <p>
     * The currently available specs are:
     * <ul>
     *     <li> {@code "SFC-256"}: a 256 bit size spec resulting in a security level of roughly 100 bits
     * </ul>
     * @param spec a {@code String} specifying the parameter spec
     * @return the corresponding spec
     * @throws IllegalArgumentException if the given spec string does not correspond to any
     */
    public static BarretoNaehrigParameterSpec getParameters(String spec) {
        if (spec.equals("SFC-256")) {
            return sfc256();
        }
        throw new IllegalArgumentException("Unknown cipher spec.");
    }
}
