package ch.epfl.rechor.timetable;

/**
 * Represents station aliases (alternative names)
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface StationAliases extends Indexed {
    /**
     * Returns the alternative name for the given index
     *
     * @param id alias index
     * @return alternative name (example of Losanna for Lausanne_
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    String alias(int id);

    /**
     * Returns the official station name for the given alias index
     *
     * @param id alias index
     * @return corresponding station name
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    String stationName(int id);
}
