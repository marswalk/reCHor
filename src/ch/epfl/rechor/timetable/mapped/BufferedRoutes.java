package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;
import static ch.epfl.rechor.timetable.mapped.Structure.field;

/**
 * Implementation of Routes interface that provides access to route data
 * represented in a flattened format.
 * <p>
 * Each route is represented by two fields:
 * <ul>
 *   <li>NAME_ID (U16): Index of the route name in the string table</li>
 *   <li>KIND (U8): Vehicle type serving the route (0-6)</li>
 * </ul>
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class BufferedRoutes implements Routes {
    // Field indices constants
    private static final int NAME_ID = 0;
    private static final int KIND = 1;

    // Structure definition for routes
    private static final Structure STRUCTURE = new Structure(
            field(NAME_ID, U16),
            field(KIND, U8)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedRoutes instance to access flattened route data.
     *
     * @param stringTable The table of strings referenced by the flattened data
     * @param buffer      The byte buffer containing the flattened data
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Returns the number of routes in the table.
     *
     * @return the number of routes
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Gets the vehicle type for the specified route.
     *
     * @param id route index
     * @return vehicle type enum
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public Vehicle vehicle(int id) {
        int vehicleOrdinal = buffer.getU8(KIND, id);
        return Vehicle.values()[vehicleOrdinal];
    }

    /**
     * Gets the name of the line.
     *
     * @param id route index
     * @return route name (e.g., "IR 15")
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public String name(int id) {
        int nameIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }
}