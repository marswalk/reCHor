package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.field;

/**
 * Implementation of Platforms interface that provides access to platforms/tracks
 * represented in a flattened format.
 * <p>
 * Each platform is represented by two fields:
 * <ul>
 *   <li>NAME_ID (U16): Index of the platform name in the string table</li>
 *   <li>STATION_ID (U16): Index of the parent station in the station table</li>
 * </ul>
 * <p>
 * This allows mapping platforms (like "1", "70", "1AB") to their respective stations.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class BufferedPlatforms implements Platforms {
    // Field indices constants
    private static final int NAME_ID = 0;
    private static final int STATION_ID = 1;

    // Structure definition for platforms
    private static final Structure STRUCTURE = new Structure(
            field(NAME_ID, U16),
            field(STATION_ID, U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedPlatforms instance to access flattened platform data.
     *
     * @param stringTable The table of strings referenced by the flattened data
     * @param buffer      The byte buffer containing the flattened data
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Returns the number of platforms in the table.
     *
     * @return the number of platforms
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the name of the platform with the given ID.
     *
     * @param id the platform ID
     * @return the platform name (e.g., "1", "70", "1AB")
     */
    @Override
    public String name(int id) {
        int nameIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * Returns the ID of the parent station for the platform with the given ID.
     *
     * @param id the platform ID
     * @return the ID of the parent station
     */
    @Override
    public int stationId(int id) {
        return buffer.getU16(STATION_ID, id);
    }
}