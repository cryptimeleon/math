package org.cryptimeleon.math.serialization.converter;


import org.cryptimeleon.math.serialization.Representation;

/**
 * Allows converting between a {@code Representation} object and a pretty-printed JSON structure.
 * <p>
 * Note that the order of attributes in a JSON Object is meaningless.
 * However, this {@code Converter} guarantees a consistent order between calls,
 * making the {@code Representation -> String} relation left-unique (i.e. a well-defined mapping).
 * This allows this {@code Converter} to be used for, e.g,. {@code HashRepresentationIntoStructure},
 * and similar tasks that require a unique and consistent output.
 */
public class JSONPrettyConverter extends JSONConverter {

    @Override
    public String serialize(Representation r) {
        return prettyPrintJson(super.serialize(r));
    }

    /**
     * Creates a nicely formatted string from the given JSON string.
     * @param json the json to pretty-print
     * @return a formatted string for printing
     */
    public String prettyPrintJson(String json) {
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

    private void writeNewlineAndIndent(int indent, StringBuilder builder) {
        builder.append("\n");
        for (int i = 0; i < indent; i++)
            builder.append("   ");
    }
}
