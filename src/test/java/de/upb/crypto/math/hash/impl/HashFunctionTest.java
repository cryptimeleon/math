package de.upb.crypto.math.hash.impl;


import de.upb.crypto.math.hash.HashFunction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class HashFunctionTest {

    private HashFunction function;

    public HashFunctionTest(HashFunction function) {
        this.function = function;
    }

    @Test
    public void checkForCorrectness() {
        String testString = "IAMATEST";
        String testString2 = "IAMATEST";

        byte[] hash1 = function.hash(testString);
        byte[] hash2 = function.hash(testString2);
        Assert.assertArrayEquals(hash1, hash2);

    }

    @Test
    public void checkEscapedBytes() {
        String testString = "IAMA\tEST";
        String testString2 = "IAMAtEST";
        byte[] hash1 = function.hash(testString);
        byte[] hash2 = function.hash(testString2);
        Assert.assertFalse(Arrays.equals(hash1, hash2));
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<HashFunction> getParams() {
        ArrayList<HashFunction> list = new ArrayList<HashFunction>();
        list.add(new SHA256HashFunction());
        list.add(new SHA512HashFunction());
        return list;
    }
}
