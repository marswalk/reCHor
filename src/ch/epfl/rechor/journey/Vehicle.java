package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Les différents types de véhicules de transport public en Suisse.
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