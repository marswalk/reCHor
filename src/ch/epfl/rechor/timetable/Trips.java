package ch.epfl.rechor.timetable;

/**
 * Represents indexed public transport trips/runs
 */
public interface Trips extends Indexed {

    /**
     * @param id trip index
     * @return route ID for this trip (index of the line i.e. for IR 15)
     * @throws IndexOutOfBoundsException for invalid ID
     */
    int routeId(int id);

    /**
     * @param id trip index
     * @return final destination name for this trip
     * @throws IndexOutOfBoundsException for invalid ID
     */
    String destination(int id);
}
