package de.upb.crypto.math.interfaces.hash;

/**
 * A EscapingByteAccumulator A wraps some ByteAccumulator B.
 * On input x, A writes x.replace(SEPARATOR_BYTE with (SEPARATOR_BYTE concat SEPARATOR_BYTE)) to B.
 * <p>
 * This allows recursive calls for lists, i.e. one would use the following template:
 * <p>
 * for each item i in list {
 * i.appendAccumulator(new EscapingByteAccumulator(acc));
 * acc.appendSeparator();
 * }
 * This ensures that the only unescaped separator bytes in acc are the ones
 * separating list items (all other separator bytes written by the items i are
 * escaped)
 *
 * @author Jan
 */
public class EscapingByteAccumulator extends ByteAccumulator {
    protected ByteAccumulator acc;
    protected byte escapedSymbol;

    /**
     * Sets up an escaping byte accumulator that escapes the separator symbol
     */
    public EscapingByteAccumulator(ByteAccumulator acc) {
        this(acc, SEPARATOR);
    }

    /**
     * Sets up an escaping byte accumulator that escapes the given symbol
     */
    public EscapingByteAccumulator(ByteAccumulator acc, byte escapedSymbol) {
        this.acc = acc;
        this.escapedSymbol = escapedSymbol;
    }

    @Override
    public void append(byte[] bytes) {
        for (byte b : bytes) {
            if (Byte.compare(b, escapedSymbol) == 1) {
                acc.append(new byte[]{escapedSymbol});
                acc.append(new byte[]{escapedSymbol});
            } else {
                acc.append(new byte[]{b});
            }
        }
    }

    @Override
    public byte[] extractBytes() {
        return acc.extractBytes();
    }

}
