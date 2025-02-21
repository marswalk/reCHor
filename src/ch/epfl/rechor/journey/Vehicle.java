package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Enum of different Swiss public transport vehicle types.
 *
 * @author Guanting Wen (392412)
 *
 */
public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FUNICULAR;

    /**
     * Immutable list of all vehicle types in defined order.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());
}