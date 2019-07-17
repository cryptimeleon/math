package de.upb.crypto.math.swante;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigGroup1;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class misc {
    
    public static void main(String[] args) {
        pln(randInts(15, 3, 9).toString());
        pln(tos(randInts(15, 3, 9)));
        pln(setOf("hello", "nice"));
        myAssert(1 < 0, () -> "fail");
    }
    
    /**
     * @param i
     * @return Just like BigInteger.bitLength, but for a simple int.
     */
    public static int bitLength(int i) {
        int res = 0;
        if (i >= 65536) {
            i >>= 16;
            res += 16;
        }
        if (i >= 256) {
            i >>= 8;
            res += 8;
        }
        if (i >= 16) {
            i >>= 4;
            res += 4;
        }
        if (i >= 4) {
            i >>= 2;
            res += 2;
        }
        if (i >= 2) {
            i >>= 1;
            res += 1;
        }
        return res + i;
    }
    
    /**
     * @return Lowest n bits of i. Works for all n < 32.
     */
    public static int getNLeastSignificantBits(int i, int numberOfLowBits) {
        return i & ((1 << numberOfLowBits) - 1);
    }
    
    static class Pair<A, B> {
        public final A a;
        public final B b;
        
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
    
    static class Triple<A, B, C> {
        public final A a;
        public final B b;
        public final C c;
        
        public Triple(A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
    
    public static <T> ArrayList<T> listOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }
    
    public static <T> HashSet<T> setOf(T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
    
    public static <K, V> HashMap<K, V> mapOf(Pair<K, V>... elements) {
        HashMap<K, V> hashMap = new HashMap<>();
        for (Pair<K, V> element : elements) {
            hashMap.put(element.a, element.b);
        }
        return hashMap;
    }
    
    public static void myAssert(boolean condition) {
        if (!condition) {
            throw new AssertionError("myAssert failed! (Without error message)");
        }
    }
    
    // also throws AssertionErrors during Runtime, so better than default assert
    public static void myAssert(boolean condition, Supplier<String> messageGenerator) {
        if (!condition) {
            throw new AssertionError("myAssert failed! Error message:\n" +
                    messageGenerator.get());
        }
    }
    
    private static Random defaultRand = new Random();
    
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
    
    /**
     * @param bitLength
     * @return parameters for MyShortFormWeierstassCurve for a BN curve of given bitLength
     */
    public static MyShortFormWeierstrassCurveParameters createBnWeierstrassCurveGroupParams(int bitLength) {
        BarretoNaehrigProvider bnProvider = new BarretoNaehrigProvider();
        BilinearMap bnMap = null;
        if (bitLength == 256) { // Barreto-Naehrig non-native, SFC-256
            bnMap = bnProvider.provideBilinearGroupFromSpec(BarretoNaehrigProvider.ParamSpecs.SFC256).getBilinearMap();
        } else {
            // Barreto-Naehrig non-native
            bnMap = bnProvider.provideBilinearGroup(bitLength, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3)).getBilinearMap();
        }
        BarretoNaehrigGroup1 g1 = (BarretoNaehrigGroup1) bnMap.getG1();
        AbstractEllipticCurvePoint G = ((AbstractEllipticCurvePoint) g1.getGenerator()).normalize();
        BigInteger h = new BigInteger("01", 16);
        BigInteger n = g1.size();
        Zp.ZpElement b = (Zp.ZpElement) ((ExtensionFieldElement) g1.getA6()).getCoefficients()[0];
        Zp.ZpElement Gx = (Zp.ZpElement) ((ExtensionFieldElement) G.getX()).getCoefficients()[0];
        Zp.ZpElement Gy = (Zp.ZpElement) ((ExtensionFieldElement) G.getY()).getCoefficients()[0];
        BigInteger p = Gx.getStructure().size();
        return new MyShortFormWeierstrassCurveParameters(p, BigInteger.ZERO, b.getInteger(), Gx.getInteger(), Gy.getInteger(), n, h);
    }
    
    
    private static double lastTickMillis = System.nanoTime() / 1.0e6;
    
    /**
     * @return time since last call of tick (or program startup), in milli-seconds
     */
    public static double tick() {
        double currentTimeInMillis = System.nanoTime() / 1.0e6;
        double deltaTimeInMillis = currentTimeInMillis - lastTickMillis;
        lastTickMillis = currentTimeInMillis;
        return deltaTimeInMillis;
    }
    
    public static void sleep(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000.0));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<Integer> arange(int maxExclusive) {
        return arange(0, maxExclusive);
    }
    
    public static ArrayList<Integer> arange(int min, int maxExclusive) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = min; i < maxExclusive; i++) {
            res.add(i);
        }
        return res;
    }
    
    public static BigInteger randBig(BigInteger maxExcluding) {
        BigInteger result = new BigInteger(maxExcluding.bitLength(), defaultRand);
        while (result.compareTo(maxExcluding) >= 0) {
            result = new BigInteger(maxExcluding.bitLength(), defaultRand);
        }
        return result;
    }
    
    public static ArrayList<Integer> randInts(int count, int min, int maxExclusive) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(defaultRand.nextInt(maxExclusive - min) + min);
        }
        return res;
    }
    
    
    public static ArrayList<Long> randLongs(int count, long min, long maxExclusive) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        ArrayList<Long> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(rand.nextLong(min, maxExclusive));
        }
        return res;
    }
    
    public static ArrayList<Float> randFloats(int count, float min, float maxExclusive) {
        ArrayList<Float> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(min + defaultRand.nextFloat() * (maxExclusive - min));
        }
        return res;
    }
    
    public static ArrayList<Double> randDoubles(int count, double min, double maxExclusive) {
        ArrayList<Double> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(min + defaultRand.nextDouble() * (maxExclusive - min));
        }
        return res;
    }
    
    public static void pln(Object... objs) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            sb.append(" ").append(obj.toString());
        }
        if (sb.length() == 0) {
            System.out.println();
            return;
        }
        String res = sb.toString().substring(1);
        System.out.println(res);
    }
    
    public static <T> String tos(Iterable<T> list) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(", ").append(obj.toString());
        }
        if (sb.length() == 0) {
            return "[]";
        }
        String res = "[" + sb.toString().substring(2) + "]";
        return res;
    }
    
    public static <T> String tos(T[] list) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(", ").append(obj.toString());
        }
        if (sb.length() == 0) {
            return "[]";
        }
        return "[" + sb.toString().substring(2) + "]";
    }
    
    
}