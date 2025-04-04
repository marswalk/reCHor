package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.field;

/**
 * Implementation of StationAliases interface that provides access to alternative
 * station names represented in a flattened format.
 * <p>
 * Each alternative name is represented by two fields:
 * <ul>
 *   <li>ALIAS_ID (U16): Index of the alternative name in the string table</li>
 *   <li>STATION_NAME_ID (U16): Index of the original station name in the string table</li>
 * </ul>
 * <p>
 * This allows mapping from alternative names (like "Losanna") to canonical names
 * (like "Lausanne") for station searching functionality.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class BufferedStationAliases implements StationAliases {
    // Field indices constants
    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;

    // Structure definition for station aliases
    private static final Structure STRUCTURE = new Structure(
            field(ALIAS_ID, U16),
            field(STATION_NAME_ID, U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedStationAliases instance to access flattened station alias data.
     *
     * @param stringTable The table of strings referenced by the flattened data
     * @param buffer      The byte buffer containing the flattened data
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Returns the number of station aliases in the table.
     *
     * @return the number of station aliases
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the alternative name for the station alias with the given ID.
     *
     * @param id the station alias ID
     * @return the alternative name (e.g., "Losanna")
     */
    @Override
    public String alias(int id) {
        int aliasIndex = buffer.getU16(ALIAS_ID, id);
        return stringTable.get(aliasIndex);
    }

    /**
     * Returns the canonical station name corresponding to the alias with the given ID.
     *
     * @param id the station alias ID
     * @return the canonical station name (e.g., "Lausanne")
     */
    @Override
    public String stationName(int id) {
        int stationNameIndex = buffer.getU16(STATION_NAME_ID, id);
        return stringTable.get(stationNameIndex);
    }
}