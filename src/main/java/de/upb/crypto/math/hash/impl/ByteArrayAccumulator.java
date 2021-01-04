package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;

import java.io.ByteArrayOutputStream;

/**
 * A {@link ByteAccumulator} that outputs the exact bytes that were input to it, so f(x) = x.
 */
public class ByteArrayAccumulator extends ByteAccumulator {

    protected final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @Override
    public void append(byte[] escapedBytes) {
        buffer.write(escapedBytes, 0, escapedBytes.length);
    }

    @Override
    public byte[] extractBytes() {
        return buffer.toByteArray();
    }
}