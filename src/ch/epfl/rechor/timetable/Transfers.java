package ch.epfl.rechor.timetable;

import java.util.NoSuchElementException;

/**
 * Represents indexed transfer information between stations.
 * Transfers can only occur between stations (not platforms).
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface Transfers extends Indexed {

    /**
     * @param id transfer index
     * @return departure station ID for the transfer
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int depStationId(int id);

    /**
     * @param id transfer index
     * @return transfer duration in minutes
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int minutes(int id);

    /**
     * @param stationId arrival station ID
     * @return packed interval - according to PackedRange - of transfer indices arriving at given station
     * @throws IndexOutOfBoundsException for invalid station ID
     */
    int arrivingAt(int stationId);

    /**
     * @param depStationId departure station ID
     * @param arrStationId arrival station ID
     * @return transfer duration in minutes between stations
     * @throws NoSuchElementException    if no transfer exists
     * @throws IndexOutOfBoundsException for invalid station IDs
     */
    int minutesBetween(int depStationId, int arrStationId);
}