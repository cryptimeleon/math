package org.cryptimeleon.math.prf.aes;

import org.cryptimeleon.math.misc.ByteArrayImpl;
import org.cryptimeleon.math.prf.PrfImage;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.prf.PrfPreimage;
import org.cryptimeleon.math.prf.PseudorandomFunction;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * AES as a pseudorandom function (permutation) \(f_k : \{0,1\}^l \rightarrow \{0,1\}^l\)
 * for \(k \in \{0,1\}^l\). Here, l is any valid AES keylength (e.g., 128, 256).
 * <p>
 * {@link PrfKey}, {@link PrfPreimage}, and  {@link PrfImage} are of type {@link ByteArrayImpl}.
 */
public class AesPseudorandomFunction implements PseudorandomFunction {
    @Represented
    protected Integer keylength; //length of keys in bit

    /**
     * Instantiates the PRF with a given AES key length.
     *
     * @param keylength a valid AES key length in bit (e.g., 128 or 256)
     */
    public AesPseudorandomFunction(int keylength) {
        this.keylength = keylength;
    }

    public AesPseudorandomFunction(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public PrfKey generateKey() {
        return ByteArrayImpl.fromRandom(keylength / 8);
    }

    @Override
    public PrfImage evaluate(PrfKey k, PrfPreimage x) {
        if (((ByteArrayImpl) k).length() != keylength / 8)
            throw new IllegalArgumentException("key k in the AES PRF has invalid length");
        if (((ByteArrayImpl) x).length() != keylength / 8)
            throw new IllegalArgumentException("preimage x in the AES PRF has invalid length");

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(((ByteArrayImpl) k).getData(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return new ByteArrayImpl(cipher.doFinal(((ByteArrayImpl) x).getData()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Input k to AES PRF must be of valid AES key length");
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Input x to AES PRF must be of valid AES key length");
        }
    }

    @Override
    public PrfKey restoreKey(Representation repr) {
        return new ByteArrayImpl(repr);
    }

    @Override
    public PrfPreimage restorePreimage(Representation repr) {
        return new ByteArrayImpl(repr);
    }

    @Override
    public PrfImage restoreImage(Representation repr) {
        return new ByteArrayImpl(repr);
    }

    @Override
    public int hashCode() {
        return keylength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AesPseudorandomFunction other = (AesPseudorandomFunction) o;
        return Objects.equals(keylength, other.keylength);
    }

    public Integer getKeylength() {
        return keylength;
    }
}
