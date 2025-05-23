package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;

/**
 * Represents a router capable of calculating the profile of all optimal journeys
 * to reach a given destination stop on a specific day using the Connection Scan Algorithm (CSA).
 * <p>
 * The algorithm iteratively updates Pareto fronts for trips and stations to determine
 * the optimal journeys based on criteria such as arrival time and number of transfers.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record Router(TimeTable timeTable) {

    // Constant for "not walkable" sentinel value
    private static final int WALKABLE_DISTANCE_NOT_FOUND = -1;

    /**
     * Computes the profile of all optimal journeys to a given destination stop on a specific date.
     *
     * @param date              the date for which the journeys are calculated
     * @param destinationStopId the ID of the destination stop
     * @return the profile containing the Pareto fronts of optimal journeys
     */
    public Profile profile(LocalDate date, int destinationStopId) {
        Profile.Builder profile = new Profile.Builder(
                timeTable, date, timeTable.stationId(destinationStopId));

        Connections connections = timeTable.connectionsFor(date);
        int[] walkableDistance = getWalkableDistance(destinationStopId);

        for (int connectionId = 0; connectionId < connections.size(); connectionId++) {
            ParetoFront.Builder pareto = new ParetoFront.Builder();

            int depStopId = connections.depStopId(connectionId);
            int arrStopId = connections.arrStopId(connectionId);
            int depMins = connections.depMins(connectionId);
            int arrMins = connections.arrMins(connectionId);
            int tripId = connections.tripId(connectionId);
            int depStationId = timeTable.stationId(depStopId);
            int arrStationId = timeTable.stationId(arrStopId);

            if (walkableDistance[arrStationId] != WALKABLE_DISTANCE_NOT_FOUND) {
                int payload = Bits32_24_8.pack(connectionId, 0);
                pareto.add(arrMins + walkableDistance[arrStationId], 0, payload);
            }

            ParetoFront.Builder tripFront = profile.forTrip(tripId);
            if (tripFront != null) {
                pareto.addAll(tripFront);
            }

            ParetoFront.Builder stationBuilder = profile.forStation(arrStationId);
            if (stationBuilder != null) {
                int payload = Bits32_24_8.pack(connectionId, 0);
                stationBuilder.forEach(t -> {
                    if (PackedCriteria.hasDepMins(t) && PackedCriteria.depMins(t) >= arrMins) {
                        pareto.add(PackedCriteria.arrMins(t),
                                PackedCriteria.changes(t) + 1,
                                payload);
                    }
                });
            }

            if (pareto.isEmpty()) {
                continue;
            }

            profile.setForTrip(tripId, pareto);

            ParetoFront.Builder existingStationFront = profile.forStation(depStationId);
            if (existingStationFront != null && existingStationFront.fullyDominates(pareto, depMins)) {
                continue;
            }

            updateStation(depStationId, depMins, profile, connectionId, pareto, connections);
        }

        return profile.build();
    }

    /**
     * Updates the Pareto front of a station based on the given connection and its associated Pareto front.
     *
     * @param depStationId the ID of the departure station
     * @param depMins      the departure time in minutes
     * @param profile      the profile being updated
     * @param connectionId the ID of the current connection
     * @param pareto       the Pareto front of the current connection
     * @param connections  the connections data
     */
    private void updateStation(int depStationId, int depMins, Profile.Builder profile,
                               int connectionId, ParetoFront.Builder pareto, Connections connections) {
        int transfersRange = timeTable.transfers().arrivingAt(depStationId);

        for (int transferIndice = PackedRange.startInclusive(transfersRange);
             transferIndice < PackedRange.endExclusive(transfersRange); transferIndice++) {

            int transferDepStationId = timeTable.transfers().depStationId(transferIndice);
            int departureTime = depMins - timeTable.transfers().minutes(transferIndice);

            ParetoFront.Builder stationFront = profile.forStation(transferDepStationId);
            if (stationFront == null) {
                stationFront = new ParetoFront.Builder();
                profile.setForStation(transferDepStationId, stationFront);
            }

            pareto.forEach(t -> {
                int intermediateStops;
                int arrivalConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(t));
                int departurePos = connections.tripPos(connectionId);
                int arrivalPos = connections.tripPos(arrivalConnectionId);
                intermediateStops = arrivalPos - departurePos;

                int newPayload = Bits32_24_8.pack(connectionId, intermediateStops);

                long packedCriteria = PackedCriteria.pack(PackedCriteria.arrMins(t), PackedCriteria.changes(t), newPayload);
                profile.forStation(transferDepStationId).add(PackedCriteria.withDepMins(packedCriteria, departureTime));
            });
        }
    }

    /**
     * Retrieves the walkable distance for each station from the given destination stop ID.
     *
     * @param destinationStopId the ID of the destination stop
     * @return an array containing the walkable distances for each station
     */
    private int[] getWalkableDistance(int destinationStopId) {
        int[] walkableDistance = new int[timeTable.stations().size()];
        for (int i = 0; i < timeTable.stations().size(); i++) {
            walkableDistance[i] = WALKABLE_DISTANCE_NOT_FOUND;
        }

        int range = timeTable.transfers().arrivingAt(timeTable.stationId(destinationStopId));
        for (int transferIndice = PackedRange.startInclusive(range);
             transferIndice < PackedRange.endExclusive(range);
             transferIndice++) {

            walkableDistance[timeTable.transfers().depStationId(transferIndice)] = timeTable.transfers().minutes(transferIndice);
        }
        return walkableDistance;
    }
}
