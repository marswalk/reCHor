package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Journey extractor that converts optimization criteria from a profile into complete journeys.
 * <p>
 * This class is not instantiable and provides a single static method to extract journeys
 * from a profile for a specific departure station.
 */
public final class JourneyExtractor {

    // Private constructor to prevent instantiation
    private JourneyExtractor() {}

    /**
     * Extracts all optimal journeys from the given profile for the specified departure station.
     * <p>
     * The returned journeys are sorted first by departure time (ascending), then by arrival time (ascending).
     *
     * @param profile the profile containing Pareto frontiers for all stations
     * @param depStationId the departure station index
     * @return a list of all optimal journeys from the departure station, sorted by departure time then arrival time
     * @throws NullPointerException if the profile is null
     * @throws IndexOutOfBoundsException if the departure station ID is invalid
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
//        Objects.requireNonNull(profile, "Profile cannot be null");

        TimeTable timeTable = profile.timeTable();
        LocalDate date = profile.date();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        // Check if the departure station ID is valid
//        if (depStationId < 0 || depStationId >= timeTable.stations().size()) {
//            throw new IndexOutOfBoundsException("Invalid departure station ID: " + depStationId);
//        }

        List<Journey> journeys = new ArrayList<>();
        ParetoFront depStationFront = profile.forStation(depStationId);

        depStationFront.forEach((long criteria) -> {
            // Extract journey parameters
            int depMins = PackedCriteria.depMins(criteria);
            int arrMins = PackedCriteria.arrMins(criteria);
            int changes = PackedCriteria.changes(criteria);
            int payload = PackedCriteria.payload(criteria);

            // Extract first connection ID and stops to travel
            int firstConnId = Bits32_24_8.unpack24(payload); // Extract upper 24 bits
            int stopsToTravel = Bits32_24_8.unpack8(payload); // Extract lower 8 bits

            Journey journey = extractJourney(timeTable, date, profile, depStationId,
                    depMins, arrMins, changes, firstConnId, stopsToTravel);
            journeys.add(journey);
        });

        // Sort journeys by departure time, then arrival time
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extracts a complete journey based on the first connection and optimization criteria.
     */
    private static Journey extractJourney(TimeTable timeTable, LocalDate date, Profile profile,
                                         int depStationId, int depMins, int arrMins, int changes,
                                         int firstConnId, int stopsToTravel) {
        Stations stations = timeTable.stations();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        List<Journey.Leg> legs = new ArrayList<>();

        // Current position and criteria tracking
        int currentStationId = depStationId;
        int remainingChanges = changes;
        int targetArrMins = arrMins;

        // First connection details
        int connDepStopId = connections.depStopId(firstConnId);
        int depStopId = connDepStopId;
        int connTripId = connections.tripId(firstConnId);

        // If the journey doesn't start directly from the departure station, add a walking leg
        if (timeTable.stationId(connDepStopId) != depStationId) {
            // Add initial walking leg
            Stop depStop = createStop(timeTable, depStationId);
            Stop connDepStop = createStop(timeTable, connDepStopId);

            LocalDateTime depTime = LocalDateTime.of(date, LocalTime.of(depMins / 60, depMins % 60));
            LocalDateTime WalkToConnDepTime = depTime.plusMinutes(timeTable.transfers().minutesBetween(depStationId, timeTable.stationId(connDepStopId)));

            legs.add(new Journey.Leg.Foot(depStop, depTime, connDepStop, WalkToConnDepTime));
        }

        // Continue extracting journey segments until we reach the destination
        while (remainingChanges >= 0) {
            // Get the current connection
            int connId = firstConnId;
            int tripId = connTripId;

            // Find destination station after traveling 'stopsToTravel' stops
            int finalStopIndex = findFinalStopIndex(connections, connId, stopsToTravel);
            int arrStopId = connections.arrStopId(finalStopIndex);
            int arrStationId = timeTable.stationId(arrStopId);

            // Create transport leg
            List<Journey.Leg.IntermediateStop> intermediateStops = extractIntermediateStops(
                    timeTable, date, connections, connId, stopsToTravel);

            Stop depStop = createStop(timeTable, depStopId);
            Stop arrStop = createStop(timeTable, arrStopId);

            LocalDateTime depTime = LocalDateTime.of(date,
                    LocalTime.of(connections.depMins(connId) / 60, connections.depMins(connId) % 60));
            LocalDateTime arrTime = LocalDateTime.of(date,
                    LocalTime.of(connections.arrMins(finalStopIndex) / 60, connections.arrMins(finalStopIndex) % 60));

            String route = timeTable.routes().name(trips.routeId(tripId));
            String destination = trips.destination(tripId);
            Vehicle vehicle = timeTable.routes().vehicle(trips.routeId(tripId));

            legs.add(new Journey.Leg.Transport(
                    depStop, depTime, arrStop, arrTime, intermediateStops, vehicle, route, destination));

            // Check if we've reached the destination
            if (remainingChanges == 0) {
                // If we're not yet at the final destination, add a final walking leg
                if (arrStationId != profile.arrStationId()) {
                    // Add final walking leg
                    Stop finalDepStop = arrStop;
                    Stop finalArrStop = createStop(timeTable, profile.arrStationId());

                    LocalDateTime finalArrTime = arrTime.plusMinutes(timeTable.transfers().minutesBetween(arrStationId, profile.arrStationId()));

                    legs.add(new Journey.Leg.Foot(finalDepStop, arrTime, finalArrStop, finalArrTime));
                }
                break;
            }

            // Prepare for next leg
            remainingChanges--;
            currentStationId = arrStationId;

            // Find next transport leg from the current station
            ParetoFront nextStationFront = profile.forStation(currentStationId);
            long nextCriteria = nextStationFront.get(targetArrMins, remainingChanges);
            int nextPayload = PackedCriteria.payload(nextCriteria);

            int nextConnId = Bits32_24_8.unpack24(nextPayload);
            stopsToTravel = Bits32_24_8.unpack8(nextPayload);

            int nextDepStopId = connections.depStopId(nextConnId);

            // Always add a walking leg between the last arrival stop and the next departure stop
            Stop walkDepStop = arrStop;
            Stop walkArrStop = createStop(timeTable, nextDepStopId);

            LocalDateTime walkDepTime = arrTime;
            LocalDateTime walkArrTime = walkDepTime.plusMinutes(timeTable.transfers().minutesBetween(timeTable.stationId(arrStopId), timeTable.stationId(nextDepStopId)));

            legs.add(new Journey.Leg.Foot(walkDepStop, walkDepTime, walkArrStop, walkArrTime));


            // Update for next iteration
            firstConnId = nextConnId;
            connTripId = connections.tripId(nextConnId);
            depStopId = nextDepStopId;
        }

        return new Journey(legs);
    }

