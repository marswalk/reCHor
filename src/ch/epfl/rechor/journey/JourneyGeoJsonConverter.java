package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;
import ch.epfl.rechor.Json.JObject;
import ch.epfl.rechor.Json.JArray;
import ch.epfl.rechor.Json.JString;
import ch.epfl.rechor.Json.JNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for converting journeys into GeoJSON format for visualization on maps.
 * The GeoJSON format is used to represent the journey as a LineString.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class JourneyGeoJsonConverter {

    // Precision for rounding coordinates to 5 decimal places (about 1 meter)
    private static final int GEOJSON_COORDINATE_PRECISION = 100000;

    // Private constructor to prevent instantiation
    private JourneyGeoJsonConverter() {}

    /**
     * Converts a journey into a GeoJSON LineString representation.
     * The GeoJSON includes all stops in the journey, with coordinates rounded to 5 decimal places.
     *
     * @param journey the journey to convert. Must not be null.
     * @return a {@link Json} object representing the journey in GeoJSON format.
     */
    public static Json toGeoJson(Journey journey) {
        List<Json> coordinates = new ArrayList<>();
        Set<String> addedCoords = new HashSet<>();

        // Add departure stop
        addCoordinates(journey.depStop(), coordinates, addedCoords);

        // Process all legs
        for (Journey.Leg leg : journey.legs()) {
            // Add intermediate stops
            for (Journey.Leg.IntermediateStop intermediateStop : leg.intermediateStops()) {
                addCoordinates(intermediateStop.stop(), coordinates, addedCoords);
            }

            // Add arrival stop
            addCoordinates(leg.arrStop(), coordinates, addedCoords);
        }

        // Create the GeoJSON LineString object
        Map<String, Json> geoJson = Map.of(
                "type", new JString("LineString"),
                "coordinates", new JArray(coordinates)
        );

        return new JObject(geoJson);
    }

    /**
     * Adds a stop's coordinates to the GeoJSON coordinate list if they are not already included.
     *
     * @param stop the stop whose coordinates are to be added. Must not be null.
     * @param coordinates the list of coordinates to which the stop's coordinates will be added.
     * @param addedCoords a set of coordinate keys used to track duplicates.
     */
    private static void addCoordinates(Stop stop, List<Json> coordinates, Set<String> addedCoords) {
        // Round to 5 decimal places using the defined precision constant
        double lon = Math.round(stop.longitude() * GEOJSON_COORDINATE_PRECISION) / (double) GEOJSON_COORDINATE_PRECISION;
        double lat = Math.round(stop.latitude() * GEOJSON_COORDINATE_PRECISION) / (double) GEOJSON_COORDINATE_PRECISION;

        // Create a string key for the coordinates to check for duplicates
        String coordKey = lon + "," + lat;

        // Skip duplicate coordinates
        if (addedCoords.contains(coordKey)) {
            return;
        }

        // Mark these coordinates as added
        addedCoords.add(coordKey);

        // Create coordinate pair [lon, lat]
        List<Json> coordPair = List.of(
                new JNumber(lon),
                new JNumber(lat)
        );
        coordinates.add(new JArray(coordPair));
    }
}
