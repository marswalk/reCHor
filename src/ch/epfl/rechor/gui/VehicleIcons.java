package ch.epfl.rechor.gui;

import javafx.scene.image.Image;
import java.util.EnumMap;
import java.util.Map;
import ch.epfl.rechor.journey.Vehicle;

public final class VehicleIcons {
    private static final Map<Vehicle, Image> ICONS = new EnumMap<>(Vehicle.class);

    private VehicleIcons() {
        // Non-instantiable
    }

    /**
     * Returns the icon for the given vehicle type.
     *
     * @param vehicle the vehicle type
     * @return the corresponding icon
     */
    public static Image iconFor(Vehicle vehicle) {
        return ICONS.computeIfAbsent(vehicle, v -> new Image(v.name() + ".png"));
    }
}