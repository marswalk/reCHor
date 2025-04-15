package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents an immutable router that computes optimal journey profiles
 * from any station to a specific destination on a given date.
 *
 * @author Your Name
 */
public record Router(TimeTable timeTable) {

    /**
     * Constructs a new Router with the given timetable.
     *
     * @param timeTable the timetable containing schedule data
     * @throws NullPointerException if timeTable is null
     */
    public Router {
        Objects.requireNonNull(timeTable, "TimeTable cannot be null");
    }

    /**
     * Computes the profile of all optimal journeys from any station to the given destination
     * station on the specified date using the Connection Scan Algorithm (CSA).
     *
     * @param date the date for which to compute journeys
     * @param destStationId the ID of the destination station
     * @return a profile containing all optimal journeys to the destination
     * @throws NullPointerException if date is null
     */
    public Profile profile(LocalDate date, int destStationId) {
        Objects.requireNonNull(date, "Date cannot be null");

        // Get connections and transfers for the specified date
        Connections connections = timeTable.connectionsFor(date);
        Transfers transfers = timeTable.transfers();

        // Create a profile builder
        Profile.Builder profileBuilder = new Profile.Builder(timeTable, date, destStationId);

        // Calculate walking times to destination (or -1 if not walkable)
        int[] walkingTimesToDest = calculateWalkingTimes(destStationId, transfers);

        // Process connections in decreasing departure time order (increasing index)
        for (int connIdx = 0; connIdx < connections.size(); connIdx++) {
            // Create a Pareto frontier for this connection
            ParetoFront.Builder connFrontier = new ParetoFront.Builder();

            // Get connection details
            int arrStopId = connections.arrStopId(connIdx);
            int arrTime = connections.arrMins(connIdx);
            int tripId = connections.tripId(connIdx);

            // Convert arrival stop to station if it's a platform
            int arrStationId = timeTable.stationId(arrStopId);

            // Option 1: Walk from arrival to destination
            int walkingTime = walkingTimesToDest[arrStationId];
            if (walkingTime >= 0) {
                int arrivalAtDest = arrTime + walkingTime;
                // Add tuple (arrival time, 0 changes, connIdx as payload)
                connFrontier.add(arrivalAtDest, 0, connIdx);
            }

            // Option 2: Continue with the next connection in the trip
            ParetoFront.Builder tripFrontier = profileBuilder.forTrip(tripId);
            if (tripFrontier != null) {
                connFrontier.addAll(tripFrontier);
            }

            // Option 3: Change vehicle at the end of this connection
            ParetoFront.Builder arrStationFrontier = profileBuilder.forStation(arrStationId);
            if (arrStationFrontier != null) {
                int finalConnIdx = connIdx;
                arrStationFrontier.forEach(tuple -> {
                    // Check if the departure time in the tuple is after the arrival of this connection
                    if (!PackedCriteria.hasDepMins(tuple) || PackedCriteria.depMins(tuple) >= arrTime) {
                        int tupleArrMins = PackedCriteria.arrMins(tuple);
                        int tupleChanges = PackedCriteria.changes(tuple);
                        // Add tuple with one more change
                        connFrontier.add(tupleArrMins, tupleChanges + 1, finalConnIdx);
                    }
                });
            }

            // Optimization 1: Skip further processing if frontier is empty
            if (connFrontier.isEmpty()) {
                continue;
            }

            // Update trip frontier
            if (tripFrontier == null) {
                tripFrontier = new ParetoFront.Builder();
                profileBuilder.setForTrip(tripId, tripFrontier);
            }
            tripFrontier.addAll(connFrontier);

            // Get departure station and time
            int depStopId = connections.depStopId(connIdx);
            int depTime = connections.depMins(connIdx);

            // Convert departure stop to station if it's a platform
            int depStationId = timeTable.stationId(depStopId);

            // Get or create frontier for departure station
            ParetoFront.Builder depStationFrontier = profileBuilder.forStation(depStationId);
            if (depStationFrontier == null) {
                depStationFrontier = new ParetoFront.Builder();
                profileBuilder.setForStation(depStationId, depStationFrontier);
            }

            // Optimization 2: Check if all tuples in connFrontier are dominated by depStationFrontier
            boolean allDominated = !connFrontier.isEmpty() &&
                    depStationFrontier.fullyDominates(connFrontier, depTime);

            if (!allDominated) {
                // Update frontiers for all walkable stations
                updateStationFrontiers(connFrontier, depStationId, depTime, transfers, profileBuilder);
            }
        }

        // Build and return the profile
        return profileBuilder.build();
    }

    /**
     * Calculates walking times from each station to the destination station.
     * Returns -1 for stations that are not walkable to the destination.
     */
    private int[] calculateWalkingTimes(int destStationId, Transfers transfers) {
        int[] walkingTimes = new int[timeTable.stations().size()];
        Arrays.fill(walkingTimes, -1);

        // Find all stations that can walk to the destination
        for (int stationId = 0; stationId < walkingTimes.length; stationId++) {
            try {
                int walkingTime = transfers.minutesBetween(stationId, destStationId);
                walkingTimes[stationId] = walkingTime;
            } catch (NoSuchElementException e) {
                // No transfer exists, leave as -1
            }
        }

        return walkingTimes;
    }

    /**
     * Updates the frontiers of the departure station and all stations that can walk to it.
     */
    private void updateStationFrontiers(
            ParetoFront.Builder connFrontier,
            int depStationId,
            int depTime,
            Transfers transfers,
            Profile.Builder profileBuilder) {

        // Update the frontier for the departure station
        updateStationFrontier(connFrontier, depStationId, depTime, profileBuilder);

        // Update frontiers for all stations that can walk to the departure station
        for (int stationId = 0; stationId < timeTable.stations().size(); stationId++) {
            if (stationId == depStationId) {
                continue;
            }

            try {
                int walkingTime = transfers.minutesBetween(stationId, depStationId);
                int adjustedDepTime = depTime - walkingTime;
                updateStationFrontier(connFrontier, stationId, adjustedDepTime, profileBuilder);
            } catch (NoSuchElementException e) {
                // No transfer exists between these stations, skip
            }
        }
    }

    /**
     * Updates the frontier for a single station.
     */
    private void updateStationFrontier(
            ParetoFront.Builder connFrontier,
            int stationId,
            int depTime,
            Profile.Builder profileBuilder) {

        ParetoFront.Builder stationFrontier = profileBuilder.forStation(stationId);
        if (stationFrontier == null) {
            stationFrontier = new ParetoFront.Builder();
            profileBuilder.setForStation(stationId, stationFrontier);
        }

        ParetoFront.Builder finalStationFrontier = stationFrontier;
        connFrontier.forEach(tuple -> {
            // Create a new tuple with departure time and add it to the station frontier
            long tupleWithDep = PackedCriteria.withDepMins(tuple, depTime);
            finalStationFrontier.add(tupleWithDep);
        });
    }
}
