package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents an immutable router that computes optimal journey profiles
 * from any station to a specific destination on a given date.
 *
 * @author Your Name
 */
public record Router(TimeTable timeTable) {

    // Set to false in production for better performance
    private static final boolean DEBUG = false;
    // Progress reporting frequency (number of connections between reports)
    private static final int PROGRESS_REPORT_FREQUENCY = 100000;

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
     * @param date          the date for which to compute journeys
     * @param destStationId the ID of the destination station
     * @return a profile containing all optimal journeys to the destination
     * @throws NullPointerException if date is null
     */
    public Profile profile(LocalDate date, int destStationId) {
        Objects.requireNonNull(date, "Date cannot be null");
        System.out.println("Starting profile calculation for destination: " + destStationId + " on date: " + date);
        long startTime = System.currentTimeMillis();

        // Get connections and transfers for the specified date
        Connections connections = timeTable.connectionsFor(date);
        Transfers transfers = timeTable.transfers();

        System.out.println("Loaded " + connections.size() + " connections");

        // Create a profile builder
        Profile.Builder profileBuilder = new Profile.Builder(timeTable, date, destStationId);

        // Calculate walking times to destination (or -1 if not walkable)
        int[] walkingTimesToDest = calculateWalkingTimes(destStationId, transfers);
        System.out.println("Calculated walking times to destination");
        
        // PERFORMANCE OPTIMIZATION: Precompute stations that can walk to each station
        List<int[]> reversedTransfers = precomputeReversedTransfers(transfers);
        System.out.println("Precomputed reversed transfers");

        // Stats counters
        int processedConnections = 0;
        int validFrontiers = 0;
        int skippedConnections = 0;

        // Process connections in decreasing departure time order (increasing index)
        for (int connIdx = 0; connIdx < connections.size(); connIdx++) {
            // Progress reporting
            if (connIdx % PROGRESS_REPORT_FREQUENCY == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("Processing connection: " + connIdx + "/" + connections.size() +
                    " (" + (connIdx * 100 / connections.size()) + "%) - Time elapsed: " +
                    (elapsed / 1000) + "s - Speed: " +
                    (connIdx > 0 ? String.format("%.1f", connIdx / (elapsed / 1000.0)) + " conn/s" : "N/A"));
            }

            // Get connection details
            int arrStopId = connections.arrStopId(connIdx);
            int arrTime = connections.arrMins(connIdx);
            int tripId = connections.tripId(connIdx);

            // Convert arrival stop to station if it's a platform
            int arrStationId = timeTable.stationId(arrStopId);

            // Quick check - if there's no way to continue from this stop, skip
            ParetoFront.Builder arrStationFrontier = profileBuilder.forStation(arrStationId);
            ParetoFront.Builder tripFrontier = profileBuilder.forTrip(tripId);
            int walkingTime = walkingTimesToDest[arrStationId];

            // If there's no walking option and no existing frontiers, we can skip
            if (walkingTime < 0 && tripFrontier == null && arrStationFrontier == null) {
                skippedConnections++;
                continue;
            }

            // Create a Pareto frontier for this connection
            ParetoFront.Builder connFrontier = new ParetoFront.Builder();

            // Option 1: Walk from arrival to destination
            if (walkingTime >= 0) {
                int arrivalAtDest = arrTime + walkingTime;
                int payload = encodePayload(connIdx, 0);
                connFrontier.add(arrivalAtDest, 0, payload);

                if (DEBUG && connIdx % PROGRESS_REPORT_FREQUENCY == 0) {
                    System.out.println("  Added walking option from station " + arrStationId +
                        " to destination, arrival: " + arrivalAtDest);
                }
            }

            // Option 2: Continue with the next connection in the trip
            if (tripFrontier != null) {
                connFrontier.addAll(tripFrontier);
                if (DEBUG && connIdx % PROGRESS_REPORT_FREQUENCY == 0) {
                    System.out.println("  Added options from trip " + tripId);
                }
            }

            // Option 3: Change vehicle at the end of this connection
            if (arrStationFrontier != null) {
                int finalConnIdx = connIdx;
                arrStationFrontier.forEach(tuple -> {
                    // Check if the departure time in the tuple is after the arrival of this connection
                    if (!PackedCriteria.hasDepMins(tuple) || PackedCriteria.depMins(tuple) >= arrTime) {
                        int tupleArrMins = PackedCriteria.arrMins(tuple);
                        int tupleChanges = PackedCriteria.changes(tuple);
                        int payload = encodePayload(finalConnIdx, 0);
                        connFrontier.add(tupleArrMins, tupleChanges + 1, payload);
                    }
                });

                if (DEBUG && connIdx % PROGRESS_REPORT_FREQUENCY == 0) {
                    System.out.println("  Added options from station change at " + arrStationId);
                }
            }

            // Optimization 1: Skip further processing if frontier is empty
            if (connFrontier.isEmpty()) {
                continue;
            }

            validFrontiers++;

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
                // Update frontiers for departure station and walkable stations
                updateStationFrontiers(connFrontier, depStationId, depTime, transfers, profileBuilder, 
                                      connections, connIdx, reversedTransfers);

                if (DEBUG && connIdx % PROGRESS_REPORT_FREQUENCY == 0) {
                    System.out.println("  Updated station frontiers for " + depStationId);
                }
            }

            processedConnections++;
        }

