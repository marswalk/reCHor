package ch.epfl.rechor.timetable;

/**
 * Represents indexed connections between stops, ordered by decreasing departure time
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface Connections extends Indexed {

    /**
     * @param id connection index
     * @return departure stop ID (station or platform)
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int depStopId(int id);

    /**
     * @param id connection index
     * @return departure time in minutes since midnight
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int depMins(int id);

    /**
     * @param id connection index
     * @return arrival stop ID (station or platform)
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int arrStopId(int id);

    /**
     * @param id connection index
     * @return arrival time in minutes since midnight
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int arrMins(int id);

    /**
     * @param id connection index
     * @return trip ID containing this connection
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int tripId(int id);

    /**
     * @param id connection index
     * @return position in trip (0 for first connection)
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int tripPos(int id);

    /**
     * @param id connection index
     * @return next connection ID in the trip (wraps to first if last)
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int nextConnectionId(int id);
}