    /**
     * Creates a Stop object representing the stop with the given ID.
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        Stations stations = timeTable.stations();
        int stationId = timeTable.stationId(stopId);
        String name = stations.name(stationId);
        String platformName = timeTable.platformName(stopId);
        double longitude = stations.longitude(stationId);
        double latitude = stations.latitude(stationId);

        return new Stop(name, platformName, longitude, latitude);
    }

    /**
     * Finds the index of the final stop after traveling a specified number of stops.
     */
    private static int findFinalStopIndex(Connections connections, int startConnId, int stopsToTravel) {
        int currentConnId = startConnId;
        for (int i = 0; i < stopsToTravel; i++) {
            currentConnId = connections.nextConnectionId(currentConnId);
        }
        return currentConnId;
    }

    /**
     * Extracts intermediate stops for a transport leg.
     */
    private static List<Journey.Leg.IntermediateStop> extractIntermediateStops(
            TimeTable timeTable, LocalDate date, Connections connections, int startConnId, int stopsToTravel) {

        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        int currentConnId = startConnId;

        // For each intermediate stop
        for (int i = 0; i < stopsToTravel; i++) {
            int nextConnId = connections.nextConnectionId(currentConnId);

            // Create intermediate stop data
            int arrStopId = connections.arrStopId(currentConnId);
            int depStopId = connections.depStopId(nextConnId);

            Stop stop = createStop(timeTable, arrStopId);

            LocalDateTime arrTime = LocalDateTime.of(date,
                    LocalTime.of(connections.arrMins(currentConnId) / 60, connections.arrMins(currentConnId) % 60));
            LocalDateTime depTime = LocalDateTime.of(date,
                    LocalTime.of(connections.depMins(nextConnId) / 60, connections.depMins(nextConnId) % 60));

            intermediateStops.add(new Journey.Leg.IntermediateStop(stop, arrTime, depTime));

            currentConnId = nextConnId;
        }

        return intermediateStops;
    }
}