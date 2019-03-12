package de.upb.crypto.math.swante;

class A {
    public static class B {
        public A test() {
            return new A();
        }
    }
}


public class Foo {
    
    public static void main(String[] args) {
        A a = new A();
        new A.B();
    }
}
