package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.nio.ByteBuffer;

/**
 * A hash function with variable output length.
 * <p>
 * This class uses an "inner" hash function and applies some tricks to get the
 * output length to what is desired.
 * <p>
 * If the inner hash function is modeled as a random oracle, the resulting hash function
 * is also a random oracle.
 * If the inner hash function is collision resistant and the desired outputLength is larger than
 * the inner hash function's output length, then the resulting hash function is also collision resistant.
 */
public class VariableOutputLengthHashFunction implements HashFunction, StandaloneRepresentable {

    /**
     * The base hash function to use
     */
    @Represented
    private HashFunction innerFunction;

    /**
     * Desired output length of this hash function in bytes
     */
    @Represented
    private int outputLength;

    /**
     * Instantiates a HashFunction with SHA256 as the base algorithm.
     *
     * @param length desired output length of this hash function in byte
     */
    public VariableOutputLengthHashFunction(int outputLength) {
        this(new SHA256HashFunction(), outputLength);
    }

    public HashFunction getInnerHashFunction() {
        return innerFunction;
    }

    /**
     * Instantiates a HashFunction with with a supplied base algorithm.
     *
     * @param hashFunction a base hash function
     * @param length       desired output length of this hash function in byte
     */
    public VariableOutputLengthHashFunction(HashFunction hashFunction, int outputLength) {
        innerFunction = hashFunction;
        this.outputLength = outputLength;
        if (outputLength <= 0)
            throw new IllegalArgumentException("Output length should be positive, but is " + outputLength);
    }

    /**
     * Reconstructs the hash function from its representation
     */
    public VariableOutputLengthHashFunction(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
    }

    @Override
    public byte[] hash(byte[] x) {
        // Construction: Use y = innerFunction(0||x) as a starting value. Note that the length of y is constant.
        // The hash value of x is innerFunction(1 || y) || innerFunction(2 || y) || ... (the resulting byte sequence is truncated to fit the desired byte number)
        // This preserves random-oracle (this uses the trick that for a random oracle h, x -> (0 || x) is a random oracle and x -> (1 || x) is a random oracle, etc.
        // This also preserves collision resistance (if not too much is truncated): given a collision (x,x'), either innerFunction(0||x) = innerFunction(0||x') or innerFunction(1 || innerFunction(0||x)) = innerFunction(1 || innerFunction(0||x')). We have found a collision in both cases.

        byte[] result = new byte[outputLength];
        int bytesFilled = 0;
        byte[] y = innerFunction.hash(prependInt(0, x));
        int c = 1; //counter for the innerFunction(c || y) segments
        while (bytesFilled < result.length) {
            byte[] hash = innerFunction.hash(prependInt(c++, y)); //innerFunction(c || y)
            for (int i = 0; i < hash.length && bytesFilled < result.length; i++) // copy hash into result array
                result[bytesFilled++] = hash[i];
        }

        return result;
    }

    /**
     * Given c and value, computes c || value
     */
    private byte[] prependInt(int c, byte[] value) {
        byte[] result = new byte[Integer.BYTES + value.length];
        ByteBuffer b = ByteBuffer.wrap(result);
        b.putInt(c);
        b.put(value);

        return result;
    }

    @Override
    public int getOutputLength() {
        return outputLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((innerFunction == null) ? 0 : innerFunction.hashCode());
        result = prime * result + outputLength;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableOutputLengthHashFunction other = (VariableOutputLengthHashFunction) obj;
        if (innerFunction == null) {
            if (other.innerFunction != null)
                return false;
        } else if (!innerFunction.equals(other.innerFunction))
            return false;
        if (outputLength != other.outputLength)
            return false;
        return true;
    }

}
