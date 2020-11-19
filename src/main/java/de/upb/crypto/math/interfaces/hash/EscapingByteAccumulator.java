package de.upb.crypto.math.interfaces.hash;

/**
 * An {@code EscapingByteAccumulator} {@code A} wraps some {@code ByteAccumulator} {@code B} where a single escaped
 * symbol is replaced by two of those symbols when writing to the accumulator.
 * <p>
 * This allows recursive calls for lists, i.e. one would use the following template:
 * <pre>
 * for (Object i : list) {
 *     i.appendAccumulator(new EscapingByteAccumulator(acc));
 *     acc.appendSeparator();
 * }
 * </pre>
 * This ensures that the only unescaped separator bytes in {@code acc} are the ones
 * separating list items (all other separator bytes written by the items {@code i} are
 * escaped).
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
