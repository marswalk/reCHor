package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents a router capable of computing the profile of all optimal journeys
 * from any station to a given destination, for a specific date, using the Connection Scan Algorithm (CSA).
 *
 * The Router is immutable and public. Its only attribute is the timetable to use.
 * The main method, {@code profile}, computes the optimal journey profiles to a given destination.
 *
 * Debug outputs are printed to System.out at key steps for tracing the algorithm's execution.
 *
 * @author [Your Name]
 */
public record Router(TimeTable timeTable) {

    // Configuration for debug output frequency - only print every DEBUG_OUTPUT_INTERVAL connections
    private static final int DEBUG_OUTPUT_INTERVAL = 1000;

    /**
     * Computes the profile of all optimal journeys to the given destination station on the specified date.
     * Uses the Connection Scan Algorithm (CSA) as described in the project documentation.
     *
     * @param date         The date for which to compute optimal journeys.
     * @param dstStationId The station ID of the destination.
     * @return The computed journey profile for all stations to the destination.
     * @throws NullPointerException     if date is null.
     * @throws IllegalArgumentException if dstStationId is not a valid station.
     */
    public Profile profile(LocalDate date, int dstStationId) {
        Objects.requireNonNull(date, "date cannot be null");
        if (dstStationId < 0 || dstStationId >= timeTable.stations().size())
            throw new IllegalArgumentException("Invalid destination station ID: " + dstStationId);

        System.out.println("[CSA] Starting profile computation for destination station " + dstStationId + " on " + date);

        // Retrieve timetable data for the given date
        Connections connections = timeTable.connectionsFor(date);
        Trips trips = timeTable.tripsFor(date);
        Transfers transfers = timeTable.transfers();

        int nStations = timeTable.stations().size();
        int nTrips = trips.size();
        int nConnections = connections.size();

        // Prepare fast lookup for walking times: -1 if not walkable, else minutes
        int[] walkToDst = new int[nStations];
        Arrays.fill(walkToDst, -1);
        for (int s = 0; s < nStations; ++s) {
            try {
                walkToDst[s] = transfers.minutesBetween(s, dstStationId);
            } catch (NoSuchElementException e) {
                // Not walkable
            }
        }
        System.out.println("[CSA] Precomputed walking times to destination.");

        // Initialize Pareto front builders for stations and trips
        ParetoFront.Builder[] stationFronts = new ParetoFront.Builder[nStations];
        ParetoFront.Builder[] tripFronts = new ParetoFront.Builder[nTrips];
        for (int i = 0; i < nStations; ++i) stationFronts[i] = new ParetoFront.Builder();
        for (int i = 0; i < nTrips; ++i) tripFronts[i] = new ParetoFront.Builder();

        System.out.println("[CSA] Processing " + nConnections + " connections...");

        // Main CSA scan: connections are sorted by decreasing departure time
        for (int l = 0; l < nConnections; ++l) {
            int depStop = connections.depStopId(l);
            int arrStop = connections.arrStopId(l);
            int depMins = connections.depMins(l);
            int arrMins = connections.arrMins(l);
            int tripId = connections.tripId(l);
            int tripPos = connections.tripPos(l);

            int depStation = timeTable.stationId(depStop);
            int arrStation = timeTable.stationId(arrStop);

            // Only print debug info at regular intervals or for the first/last connection
            boolean shouldPrintDebug = l == 0 || l == nConnections - 1 || l % DEBUG_OUTPUT_INTERVAL == 0;

            if (shouldPrintDebug) {
                System.out.printf("[CSA] Processing connection %d of %d: depStation=%d, arrStation=%d, depMins=%d, arrMins=%d, tripId=%d, tripPos=%d%n",
                        l, nConnections, depStation, arrStation, depMins, arrMins, tripId, tripPos);
            }

            ParetoFront.Builder f = new ParetoFront.Builder();

            // Option 1: Walk from arrival to destination
            if (arrStation < nStations && walkToDst[arrStation] >= 0) {
                int walkDuration = walkToDst[arrStation];
                int arrivalAtDst = arrMins + walkDuration;
                f.add(arrivalAtDst, 0, 0); // payload = 0 for now, as base version
            }

            // Option 2: Continue with next connection in same trip
            f.addAll(tripFronts[tripId]);

            // Option 3: Change vehicle at end of connection
            ParetoFront.Builder arrStationFront = stationFronts[arrStation];
            arrStationFront.forEach(tuple -> {
                int tupleDepMins = PackedCriteria.hasDepMins(tuple) ? PackedCriteria.depMins(tuple) : -1;
                int tupleArrMins = PackedCriteria.arrMins(tuple);
                int tupleChanges = PackedCriteria.changes(tuple);
                if (tupleDepMins == -1 || tupleDepMins >= arrMins) {
                    long newTuple = PackedCriteria.pack(tupleArrMins, tupleChanges + 1, 0);
                    f.add(newTuple);
                }
            });

            // Update Pareto front for the trip
            tripFronts[tripId].addAll(f);

            // Only show trip updates on debug intervals
            if (shouldPrintDebug && !f.isEmpty()) {
//                System.out.printf("[CSA]   Updated trip %d Pareto front: %s%n", tripId, tripFronts[tripId]);
            }

            // Update Pareto fronts for all stations from which you can walk to depStation
            int arrivingTransfers = transfers.arrivingAt(depStation);
            if (arrivingTransfers != -1) {
                // Unpack transfer indices (assume packed range: lower 16 bits = start, upper 16 bits = end)
                int start = arrivingTransfers & 0xFFFF;
                int end = (arrivingTransfers >>> 16) & 0xFFFF;
                for (int t = start; t < end; ++t) {
                    int fromStation = transfers.depStationId(t);
                    int walkDuration = transfers.minutes(t);
                    int depTime = depMins - walkDuration;
                    f.forEach(tuple -> {
                        long tupleWithDep = PackedCriteria.withDepMins(tuple, depTime);
                        stationFronts[fromStation].add(tupleWithDep);

                        // Skip detailed transfer debug output to reduce noise
                    });
                }
            }

            // Always update the frontier for the departure station itself (walking time 0)
            f.forEach(tuple -> {
                long tupleWithDep = PackedCriteria.withDepMins(tuple, depMins);
                stationFronts[depStation].add(tupleWithDep);

                // Skip detailed station update debug output to reduce noise
            });

            // Print progress indicator at regular intervals
            if (shouldPrintDebug && l > 0 && l < nConnections - 1) {
                System.out.printf("[CSA] %.1f%% complete (%d/%d connections processed)%n",
                        (l * 100.0) / nConnections, l, nConnections);
            }
        }

        // Build the final Profile object using the station Pareto fronts
        Profile.Builder builder = new Profile.Builder(timeTable, date, dstStationId);
        for (int s = 0; s < nStations; ++s) {
            builder.setForStation(s, stationFronts[s]);
        }
        System.out.println("[CSA] Profile computation complete.");
        return builder.build();
    }

    /**
     * Helper to format a packed criteria tuple for debug output.
     */
    private static String tupleToString(long tuple) {
        StringBuilder sb = new StringBuilder();
        if (PackedCriteria.hasDepMins(tuple)) {
            sb.append("dep=").append(PackedCriteria.depMins(tuple)).append(", ");
        }
        sb.append("arr=").append(PackedCriteria.arrMins(tuple));
        sb.append(", chg=").append(PackedCriteria.changes(tuple));
        sb.append(", payload=").append(PackedCriteria.payload(tuple));
        return sb.toString();
    }
}
