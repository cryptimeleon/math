package de.upb.crypto.math.swante;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import org.junit.Test;

import static de.upb.crypto.math.swante.misc.pln;




public class BarTest {
    
    
    @Test
    public void testOne() {
        int securityParameter = 200;
        BilinearGroupFactory fac = new BilinearGroupFactory(securityParameter);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup group = fac.createBilinearGroup();
        pln(group.getG1());
        
    }
}
