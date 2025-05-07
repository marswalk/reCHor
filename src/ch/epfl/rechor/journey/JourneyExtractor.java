package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Journey extractor that converts optimization criteria from a profile into complete journeys.
 * <p>
 * This class is not instantiable and provides a single static method to extract journeys
 * from a profile for a specific departure station.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class JourneyExtractor {

    // Private constructor to prevent instantiation
    private JourneyExtractor() {
    }

    /**
     * Extracts all optimal journeys from the given profile for the specified departure station.
     * <p>
     * The returned journeys are sorted first by departure time (ascending), then by arrival time (ascending).
     *
     * @param profile      the profile containing Pareto frontiers for all stations
     * @param depStationId the departure station index
     * @return a list of all optimal journeys from the departure station, sorted by departure time then arrival time
     * @throws NullPointerException      if the profile is null
     * @throws IndexOutOfBoundsException if the departure station ID is invalid
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {

        TimeTable timeTable = profile.timeTable();
        LocalDate date = profile.date();
        List<Journey> journeys = new ArrayList<>();
        ParetoFront depStationFront = profile.forStation(depStationId);

        // Debug: front size
        Stations stations = timeTable.stations();
        System.out.printf("Debug JourneyExtractor.journeys: depStationId=%d [%s] frontSize=%d, arrStationId=%d [%s]%n",
            depStationId, stations.name(depStationId), depStationFront.size(),
            profile.arrStationId(), stations.name(profile.arrStationId()));

        depStationFront.forEach((long criteria) -> {
            // Extract journey parameters
            int depMins = PackedCriteria.depMins(criteria);
            int arrMins = PackedCriteria.arrMins(criteria);
            int changes = PackedCriteria.changes(criteria);
            int payload = PackedCriteria.payload(criteria);

            // Debug each criteria
            System.out.println("\nDebug JourneyExtractor.criteria: new criteria");
            System.out.printf("Debug JourneyExtractor.criteria: depMins=%d (%02d:%02d) arrMins=%d (%02d:%02d) changes=%d payload=%d%n",
                depMins, depMins/60, depMins%60,
                arrMins, arrMins/60, arrMins%60,
                changes, payload);

            // Extract first connection ID and stops to travel
            int firstConnId = Bits32_24_8.unpack24(payload); // Extract upper 24 bits
            int stopsToTravel = Bits32_24_8.unpack8(payload); // Extract lower 8 bits

            // Debug connection info
            Connections connections = profile.connections();
            int connDepStopId = connections.depStopId(firstConnId);
            int connArrStopId = connections.arrStopId(firstConnId);
            System.out.printf("Debug JourneyExtractor.connection: firstConnId=%d, stopsToTravel=%d, depStopId=%d [%s], arrStopId=%d [%s]%n",
                firstConnId, stopsToTravel,
                connDepStopId, stations.name(timeTable.stationId(connDepStopId)),
                connArrStopId, stations.name(timeTable.stationId(connArrStopId)));

            // stopsToTravel represents the number of intermediate stops for each connection in the journey (right here, it is for the very first connection)

            Journey journey = extractJourney(timeTable, date, profile, depStationId,
                    depMins, arrMins, changes, firstConnId, stopsToTravel);
            journeys.add(journey);
        });

        // Sort journeys by departure time, then arrival time
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        System.out.printf("Debug JourneyExtractor.journeys: Found %d journeys for depStationId=%d [%s]%n",
            journeys.size(), depStationId, stations.name(depStationId));

        return journeys;
    }

    /**
     * Extracts a complete journey based on the first connection and optimization criteria.
     */
    private static Journey extractJourney(TimeTable timeTable, LocalDate date, Profile profile,
                                          int depStationId, int depMins, int arrMins, int changes,
                                          int firstConnId, int stopsToTravel) {
        Connections connections = profile.connections();
        Trips trips = profile.trips();
        // debug
        Stations stations = timeTable.stations();

        System.out.printf("Debug extractJourney: start extracting journey from depStationId=%d [%s] to arrStationId=%d [%s], changes=%d%n",
            depStationId, stations.name(depStationId),
            profile.arrStationId(), stations.name(profile.arrStationId()),
            changes);

        List<Journey.Leg> legs = new ArrayList<>();

        // Current position and criteria tracking
        int currentStationId = depStationId;
        int remainingChanges = changes;
        int targetArrMins = arrMins;

        // First connection details
        int connDepStopId = connections.depStopId(firstConnId);
        int depStopId = connDepStopId;
        int connTripId = connections.tripId(firstConnId);

        System.out.printf("Debug extractJourney: firstConnId=%d depStopId=%d [%s], tripId=%d%n",
            firstConnId, depStopId, stations.name(timeTable.stationId(depStopId)), connTripId);

        // If the journey doesn't start directly from the departure station, add a walking leg
        if (timeTable.stationId(connDepStopId) != depStationId) {
            // Add initial walking leg
            Stop depStop = createStop(timeTable, depStationId);
            Stop connDepStop = createStop(timeTable, connDepStopId);

            LocalDateTime depTime = minsToDateTime(date, depMins);
            int walkMins = timeTable.transfers().minutesBetween(depStationId, timeTable.stationId(connDepStopId));
            LocalDateTime WalkToConnDepTime = depTime.plusMinutes(walkMins);

            System.out.printf("Debug extractJourney: Adding initial walking leg from stationId=%d [%s] to stationId=%d [%s] (%d mins)%n",
                depStationId, stations.name(depStationId),
                timeTable.stationId(connDepStopId), stations.name(timeTable.stationId(connDepStopId)),
                walkMins);

            legs.add(new Journey.Leg.Foot(depStop, depTime, connDepStop, WalkToConnDepTime));
        }

        // Continue extracting journey segments until we reach the destination
        while (remainingChanges >= 0) {
            // Get the current connection
            int connId = firstConnId;
            int tripId = connTripId;

            // Find destination station after traveling 'stopsToTravel' stops
            int finalConnectionIndex = findFinalConnectionIndex(connections, connId, stopsToTravel);
            int arrStopId = connections.arrStopId(finalConnectionIndex);
            int arrStationId = timeTable.stationId(arrStopId);

            System.out.printf("Debug extractJourney: Transport leg - connId=%d to finalConnId=%d, from stationId=%d [%s] to stationId=%d [%s], stopsToTravel=%d%n",
                connId, finalConnectionIndex,
                timeTable.stationId(depStopId), stations.name(timeTable.stationId(depStopId)),
                arrStationId, stations.name(arrStationId),
                stopsToTravel);

            // Create transport leg
            List<Journey.Leg.IntermediateStop> intermediateStops = extractIntermediateStops(
                    timeTable, date, connections, connId, stopsToTravel);

            Stop depStop = createStop(timeTable, depStopId);
            Stop arrStop = createStop(timeTable, arrStopId);

            LocalDateTime depTime = minsToDateTime(date, connections.depMins(connId));
            LocalDateTime arrTime = minsToDateTime(date, connections.arrMins(finalConnectionIndex));

            String route = timeTable.routes().name(trips.routeId(tripId));
            String destination = trips.destination(tripId);
            Vehicle vehicle = timeTable.routes().vehicle(trips.routeId(tripId));

            System.out.printf("Debug extractJourney: Adding transport leg - route=%s, destination=%s, vehicle=%s, depTime=%s, arrTime=%s%n",
                route, destination, vehicle, depTime.toLocalTime(), arrTime.toLocalTime());

            legs.add(new Journey.Leg.Transport(
                    depStop, depTime, arrStop, arrTime, intermediateStops, vehicle, route, destination));

            // Check if we've reached the destination
            if (remainingChanges == 0) {
                // If we're not yet at the final destination, add a final walking leg
                if (arrStationId != profile.arrStationId()) {
                    // Add final walking leg
                    Stop finalDepStop = arrStop;
                    Stop finalArrStop = createStop(timeTable, profile.arrStationId());
                    int walkMins = timeTable.transfers().minutesBetween(arrStationId, profile.arrStationId());
                    LocalDateTime finalArrTime = arrTime.plusMinutes(walkMins);
                    System.out.println("Debug extractJourney: Adding final walking leg from stationId=" + arrStationId
                            + " to stationId=" + profile.arrStationId() + " (" + walkMins + " mins)");
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
            int nextConnTripId = connections.tripId(nextConnId);

            System.out.printf("\nDebug extractJourney: Next leg - remainingChanges=%d, nextConnId=%d, stopsToTravel=%d, tripId=%d%n",
                remainingChanges, nextConnId, stopsToTravel, nextConnTripId);

            // Always add a walking leg between the last arrival stop and the next departure stop
            Stop walkDepStop = arrStop;
            Stop walkArrStop = createStop(timeTable, nextDepStopId);

            LocalDateTime walkDepTime = arrTime;
            int transferMins = timeTable.transfers().minutesBetween(timeTable.stationId(arrStopId), timeTable.stationId(nextDepStopId));
            LocalDateTime walkArrTime = walkDepTime.plusMinutes(transferMins);

            System.out.printf("Debug extractJourney: Adding transfer walking leg from stationId=%d [%s] to stationId=%d [%s] (%d mins)%n",
                timeTable.stationId(arrStopId), stations.name(timeTable.stationId(arrStopId)),
                timeTable.stationId(nextDepStopId), stations.name(timeTable.stationId(nextDepStopId)),
                transferMins);

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
     * Converts minutes since start of day to LocalDateTime, handling day transitions.
     * @param baseDate The base date
     * @param mins Minutes since start of day
     * @return LocalDateTime with appropriate date and time
     */
    private static LocalDateTime minsToDateTime(LocalDate baseDate, int mins) {
        int days = mins / (24 * 60);       // Number of complete days
        int remainingMins = mins % (24 * 60); // Remaining minutes in the day
        int hours = remainingMins / 60;
        int minutes = remainingMins % 60;

        return LocalDateTime.of(baseDate.plusDays(days), LocalTime.of(hours, minutes));
    }

    /**
     * Finds the index of the final connection (i.e. after traveling a specified number of stops).
     */
    private static int findFinalConnectionIndex(Connections connections, int startConnId, int stopsToTravel) {
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

        // Debug start of extraction
        Stations stations = timeTable.stations();
        System.out.printf("Debug extractIntermediateStops: startConnId=%d, stopsToTravel=%d, depStopId=%d [%s], arrStopId=%d [%s]%n",
            startConnId, stopsToTravel,
            connections.depStopId(startConnId), stations.name(timeTable.stationId(connections.depStopId(startConnId))),
            connections.arrStopId(startConnId), stations.name(timeTable.stationId(connections.arrStopId(startConnId))));

        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        int currentConnId = startConnId;

        // For each intermediate stop
        for (int i = 0; i < stopsToTravel; i++) {
            int nextConnId = connections.nextConnectionId(currentConnId);

            // Create intermediate stop data
            int arrStopId = connections.arrStopId(currentConnId);
            int depStopId = connections.depStopId(nextConnId);

            //debug
            int arrMins = connections.arrMins(currentConnId);
            int depMinsNext = connections.depMins(nextConnId);
            // Debug each intermediate stop timing
            System.out.printf(
                "Debug IntermediateStop %d: connId=%d, arrStopId=%d [%s], arrMins=%d (%02d:%02d), nextConnId=%d, depStopId=%d [%s], depMins=%d (%02d:%02d)%n",
                i,
                currentConnId,
                arrStopId, stations.name(timeTable.stationId(arrStopId)),
                arrMins, arrMins/60, arrMins%60,
                nextConnId,
                depStopId, stations.name(timeTable.stationId(depStopId)),
                depMinsNext, depMinsNext/60, depMinsNext%60
            );

            Stop stop = createStop(timeTable, arrStopId);

            LocalDateTime arrTime = minsToDateTime(date, connections.arrMins(currentConnId));
            LocalDateTime depTime = minsToDateTime(date, connections.depMins(nextConnId));

            intermediateStops.add(new Journey.Leg.IntermediateStop(stop, arrTime, depTime));

            currentConnId = nextConnId;
        }

        System.out.printf("Debug extractIntermediateStops: Extracted %d intermediate stops%n", intermediateStops.size());
        return intermediateStops;
    }
}