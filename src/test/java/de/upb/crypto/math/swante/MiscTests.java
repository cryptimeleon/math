package de.upb.crypto.math.swante;

import org.junit.Assert;
import org.junit.Test;

public class MiscTests {
    
    @Test
    public void testBitLengthOfInt() {
        Assert.assertEquals(3, misc.bitLength(6));
        Assert.assertEquals(3, misc.bitLength(7));
        Assert.assertEquals(4, misc.bitLength(8));
        Assert.assertEquals(7, misc.bitLength(69));
        
    }
}
