package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.hash.impl.SHA256HashFunction;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;

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
