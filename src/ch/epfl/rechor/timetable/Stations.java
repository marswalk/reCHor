package ch.epfl.rechor.timetable;

/**
 * Represents indexed stations in a timetable
 */
public interface Stations extends Indexed {
    /**
     * Returns the station name for the given index
     * @param id station index
     * @return station name
     * @throws IndexOutOfBoundsException for invalid indexes i.e. less than 0 or greater than or equal to the size returned by size().
     */
    String name(int id);

    /**
     * Returns the longitude in degrees for the given station index
     * @param id station index
     * @return longitude value
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    double longitude(int id);

    /**
     * Returns the latitude in degrees for the given station index
     * @param id station index
     * @return latitude value
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    double latitude(int id);
}