package de.upb.crypto.math.swante;

import static de.upb.crypto.math.swante.misc.pln;

class A {
    int x;
    int y = x+2;
    public A(int i) {
        x = i;
    }
}


public class Foo {
    
    public static void main(String[] args) {
        A a = new A(4);
        pln(a.x, a.y);
    }
}
