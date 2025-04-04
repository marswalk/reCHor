package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.field;

/**
 * Implementation of Trips interface that provides access to trip data
 * represented in a flattened format.
 * <p>
 * Each trip is represented by two fields:
 * <ul>
 *   <li>ROUTE_ID (U16): Index of the route for this trip</li>
 *   <li>DESTINATION_ID (U16): Index of the final destination name in the string table</li>
 * </ul>
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class BufferedTrips implements Trips {
    // Field indices constants
    private static final int ROUTE_ID = 0;
    private static final int DESTINATION_ID = 1;

    // Structure definition for trips
    private static final Structure STRUCTURE = new Structure(
            field(ROUTE_ID, U16),
            field(DESTINATION_ID, U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedTrips instance to access flattened trip data.
     *
     * @param stringTable The table of strings referenced by the flattened data
     * @param buffer      The byte buffer containing the flattened data
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Returns the number of trips in the table.
     *
     * @return the number of trips
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the route ID for the specified trip.
     *
     * @param id trip index
     * @return route ID for this trip
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int routeId(int id) {
        return buffer.getU16(ROUTE_ID, id);
    }

    /**
     * Returns the final destination name for the specified trip.
     *
     * @param id trip index
     * @return final destination name for this trip
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public String destination(int id) {
        int destinationIndex = buffer.getU16(DESTINATION_ID, id);
        return stringTable.get(destinationIndex);
    }
}