        long endTime = System.currentTimeMillis();
        double totalSeconds = (endTime - startTime) / 1000.0;
        System.out.println("Profile calculation completed in " + String.format("%.2f", totalSeconds) + " seconds");
        System.out.println("Processed " + processedConnections + " connections, skipped " + skippedConnections +
                           " connections, with " + validFrontiers + " valid frontiers");
        System.out.println("Average processing speed: " + String.format("%.1f", connections.size() / totalSeconds) + " connections/second");

        // Build and return the profile
        return profileBuilder.build();
    }

    /**
     * Precomputes for each station, which other stations can walk to it.
     * This is the reverse of the transfers graph and helps avoid expensive lookups.
     * 
     * @param transfers the transfers data
     * @return a list where each element at index i is an array of station IDs that can walk to station i
     */
    private List<int[]> precomputeReversedTransfers(Transfers transfers) {
        int stationCount = timeTable.stations().size();
        Map<Integer, List<Integer>> reversedMap = new HashMap<>();
        
        // Initialize the map for each station
        for (int i = 0; i < stationCount; i++) {
            reversedMap.put(i, new ArrayList<>());
        }
        
        // Build the reversed transfers map
        for (int fromId = 0; fromId < stationCount; fromId++) {
            for (int toId = 0; toId < stationCount; toId++) {
                if (fromId == toId) continue;
                
                try {
                    // If there's a walking path from fromId to toId, add fromId to toId's list of incoming stations
                    transfers.minutesBetween(fromId, toId);
                    reversedMap.get(toId).add(fromId);
                } catch (NoSuchElementException e) {
                    // No transfer exists, skip
                }
            }
        }
        
        // Convert the map to array format for better performance
        List<int[]> result = new ArrayList<>(stationCount);
        for (int i = 0; i < stationCount; i++) {
            List<Integer> walkableStations = reversedMap.get(i);
            int[] arr = new int[walkableStations.size()];
            for (int j = 0; j < walkableStations.size(); j++) {
                arr[j] = walkableStations.get(j);
            }
            result.add(arr);
        }
        
        return result;
    }

    /**
     * Calculates walking times from each station to the destination station.
     * Returns -1 for stations that are not walkable to the destination.
     */
    private int[] calculateWalkingTimes(int destStationId, Transfers transfers) {
        int stationCount = timeTable.stations().size();
        int[] walkingTimes = new int[stationCount];
        Arrays.fill(walkingTimes, -1);

        // Find all stations that can walk to the destination
        for (int stationId = 0; stationId < stationCount; stationId++) {
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
     * Updates the frontiers of the departure station and stations that can walk to it.
     * OPTIMIZED: Only checks stations that are known to be able to walk to the departure station.
     */
    private void updateStationFrontiers(
            ParetoFront.Builder connFrontier,
            int depStationId,
            int depTime,
            Transfers transfers,
            Profile.Builder profileBuilder,
            Connections connections,
            int connIdx,
            List<int[]> reversedTransfers) {

        // First update the frontier for the departure station itself
        updateStationFrontier(connFrontier, depStationId, depTime, profileBuilder, connections, connIdx);

        // Then update frontiers ONLY for stations that can walk to the departure station
        int[] stationsThatCanWalkToDep = reversedTransfers.get(depStationId);
        for (int stationId : stationsThatCanWalkToDep) {
            try {
                int walkingTime = transfers.minutesBetween(stationId, depStationId);
                int adjustedDepTime = depTime - walkingTime;
                
                // Optimization: Only update if the station already has a frontier
                ParetoFront.Builder stationFrontier = profileBuilder.forStation(stationId);
                if (stationFrontier == null) {
                    stationFrontier = new ParetoFront.Builder();
                    profileBuilder.setForStation(stationId, stationFrontier);
                }
                
                updateStationFrontier(connFrontier, stationId, adjustedDepTime, profileBuilder, connections, connIdx);
            } catch (NoSuchElementException e) {
                // This shouldn't happen since we precomputed the transfers, but just in case
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
            Profile.Builder profileBuilder,
            Connections connections,
            int connIdx) {

        ParetoFront.Builder stationFrontier = profileBuilder.forStation(stationId);
        if (stationFrontier == null) {
            stationFrontier = new ParetoFront.Builder();
            profileBuilder.setForStation(stationId, stationFrontier);
        }

        ParetoFront.Builder finalStationFrontier = stationFrontier;

        connFrontier.forEach(tuple -> {
            int origPayload = PackedCriteria.payload(tuple);
            int finalConnId = decodeConnectionId(origPayload);

            // Calculate the number of intermediate stops
            int intermediateStops = 0;
            if (connections.tripId(connIdx) == connections.tripId(finalConnId)) {
                // If same trip, calculate intermediate stops based on positions
                intermediateStops = connections.tripPos(finalConnId) - connections.tripPos(connIdx);
            }

            // Create a new payload with current connection ID and intermediate stops
            int newPayload = encodePayload(connIdx, intermediateStops);

            // Create a new tuple with departure time and new payload
            long tupleWithNewPayload = PackedCriteria.withPayload(tuple, newPayload);
            long tupleWithDep = PackedCriteria.withDepMins(tupleWithNewPayload, depTime);

            finalStationFrontier.add(tupleWithDep);
        });
    }

    /**
     * Encodes the connection ID and intermediate stops count into a single 32-bit payload.
     *
     * @param connectionId      the ID of the connection (24 bits)
     * @param intermediateStops the number of intermediate stops (8 bits)
     * @return the encoded 32-bit payload
     */
    private int encodePayload(int connectionId, int intermediateStops) {
        return Bits32_24_8.pack(connectionId, intermediateStops);
    }

    /**
     * Extracts the connection ID from the payload.
     *
     * @param payload the 32-bit payload
     * @return the connection ID (24 most significant bits)
     */
    private int decodeConnectionId(int payload) {
        return Bits32_24_8.unpack24(payload);
    }

    /**
     * Extracts the intermediate stops count from the payload.
     *
     * @param payload the 32-bit payload
     * @return the intermediate stops count (8 least significant bits)
     */
    private int decodeIntermediateStops(int payload) {
        return Bits32_24_8.unpack8(payload);
    }
}
