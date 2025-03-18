package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;

/**
 * Implementation of Connections interface that provides access to connection data
 * represented in a flattened format.
 * <p>
 * Each connection is represented by five fields:
 * <ul>
 *   <li>DEP_STOP_ID (U16): Index of the departure stop</li>
 *   <li>DEP_MINUTES (U16): Departure time in minutes after midnight</li>
 *   <li>ARR_STOP_ID (U16): Index of the arrival stop</li>
 *   <li>ARR_MINUTES (U16): Arrival time in minutes after midnight</li>
 *   <li>TRIP_POS_ID (S32): Packed value containing trip index (24 high bits) and position (8 low bits)</li>
 * </ul>
 * <p>
 * Additionally, a separate buffer contains the next connection ID for each connection.
 */
public final class BufferedConnections implements Connections {
    // Field indices constants
    private static final int DEP_STOP_ID = 0;
    private static final int DEP_MINUTES = 1;
    private static final int ARR_STOP_ID = 2;
    private static final int ARR_MINUTES = 3;
    private static final int TRIP_POS_ID = 4;

    // Constants for bit manipulation
    private static final int POSITION_BITS = 8;
    private static final int POSITION_MASK = (1 << POSITION_BITS) - 1;

    // Structure definition for connections
    private static final Structure STRUCTURE = new Structure(
        field(DEP_STOP_ID, U16),
        field(DEP_MINUTES, U16),
        field(ARR_STOP_ID, U16),
        field(ARR_MINUTES, U16),
        field(TRIP_POS_ID, S32)
    );

    private final StructuredBuffer buffer;
    private final IntBuffer succBuffer;

    /**
     * Constructs a BufferedConnections instance to access flattened connection data.
     *
     * @param buffer The byte buffer containing the connection data
     * @param succBuffer The byte buffer containing the next connection IDs
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
        this.succBuffer = succBuffer.asIntBuffer();
    }

    /**
     * Returns the number of connections in the table.
     *
     * @return the number of connections
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the departure stop ID for the specified connection.
     *
     * @param id connection index
     * @return departure stop ID
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int depStopId(int id) {
        return buffer.getU16(DEP_STOP_ID, id);
    }

    /**
     * Returns the departure time in minutes after midnight for the specified connection.
     *
     * @param id connection index
     * @return departure time in minutes
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int depMins(int id) {
        return buffer.getU16(DEP_MINUTES, id);
    }

    /**
     * Returns the arrival stop ID for the specified connection.
     *
     * @param id connection index
     * @return arrival stop ID
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int arrStopId(int id) {
        return buffer.getU16(ARR_STOP_ID, id);
    }

    /**
     * Returns the arrival time in minutes after midnight for the specified connection.
     *
     * @param id connection index
     * @return arrival time in minutes
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int arrMins(int id) {
        return buffer.getU16(ARR_MINUTES, id);
    }

    /**
     * Returns the trip ID for the specified connection.
     *
     * @param id connection index
     * @return trip ID
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int tripId(int id) {
        return buffer.getS32(TRIP_POS_ID, id) >>> POSITION_BITS;
    }

    /**
     * Returns the position in the trip for the specified connection.
     *
     * @param id connection index
     * @return position in trip (0 for first connection)
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int tripPos(int id) {
        return buffer.getS32(TRIP_POS_ID, id) & POSITION_MASK;
    }

    /**
     * Returns the next connection ID in the trip for the specified connection.
     * For the last connection in a trip, returns the first connection of the trip.
     *
     * @param id connection index
     * @return next connection ID in the trip
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int nextConnectionId(int id) {
        return succBuffer.get(id);
    }
}