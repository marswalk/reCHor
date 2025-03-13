package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;

/**
 * Represents a structured buffer for accessing flattened data using a predefined structure.
 */
public final class StructuredBuffer {
    private final Structure structure;
    private final ByteBuffer buffer;

    /**
     * Constructs a structured buffer.
     *
     * @param structure The data structure description
     * @param buffer The byte buffer containing flattened data
     * @throws IllegalArgumentException if buffer size isn't a multiple of structure size
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        if (buffer.capacity() % structure.totalSize() != 0)
            throw new IllegalArgumentException("Buffer size must be multiple of structure size");

        this.structure = structure;
        this.buffer = buffer;
    }

    /**
     * Returns the number of elements in the buffer.
     *
     * @return Number of complete elements
     */
    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    /**
     * Gets an unsigned 8-bit value from the buffer.
     *
     * @param fieldIndex Field index in the structure
     * @param elementIndex Element index in the buffer
     * @return Unsigned value (0-255)
     */
    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Byte.toUnsignedInt(buffer.get(offset));
    }

    /**
     * Gets an unsigned 16-bit value from the buffer.
     *
     * @param fieldIndex Field index in the structure
     * @param elementIndex Element index in the buffer
     * @return Unsigned value (0-65535)
     */
    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    /**
     * Gets a signed 32-bit value from the buffer.
     *
     * @param fieldIndex Field index in the structure
     * @param elementIndex Element index in the buffer
     * @return Signed 32-bit integer
     */
    public int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }
}
