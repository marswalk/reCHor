package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;
import static ch.epfl.rechor.timetable.mapped.Structure.field;

/**
 * Implementation of Transfers interface that provides access to transfer data
 * represented in a flattened format.
 * Transfers (changements) are trajets à pied between stations!
 * <p>
 * Each transfer is represented by three fields:
 * <ul>
 *   <li>DEP_STATION_ID (U16): Index of the departure station</li>
 *   <li>ARR_STATION_ID (U16): Index of the arrival station</li>
 *   <li>TRANSFER_MINUTES (U8): Duration of the transfer in minutes</li>
 * </ul>
 * <p>
 * The table of flattened transfers is ordered such that all transfers arriving at
 * the same station are consecutive. This property allows representing all transfers
 * arriving at a given station as an interval of transfer indices.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
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
    private final int[] arrivingAtTable;

    /**
     * Constructs a BufferedTransfers instance to access flattened transfer data.
     *
     * @param buffer The byte buffer containing the flattened data
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);

        // Create a table mapping station IDs to transfer intervals
        // First pass: find the maximum station ID to determine table size
        int maxStationId = -1;
        for (int i = 0; i < this.buffer.size(); i++) {
            int arrStationId = this.buffer.getU16(ARR_STATION_ID, i);
            maxStationId = Math.max(maxStationId, arrStationId);
        }

        // Initialize the table (max station ID + 1)
        arrivingAtTable = new int[maxStationId + 1];

        // Second pass: fill the table with packed intervals
        int currentStationId = -1;
        int startIndex = 0;

        for (int i = 0; i < this.buffer.size(); i++) {
            int arrStationId = this.buffer.getU16(ARR_STATION_ID, i);

            if (arrStationId != currentStationId) {
                // We've reached transfers for a new station
                if (currentStationId >= 0) {
                    // Store the packed interval for the previous station
                    arrivingAtTable[currentStationId] = PackedRange.pack(startIndex, i);
                }
                currentStationId = arrStationId;
                startIndex = i;
            }
        }

        // Store the packed interval for the last station
        if (currentStationId >= 0) {
            arrivingAtTable[currentStationId] = PackedRange.pack(startIndex, this.buffer.size());
        }
    }

    /**
     * Returns the number of transfers.
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
     * @return departure station ID for the transfer
     * @throws IndexOutOfBoundsException for invalid ID
     */
    @Override
    public int depStationId(int id) {
        return buffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * Returns the transfer duration in minutes for the specified transfer.
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
     * Returns a packed interval of transfer indices arriving at the given station.
     *
     * @param stationId arrival station ID
     * @return packed interval - according to PackedRange - of transfer indices arriving at given station
     */
    @Override
    public int arrivingAt(int stationId) {
        return arrivingAtTable[stationId];
    }

    /**
     * Returns the transfer duration in minutes between two stations.
     *
     * @param depStationId departure station ID
     * @param arrStationId arrival station ID
     * @return transfer duration in minutes between stations
     * @throws NoSuchElementException    if no transfer exists
     * @throws IndexOutOfBoundsException for invalid station IDs
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        // Get packed range of transfers arriving at the destination station
        int packedRange = arrivingAt(arrStationId);
        int startIndex = PackedRange.startInclusive(packedRange);
        int endIndex = PackedRange.endExclusive(packedRange);

        // Search for a transfer from the specified departure station
        for (int i = startIndex; i < endIndex; i++) {
            if (depStationId(i) == depStationId) {
                return minutes(i);
            }
        }

        throw new NoSuchElementException("No transfer exists from station " + depStationId +
                " to station " + arrStationId);
    }
}