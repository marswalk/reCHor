package ch.epfl.rechor.gui;

import javafx.scene.image.Image;
import java.util.EnumMap;
import java.util.Map;
import ch.epfl.rechor.journey.Vehicle;

/**
 * Utility class for managing vehicle icons in the GUI.
 * <p>
 * This class provides access to icons representing different types of vehicles.
 * The icons are loaded once and cached to optimize memory usage and performance.
 * </p>
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class VehicleIcons {
    private static final Map<Vehicle, Image> ICONS = new EnumMap<>(Vehicle.class);

    private VehicleIcons() {
    }

    /**
     * Returns the icon associated with the specified vehicle type.
     * <p>
     * If the icon for the given vehicle type has not been loaded yet, it is
     * loaded and cached for future use. The same instance of the icon is
     * returned for subsequent calls with the same vehicle type.
     * </p>
     *
     * @param vehicle the type of vehicle for which the icon is requested
     * @return the JavaFX {@link Image} representing the vehicle type
     * @throws NullPointerException if the vehicle is null
     */
    public static Image iconFor(Vehicle vehicle) {
        return ICONS.computeIfAbsent(vehicle, v -> new Image(v.name() + ".png"));
    }
}