package de.upb.crypto.math.swante;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class misc {
    
    public static void main(String[] args) {
        pln(randInts(15,3,9).toString());
        pln(tos(randInts(15,3,9)));
        pln(setOf("hello", "nice"));
        myAssert(1<0, () -> "fail");
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
    
    public static <K,V> HashMap<K,V> mapOf(Pair<K,V>... elements) {
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
    
    private static Random rand = new Random();
    
    static {
        seedRand(0L);
    }
    
    public static void seedRand(long seed) {
        rand.setSeed(seed);
    }
    
    
    private static double lastTickMillis = System.nanoTime()/1.0e6;
    
    /**
     * @return time since last call of tick (or program startup), in milli-seconds
     */
    public static double tick() {
        double currentTimeInMillis = System.nanoTime()/1.0e6;
        double deltaTimeInMillis = currentTimeInMillis - lastTickMillis;
        lastTickMillis = currentTimeInMillis;
        return deltaTimeInMillis;
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
        BigInteger result = new BigInteger(maxExcluding.bitLength(), rand);
        while( result.compareTo(maxExcluding) >= 0 ) {
            result = new BigInteger(maxExcluding.bitLength(), rand);
        }
        return result;
    }
    
    public static ArrayList<Integer> randInts(int count, int min, int maxExclusive) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(rand.nextInt(maxExclusive-min) + min);
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
            res.add(min + rand.nextFloat()*(maxExclusive-min));
        }
        return res;
    }
    
    public static ArrayList<Double> randDoubles(int count, double min, double maxExclusive) {
        ArrayList<Double> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(min + rand.nextDouble()*(maxExclusive-min));
        }
        return res;
    }
    
    public static void pln(Object... objs) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            sb.append(" ").append(obj.toString());
        }
        if (sb.length()==0) {
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
        if (sb.length()==0) {
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
        if (sb.length()==0) {
            return "[]";
        }
        return "[" + sb.toString().substring(2) + "]";
    }
    
    
}