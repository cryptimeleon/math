package de.upb.crypto.math.swante;

import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.interfaces.hash.HashFunction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

interface Foo {
    String foo();
}

class Fooa implements Foo {
    int i = 42;
    String s = "foobar";
    
    @Override
    public String foo() {
        return "foo aaaa";
    }
}

class Foob implements Foo {
    int i = 42;
    String s = "foobar";
    
    @Override
    public String foo() {
        return "foo bbbb";
    }
}

@RunWith(value = Parameterized.class)
public class FooTest {
    
    private Foo foo;
    
    public FooTest(Foo foo) {
        this.foo = foo;
    }
    
    @Test
    public void checkOne() {
        Assert.assertTrue(foo.foo().contains("f"));
        Assert.assertTrue(foo.foo().contains("o"));
    }
    
    @Test
    public void checkTwo() {
        Assert.assertTrue(foo.foo().contains("a"));
        Assert.assertTrue(foo.foo().contains("f"));
    }
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Foo> getParams() {
        ArrayList<Foo> list = new ArrayList<Foo>();
        list.add(new Fooa());
        list.add(new Foob());
        return list;
    }
}
