package de.upb.crypto.math.serialization.util;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.converter.JSONConverter;

/**
 * Class that allows to pretty-print JSON strings / Representations.
 */
public abstract class JSONPrettyPrinter {
    private static final JSONConverter converter = new JSONConverter();

    /**
     * Creates a nicely formatted string from the given representation.
     * @param repr the representation to pretty-print
     * @return a formatted string for printing
     */
    public static String prettyPrint(Representation repr) {
        return prettyPrintJson(converter.serialize(repr));
    }

    /**
     * Creates a nicely formatted string from the given JSON string.
     * @param json the json to pretty-print
     * @return a formatted string for printing
     */
    public static String prettyPrintJson(String json) {
        int indent = 0;
        boolean inString = false;
        boolean ignoreNextChar = false;

        StringBuilder builder = new StringBuilder();
        for (char c : json.toCharArray()) {
            if (ignoreNextChar) {
                ignoreNextChar = false;
                builder.append(c);
                continue;
            }

            if (c == '\\') {
                ignoreNextChar = true;
                builder.append(c);
            } else if (c == '"') {
                inString = !inString;
                builder.append(c);
            } else if (!inString && (c == '{' || c == '[')) {
                indent++;
                builder.append(c);
                writeNewlineAndIndent(indent, builder);
            } else if (!inString && (c == '}' || c == ']')) {
                indent--;
                writeNewlineAndIndent(indent, builder);
                builder.append(c);
            } else if (!inString && (c == ',')) {
                builder.append(c);
                writeNewlineAndIndent(indent, builder);
            } else
                builder.append(c);
        }

        return builder.toString();
    }

    private static void writeNewlineAndIndent(int indent, StringBuilder builder) {
        builder.append("\n");
        for (int i = 0; i < indent; i++)
            builder.append("   ");
    }
}
