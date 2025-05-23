package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

/**
 * Represents a JSON document with four possible types: array, object, string, or number.
 * This interface is sealed and implemented by specific record types for each JSON data type.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public sealed interface Json {

    /**
     * Represents a JSON array containing a list of JSON values.
     *
     * @param elements the JSON elements in the array. Each element is of type {@link Json}.
     */
    record JArray(List<Json> elements) implements Json {
        /**
         * Returns the JSON string representation of this array.
         *
         * @return a JSON-formatted string representing the array.
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < elements.size(); i++) {
                builder.append(elements.get(i).toString());
                if (i < elements.size() - 1) {
                    builder.append(",");
                }
            }
            return builder.append("]").toString();
        }
    }

    /**
     * Represents a JSON object containing key-value pairs where keys are strings
     * and values are JSON values.
     *
     * @param attributes the key-value pairs in the JSON object. Keys are strings, and values are of type {@link Json}.
     */
    record JObject(Map<String, Json> attributes) implements Json {
        /**
         * Returns the JSON string representation of this object.
         *
         * @return a JSON-formatted string representing the object.
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Json> entry : attributes.entrySet()) {
                builder.append("\"")
                       .append(entry.getKey())
                       .append("\":")
                       .append(entry.getValue().toString());
                if (i < attributes.size() - 1) {
                    builder.append(",");
                }
                i++;
            }
            return builder.append("}").toString();
        }
    }

    /**
     * Represents a JSON string.
     *
     * @param value the string value. This value is stored as a standard Java {@link String}.
     */
    record JString(String value) implements Json {
        /**
         * Returns the JSON string representation of this string.
         *
         * @return a JSON-formatted string with proper escaping.
         */
        @Override
        public String toString() {
            return "\"" + value + "\"";
        }
    }

    /**
     * Represents a JSON number.
     *
     * @param value the numeric value. This value is stored as a {@code double}.
     */
    record JNumber(double value) implements Json {
        /**
         * Returns the JSON string representation of this number.
         * If the number is an integer, it is represented without a decimal point.
         *
         * @return a JSON-formatted string representing the number.
         */
        @Override
        public String toString() {
            // Handle integer values without decimal point
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                return Long.toString((long) value);
            }
            return Double.toString(value);
        }
    }
}