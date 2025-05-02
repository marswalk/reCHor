package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for loading and caching vehicle icons.
 * Icons are loaded from resources when first requested and then cached for future use.
 */
public final class VehicleIcons {

    private static final Map<Vehicle, Image> ICONS = new EnumMap<>(Vehicle.class);

    // Private constructor to prevent instantiation
    private VehicleIcons() {}

    /**
     * Returns the icon associated with the given vehicle type.
     * Icons are loaded from resources the first time they are requested, then cached.
     *
     * @param vehicle the vehicle type
     * @return the icon image for the given vehicle type
     * @throws NullPointerException if vehicle is null
     * @throws IllegalArgumentException if the icon cannot be found in resources
     */
    public static Image iconFor(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);
        return ICONS.computeIfAbsent(vehicle, VehicleIcons::loadIcon);
    }

    private static Image loadIcon(Vehicle vehicle) {
        String resourcePath = "/icons/" + vehicle.name().toLowerCase() + ".png";
        InputStream is = VehicleIcons.class.getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IllegalArgumentException("Icon not found for vehicle: " + vehicle);
        }

        return new Image(is);
    }
}