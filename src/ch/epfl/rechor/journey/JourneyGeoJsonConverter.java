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
 * Converts journeys to GeoJSON format for visualization on maps.
 */
public final class JourneyGeoJsonConverter {

    // Private constructor to prevent instantiation
    private JourneyGeoJsonConverter() {}

    /**
     * Converts a journey to a GeoJSON LineString representation.
     * The GeoJSON will include all stops in the journey with coordinates
     * rounded to 5 decimal places.
     *
     * @param journey the journey to convert
     * @return a JSON object representing the journey in GeoJSON format
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
     * Adds a stop's coordinates to the list if they haven't been added before.
     */
    private static void addCoordinates(Stop stop, List<Json> coordinates, Set<String> addedCoords) {
        // Round to 5 decimal places
        double lon = Math.round(stop.longitude() * 100000) / 100000.0;
        double lat = Math.round(stop.latitude() * 100000) / 100000.0;

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