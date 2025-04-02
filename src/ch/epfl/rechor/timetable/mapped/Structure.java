package ch.epfl.rechor.timetable.mapped;

/**
 * Facilitates the description of flattened data structures.
 * <p>
 * This class provides tools to define the structure of data stored in a flattened format,
 * where different types of data are stored in arrays with each array containing all values
 * of a given type. The structure defines the sequence and types of fields that make up
 * each element in the flattened data.
 * <p>
 * A structure is composed of fields, each with an index and a type (U8, U16, or S32).
 * The class calculates the byte offsets of each field and the total size of the structure,
 * which can be used to efficiently navigate flattened data arrays.
 * <p>
 * Example usage:
 * <pre>
 * int NAME_ID = 0;
 * int STATION_ID = 1;
 *
 * Structure STRUCTURE = new Structure(
 *     field(NAME_ID, U16),    // NAME_ID is the index of the platform name in the string table
 *     field(STATION_ID, U16));
 * </pre>
 */
public final class Structure {

    /**
     * Represents the three possible field types for flattened data.
     */
    public enum FieldType {
        /** 8 bits (1 byte) interpreted as an unsigned integer (0 to 255) */
        U8(1),

        /** 16 bits (2 bytes) interpreted as an unsigned integer (0 to 65535) */
        U16(2),

        /** 32 bits (4 bytes) interpreted as a signed integer (-2^31 to 2^31-1) */
        S32(4);

        private final int size;

        FieldType(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    /**
     * Represents a field with its index and type.
     */
    public record Field(int index, FieldType type) {
        /**
         * Creates a field with the given index and type.
         *
         * @param index the field index in the structure
         * @param type the field type
         * @throws NullPointerException if type is null
         */
        public Field {
            if (type == null)
                throw new NullPointerException();
        }
    }

    // Array storing the offset (in bytes) of each field in the structure
    private final int[] fieldOffsets;
    // Total size of the structure in bytes
    private final int totalSize;

    /**
     * Creates a field with the given index and type.
     *
     * @param index the field index
     * @param type the field type
     * @return a new Field instance
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }
    // the whole point of method field is so that you can omit new Field() when creating a new Field instance ?

    /**
     * Constructs a structure with the given fields.
     *
     * @param fields the fields composing the structure
     * @throws IllegalArgumentException if fields are not in order
     */
    public Structure(Field... fields) {
        // Check if fields are in the correct order
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].index() != i) {
                throw new IllegalArgumentException("Fields must be in order (field " +
                        i + " has index " + fields[i].index() + ")");
            }
        }

        // Calculate the offset of each field and the total size
        fieldOffsets = new int[fields.length];
        int currentOffset = 0;

        for (int i = 0; i < fields.length; i++) {
            fieldOffsets[i] = currentOffset;
            currentOffset += fields[i].type().size();
        }

        totalSize = currentOffset;
    }

    /**
     * Returns the total size in bytes of the structure.
     *
     * @return the total size in bytes
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Returns the index of the first byte of a field in the byte array.
     *
     * @param fieldIndex the index of the field
     * @param elementIndex the index of the element
     * @return the byte offset in the array
     * @throws IndexOutOfBoundsException if fieldIndex is invalid
     */
    public int offset(int fieldIndex, int elementIndex) {
        return elementIndex * totalSize + fieldOffsets[fieldIndex];
    }
}
