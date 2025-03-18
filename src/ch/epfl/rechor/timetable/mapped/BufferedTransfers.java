package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;

/**
 * Implementation of Transfers interface that provides access to transfer data
 * represented in a flattened format.
 * <p>
 * Each transfer is represented by three fields:
 * <ul>
 *   <li>DEP_STATION_ID (U16): Index of the departure station</li>
 *   <li>ARR_STATION_ID (U16): Index of the arrival station</li>
 *   <li>TRANSFER_MINUTES (U8): Transfer time in minutes</li>
 * </ul>
 * <p>
 * The transfers are organized so that all transfers arriving at the same station
 * are consecutive in the buffer.
 */
public final class BufferedTransfers implements Transfers {
    // Field indices constants
    private static final int DEP_STATION_ID = 0;
    private static final int ARR_STATION_ID = 1;
    private static final int TRANSFER_MINUTES = 2;

    // Structure definition for transfers
    private static final Structure STRUCTURE = new Structure(
        field(DEP_STATION_ID, U16),
        field(ARR_STATION_ID, U16),
        field(TRANSFER_MINUTES, U8)
    );

    private final StructuredBuffer buffer;
    private final int[] stationArrivingTransfers;

    /**
     * Constructs a BufferedTransfers instance to access flattened transfer data.
     *
     * @param buffer The byte buffer containing the transfer data
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);

        // Determine the number of stations
        int numStations = 0;
        for (int i = 0; i < this.buffer.size(); i++) {
            int stationId = this.buffer.getU16(ARR_STATION_ID, i);
            numStations = Math.max(numStations, stationId + 1);
        }

        // Create arriving transfers array
        this.stationArrivingTransfers = new int[numStations];

        // Calculate the packed ranges for each station
        int currentStationId = -1;
        int startIndex = 0;

        for (int i = 0; i < this.buffer.size(); i++) {
            int stationId = this.buffer.getU16(ARR_STATION_ID, i);

            if (currentStationId != stationId) {
                // Complete the previous station's range
                if (currentStationId >= 0) {
                    stationArrivingTransfers[currentStationId] = PackedRange.pack(startIndex, i);
                }

                // Start a new range for the current station
                currentStationId = stationId;
                startIndex = i;
            }
        }

        // Complete the last station's range
        if (currentStationId >= 0) {
            stationArrivingTransfers[currentStationId] = PackedRange.pack(startIndex, this.buffer.size());
        }
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
        return buffer.getU8(TRANSFER_MINUTES, id);
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
        if (stationId < 0 || stationId >= stationArrivingTransfers.length) {
            throw new IndexOutOfBoundsException("Invalid station ID: " + stationId);
        }
        return stationArrivingTransfers[stationId];
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
        if (depStationId < 0 || depStationId >= stationArrivingTransfers.length ||
            arrStationId < 0 || arrStationId >= stationArrivingTransfers.length) {
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