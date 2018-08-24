package de.upb.crypto.math.lazy;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.util.Objects;

/**
 * Factory that creates a LazyPairing, i.e. a pairing whose operations
 * are lazily evaluated.
 */
public class LazyBilinearGroup implements BilinearGroup {
    protected LazyPairing pairing;

    @Represented
    protected BilinearGroup baseParams;

    /**
     * Creates a new LazyBilinearGroup based on the given {@link BilinearGroup}.
     *
     * @param baseParams already initialized factory
     */
    public LazyBilinearGroup(BilinearGroup baseParams) {
        this.baseParams = baseParams;
        init();
    }

    public LazyBilinearGroup(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
        init();
    }

    private void init() {
        pairing = new LazyPairing(baseParams.getBilinearMap());
    }

    @Override
    public LazyGroup getG1() {
        return pairing.getG1();
    }

    @Override
    public LazyGroup getG2() {
        return pairing.getG2();
    }

    @Override
    public LazyGroup getGT() {
        return pairing.getGT();
    }

    @Override
    public LazyPairing getBilinearMap() {
        return pairing;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(); //if anyone needs it, feel free to write it.
    }

    @Override
    public HashIntoLazyGroup getHashIntoG1() throws UnsupportedOperationException {
        return new HashIntoLazyGroup(getG1(), baseParams.getHashIntoG1());
    }

    @Override
    public HashIntoLazyGroup getHashIntoG2() throws UnsupportedOperationException {
        return new HashIntoLazyGroup(getG2(), baseParams.getHashIntoG2());
    }

    @Override
    public HashIntoLazyGroup getHashIntoGT() throws UnsupportedOperationException {
        return new HashIntoLazyGroup(getGT(), baseParams.getHashIntoGT());
    }

    @Override
    public HashIntoStructure getHashIntoZGroupExponent() throws UnsupportedOperationException {
        return baseParams.getHashIntoZGroupExponent();
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyBilinearGroup that = (LazyBilinearGroup) o;
        return Objects.equals(baseParams, that.baseParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseParams);
    }
}
