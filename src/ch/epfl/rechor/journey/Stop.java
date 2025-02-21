package ch.epfl.rechor.journey;

import java.util.Objects;

import ch.epfl.rechor.Preconditions;

/**
 * Un arrêt de transport public.
 *
 * @param name         The name of the stop (station name if it's a platform).
 * @param platformName The name of the platform or track (null if it's a station).
 * @param longitude    The longitude of the stop in degrees (-180 to 180).
 * @param latitude     The latitude of the stop in degrees (-90 to 90).
 * @throws NullPointerException     if the name is null.
 * @throws IllegalArgumentException if longitude is not in [-180, 180] or latitude is not in [-90, 90].
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Compact constructor that validates the input parameters.
     */
    public Stop {
        Objects.requireNonNull(name, "Haltestellenname cannot be null");
        Preconditions.checkArgument(longitude >= -180 && longitude <= 180);
        Preconditions.checkArgument(latitude >= -90 && latitude <= 90);
    }
}
