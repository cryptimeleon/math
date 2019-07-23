package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.pairings.bn.*;
import de.upb.crypto.math.pairings.generic.AbstractPairing;

import static de.upb.crypto.math.swante.util.MyUtil.myAssert;
import static de.upb.crypto.math.swante.util.MyUtil.pln;

public class ThesisPairings {
    public static void main(String[] args) {
        pln("=========================");
        if (args.length == 0) {
            args = "128 1 5 Tate".split(" ");
        }
        int bitLength = Integer.parseInt(args[0]);
        AbstractPairing pairing;
        if (args[3].equals("Ate")) { // Ate
            pairing = MyBarretoNaehrigAtePairing.createAtePairing(bitLength);
        } else { // Tate
            BarretoNaehrigProvider bnProvider = new BarretoNaehrigProvider();
            BilinearMap bnMap = bnProvider.provideBilinearGroup(bitLength, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3)).getBilinearMap();
            pairing = new BarretoNaehrigTatePairing(((BarretoNaehrigSourceGroup) bnMap.getG1()), ((BarretoNaehrigSourceGroup) bnMap.getG2()), ((BarretoNaehrigTargetGroup) bnMap.getGT()));
        }
        int numPoints = Integer.parseInt(args[1]);
        int numIterations = Integer.parseInt(args[2]);
        BarretoNaehrigGroup1Element[] A = new BarretoNaehrigGroup1Element[numPoints];
        BarretoNaehrigGroup2Element[] B = new BarretoNaehrigGroup2Element[numPoints];
        for (int i = 0; i < numPoints; i++) {
            A[i] = (BarretoNaehrigGroup1Element) pairing.getG1().getUniformlyRandomNonNeutral();
            B[i] = (BarretoNaehrigGroup2Element) pairing.getUnitRandomElementFromG2Group();
        }
        pln(args);
        double startMillis = System.nanoTime() / 1.0e6;
        for (int iter = -numIterations; iter < numIterations; iter++) {
            if (iter == 0) { // start timing only after warmup phase
                startMillis = System.nanoTime() / 1.0e6;
            }
            for (int i = 0; i < numPoints; i++) {
                pairing.apply(A[i], B[i]);
            }
        }
        double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
        pln("Result: " + elapsedMillis);
    }
}
