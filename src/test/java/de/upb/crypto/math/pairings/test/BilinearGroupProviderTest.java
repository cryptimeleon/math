package de.upb.crypto.math.pairings.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigBilinearGroup;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.supersingular.SupersingularTateGroup;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BilinearGroupProviderTest {

    @Test
    public void testTYPE3Provider() {
        BilinearGroupFactory groupFactory = new BilinearGroupFactory(80);
        groupFactory.setRequirements(BilinearGroup.Type.TYPE_3);
        assertEquals(groupFactory.createBilinearGroup().getClass(), BarretoNaehrigBilinearGroup.class);

        groupFactory.setSecurityParameter(128);
        groupFactory.registerProvider(Collections.singletonList(new BarretoNaehrigProvider()));
        assertEquals(groupFactory.createBilinearGroup().getClass(), BarretoNaehrigBilinearGroup.class);
    }

    @Test
    public void testTYPE1Provider() {
        BilinearGroupFactory groupFactory = new BilinearGroupFactory(80);
        groupFactory.setRequirements(BilinearGroup.Type.TYPE_1);

        assertEquals(groupFactory.createBilinearGroup().getClass(), SupersingularTateGroup.class);
    }

    @Test
    public void testTYPE2Provider() {
        BilinearGroupFactory groupFactory = new BilinearGroupFactory(80);
        groupFactory.setRequirements(BilinearGroup.Type.TYPE_2);

        try {
            BilinearGroup group = groupFactory.createBilinearGroup();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

}
