package org.cryptimeleon.math.prf.aes;

import org.cryptimeleon.math.misc.ByteArrayImpl;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.prf.PrfPreimage;
import org.cryptimeleon.math.prf.PseudorandomFunction;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Objects;

/**
 * AES based PRF with k key length and output length of the underlying AES key length.
 * PRF_k(x) = AES_k1(x)||AES_k2(x)|..." with key k=(k1,k2,...)
 * <p>
 * This is basically a wrapper around AesPseudorandomFunction.
 **/
public class LongAesPseudoRandomFunction implements PseudorandomFunction {

    @Represented
    private AesPseudorandomFunction aesPseudorandomFunction;
    @Represented
    private Integer factor;
    private int preimageLengthBytes;
    private int keyLengthBytes;

    /**
     * Instantiates the PRF with an AES instance and desired factor.
     *
     * @param k                       factor by which output and key size of AES is increased
     * @param aesPseudorandomFunction AES instance to use k times
     */
    public LongAesPseudoRandomFunction(AesPseudorandomFunction aesPseudorandomFunction, int k) {
        this.aesPseudorandomFunction = aesPseudorandomFunction;
        this.factor = k;
        this.init();
    }

    public LongAesPseudoRandomFunction(Representation repr) {
        new ReprUtil(this).deserialize(repr);
        this.init();
    }

    private void init() {
        this.preimageLengthBytes = aesPseudorandomFunction.getKeylength() / 8;
        this.keyLengthBytes = preimageLengthBytes * factor;
    }

    @Override
    public ByteArrayImpl generateKey() {
        return ByteArrayImpl.fromRandom(keyLengthBytes);
    }

    @Override
    public ByteArrayImpl evaluate(PrfKey k, PrfPreimage x) {
        if (((ByteArrayImpl) k).length() != keyLengthBytes)
            throw new IllegalArgumentException("key k in the AES PRF has invalid length");
        if (((ByteArrayImpl) x).length() != preimageLengthBytes)
            throw new IllegalArgumentException("preimage x in the AES PRF has invalid length");

        ByteArrayImpl result = new ByteArrayImpl(new byte[0]);
        for (int i = 0; i < factor; i++) {
            ByteArrayImpl ki = ((ByteArrayImpl) k).substring(i * preimageLengthBytes, preimageLengthBytes);
            byte[] bytesToAppend = ((ByteArrayImpl) aesPseudorandomFunction.evaluate(ki, x)).getData();
            result = result.append(new ByteArrayImpl(bytesToAppend));
        }
        return result;
    }


    @Override
    public ByteArrayImpl restoreKey(Representation repr) {
        return new ByteArrayImpl(repr);
    }

    @Override
    public ByteArrayImpl restorePreimage(Representation repr) {
        return new ByteArrayImpl(repr);
    }

    @Override
    public ByteArrayImpl restoreImage(Representation repr) {
        return new ByteArrayImpl(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public int getPreimageLengthBytes() {
        return preimageLengthBytes;
    }

    public int getKeyLengthBytes() {
        return keyLengthBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongAesPseudoRandomFunction that = (LongAesPseudoRandomFunction) o;
        return factor.equals(that.factor) && Objects.equals(aesPseudorandomFunction, that.aesPseudorandomFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aesPseudorandomFunction, factor);
    }
}
