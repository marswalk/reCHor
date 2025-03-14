package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;
import java.nio.ByteBuffer;
import java.util.List;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;

/**
 * Implementation of Stations interface that provides access to stations
 * represented in a flattened format.
 * <p>
 * Each station is represented by three fields:
 * <ul>
 *   <li>NAME_ID (U16): Index of the station name in the string table</li>
 *   <li>LON (S32): Longitude of the station in an internal unit (converted to degrees)</li>
 *   <li>LAT (S32): Latitude of the station in an internal unit (converted to degrees)</li>
 * </ul>
 * <p>
 * Geographic coordinates are stored as S32 integers in an anonymous unit equivalent
 * to 2^-24 degrees, which provides centimeter-level precision.
 */
public final class BufferedStations implements Stations {
    // Field indices constants
    private static final int NAME_ID = 0;
    private static final int LON = 1;
    private static final int LAT = 2;

    // Structure definition for stations
    private static final Structure STRUCTURE = new Structure(
        field(NAME_ID, U16),
        field(LON, S32),
        field(LAT, S32)
    );

    // Conversion factor for coordinates (2^-32)
    private static final double GEO_UNIT_TO_DEGREES = Math.scalb(360, -32);

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedStations instance to access flattened station data.
     *
     * @param stringTable The table of strings referenced by the flattened data
     * @param buffer The byte buffer containing the flattened data
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Returns the number of stations.
     *
     * @return the number of stations
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the name of the station with the given ID.
     *
     * @param id the station ID
     * @return the name of the station
     */
    @Override
    public String name(int id) {
        int nameIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * Returns the longitude of the station with the given ID.
     *
     * @param id the station ID
     * @return the longitude of the station in degrees
     */
    @Override
    public double longitude(int id) {
        return buffer.getS32(LON, id) * GEO_UNIT_TO_DEGREES;
    }

    /**
     * Returns the latitude of the station with the given ID.
     *
     * @param id the station ID
     * @return the latitude of the station in degrees
     */
    @Override
    public double latitude(int id) {
        return buffer.getS32(LAT, id) * GEO_UNIT_TO_DEGREES;
    }
}