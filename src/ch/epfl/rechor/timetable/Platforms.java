package ch.epfl.rechor.timetable;

/**
 * Represents indexed platforms/tracks
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface Platforms extends Indexed {
    /**
     * Returns the platform name for the given index
     *
     * @param id platform index
     * @return platform name (eg 70 or A) (may be empty)
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    String name(int id);

    /**
     * Returns the station index containing this platform
     *
     * @param id platform index
     * @return parent station index
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    int stationId(int id);
}
