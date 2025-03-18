package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.NoSuchElementException;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;

/**
 * Implementation of Transfers interface that provides access to transfer data
 * represented in a flattened format.
 * <p>
 * Each transfer is represented by two fields:
 * <ul>
 *   <li>DEP_STATION_ID (U16): Index of the departure station</li>
 *   <li>MINUTES (U8): Transfer time in minutes</li>
 * </ul>
 * <p>
 * Additionally, a separate buffer contains the packed intervals for transfers arriving at each station.
 */
public final class BufferedTransfers implements Transfers {
    // Field indices constants
    private static final int DEP_STATION_ID = 0;
    private static final int MINUTES = 1;

    // Structure definition for transfers
    private static final Structure STRUCTURE = new Structure(
        field(DEP_STATION_ID, U16),
        field(MINUTES, U8)
    );

    private final StructuredBuffer buffer;
    private final IntBuffer arrBuffer;
    private final int stations;

    /**
     * Constructs a BufferedTransfers instance to access flattened transfer data.
     *
     * @param buffer The byte buffer containing the transfer data
     * @param arrBuffer The buffer containing packed ranges for transfers arriving at each station
     * @param stations The number of stations in the timetable
     */
    public BufferedTransfers(ByteBuffer buffer, ByteBuffer arrBuffer, int stations) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
        this.arrBuffer = arrBuffer.asIntBuffer();
        this.stations = stations;
    }

    /**
     * Returns the number of transfers in the table.
     *
     * @return the number of transfers
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the departure station ID for the specified transfer.
     *
     * @param id transfer index
     * @return departure station ID
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int depStationId(int id) {
        return buffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * Returns the transfer time in minutes for the specified transfer.
     *
     * @param id transfer index
     * @return transfer duration in minutes
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int minutes(int id) {
        return buffer.getU8(MINUTES, id);
    }

    /**
     * Returns the packed range of transfer indices arriving at the given station.
     *
     * @param stationId arrival station ID
     * @return packed interval of transfer indices
     * @throws IndexOutOfBoundsException for invalid station ID
     */
    @Override
    public int arrivingAt(int stationId) {
        if (stationId < 0 || stationId >= stations) {
            throw new IndexOutOfBoundsException("Invalid station ID: " + stationId);
        }
        return arrBuffer.get(stationId);
    }

    /**
     * Returns the transfer duration in minutes between two stations.
     *
     * @param depStationId departure station ID
     * @param arrStationId arrival station ID
     * @return transfer duration in minutes between stations
     * @throws NoSuchElementException if no transfer exists
     * @throws IndexOutOfBoundsException for invalid station IDs
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        if (depStationId < 0 || depStationId >= stations ||
            arrStationId < 0 || arrStationId >= stations) {
            throw new IndexOutOfBoundsException("Invalid station ID");
        }

        int packed = arrivingAt(arrStationId);
        int start = PackedRange.startInclusive(packed);
        int end = PackedRange.endExclusive(packed);

        for (int i = start; i < end; i++) {
            if (depStationId == depStationId(i)) {
                return minutes(i);
            }
        }

        throw new NoSuchElementException(
            "No transfer from station " + depStationId + " to station " + arrStationId);
    }
}