package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * Represents indexed public transport routes/lines
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface Routes extends Indexed {
    /**
     * Gets the vehicle type for the specified route
     *
     * @param id route index
     * @return vehicle type enum
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    Vehicle vehicle(int id);

    /**
     * Gets the name of the line
     *
     * @param id route index
     * @return route name (e.g., "IR 15")
     * @throws IndexOutOfBoundsException for invalid indexes
     */
    String name(int id);
}