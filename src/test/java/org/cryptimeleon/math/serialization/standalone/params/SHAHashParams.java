package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.hash.impl.SHA512HashFunction;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

import java.util.ArrayList;
import java.util.Collection;

public class SHAHashParams {

    public static Collection<StandaloneTestParams> get() {
        ArrayList<StandaloneTestParams> toReturn = new ArrayList<>();
        toReturn.add(new StandaloneTestParams(new SHA256HashFunction()));
        toReturn.add(new StandaloneTestParams(new SHA512HashFunction()));
        return toReturn;
    }
}
