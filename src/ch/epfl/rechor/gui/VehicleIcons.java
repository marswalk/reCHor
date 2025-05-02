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

//    private static Image loadIcon(Vehicle vehicle) {
//        System.out.println("Loading icon for vehicle: " + vehicle);
//        String resourcePath = "resources/" + vehicle.name() + ".png";
//        System.out.println("Resource path: " + resourcePath);
//        InputStream is = VehicleIcons.class.getResourceAsStream(resourcePath);
//        System.out.println("InputStream: " + is);
//
//        if (is == null) {
//            throw new IllegalArgumentException("Icon not found for vehicle: " + vehicle);
//        }
//
//        return new Image(is);
//    }
    private static Image loadIcon(Vehicle vehicle) {
        System.out.println("Loading icon for vehicle: " + vehicle);
        try {
            // Use file URL - specify the path where images are located
            String path = "file:///C:/Users/T/Documents/GitHub/reCHor/resources/" + vehicle.name() + ".png";
            System.out.println("Using path: " + path);
            return new Image(path);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e);
            throw new IllegalArgumentException("Icon not found for vehicle: " + vehicle);
        }
    }
}