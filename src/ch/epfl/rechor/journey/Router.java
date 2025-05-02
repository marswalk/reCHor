package ch.epfl.rechor.journey;


import ch.epfl.rechor.Bits32_24_8;

import ch.epfl.rechor.PackedRange;

import ch.epfl.rechor.timetable.Connections;

import ch.epfl.rechor.timetable.TimeTable;


import java.time.LocalDate;



/**

 * Router responsible for calculating optimal profile.

 *

 * This class uses the Pareto front algorithm to calculate optimal

 * travel profiles from a given timetable, taking into account

 * connections, travel times and multiple optimization criteria.

 *

 * @author Liam Vuilleumier (394026)

 * @author David Asad-Syed (391705)

 */

public record Router(TimeTable timeTable) {



    /**

     * Computes a travel profile for a given date and destination stop ID.

     *

     * @param date The date for which the profile is computed.

     * @param destinationStopId The ID of the destination stop.

     * @return A Profile object containing the computed travel profile.

     */

    public Profile profile(LocalDate date, int destinationStopId) {

        Profile.Builder profile = new Profile.Builder(

                timeTable, date, timeTable.stationId(destinationStopId));


        Connections connections = timeTable.connectionsFor(date);

        int[] walkableDistance = getWalkableDistance(destinationStopId);


// process connecting in descending order.

        for (int connectionId = 0; connectionId < connections.size(); connectionId++) {

            ParetoFront.Builder pareto = new ParetoFront.Builder();


            int depStopId = connections.depStopId(connectionId);

            int arrStopId = connections.arrStopId(connectionId);

            int depMins = connections.depMins(connectionId);

            int arrMins = connections.arrMins(connectionId);

            int tripId = connections.tripId(connectionId);

            int depStationId = timeTable.stationId(depStopId);

            int arrStationId = timeTable.stationId(arrStopId);


// walk, if possible, from arrival of l to the destination.

            if (walkableDistance[arrStationId] != -1) {

                int payload = Bits32_24_8.pack(connectionId, 0);

                pareto.add(arrMins + walkableDistance[arrStationId], 0, payload);

            }


// Continue with the next connection in the same trip.

            ParetoFront.Builder tripFront = profile.forTrip(tripId);

            if (tripFront != null) {

                pareto.addAll(tripFront);

            }


// Change the vehicle at the end of l.

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


// If pareto is empty, take the next connection.

            if (pareto.isEmpty()) {

                continue;

            }


// update of the trip of profile.

            profile.setForTrip(tripId, pareto);


// Check if we already have a front for this station and if it dominates the current one

            ParetoFront.Builder existingStationFront = profile.forStation(depStationId);

            if (existingStationFront != null && existingStationFront.fullyDominates(pareto, depMins)) {

                continue;

            }


// update of the station of profile.

            updateStation(depStationId, depMins, profile, connectionId, pareto, connections);

        }


        return profile.build();

    }



    /**

     * Updates the station profile by considering transfers and adding new tuples to the profile.

     *

     * @param depStationId The departure station ID.

     * @param depmins The departure time in minutes.

     * @param profile The profile builder to update.

     * @param connectionId The ID of the current connection.

     * @param pareto The Pareto front containing optimal tuples.

     * @param connections The connections object containing all connection details.

     */

    private void updateStation(int depStationId, int depmins, Profile.Builder profile,

                               int connectionId, ParetoFront.Builder pareto, Connections connections) {


        int transfersRange = timeTable.transfers().arrivingAt(depStationId);


        for (int transferIndice = PackedRange.startInclusive(transfersRange);

             transferIndice < PackedRange.endExclusive(transfersRange); transferIndice++) {


            int transferDepStationId = timeTable.transfers().depStationId(transferIndice);

            int departureTime = depmins - timeTable.transfers().minutes(transferIndice);


            ParetoFront.Builder stationFront = profile.forStation(transferDepStationId);

            if (stationFront == null) {

                stationFront = new ParetoFront.Builder();

                profile.setForStation(transferDepStationId, stationFront);

            }


            pareto.forEach(t -> {

// computer the number of intermediate stop.

                int intermediateStops;

                int arrivalConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(t));

                int departurePos = connections.tripPos(connectionId);

                int arrivalPos = connections.tripPos(arrivalConnectionId);

                intermediateStops = arrivalPos - departurePos;


// creation of the new payload

                int newPayload = Bits32_24_8.pack(connectionId, intermediateStops);


// add the tuple to the stations of the profile.

                long packedCriteria = PackedCriteria.pack(PackedCriteria.arrMins(t), PackedCriteria.changes(t), newPayload);

                profile.forStation(transferDepStationId).add(PackedCriteria.withDepMins(packedCriteria, departureTime));

            });

        }

    }



    /**

     * Computes the walkable distances to the destination stop for all stations.

     *

     * @param destinationStopId the ID of the destination stop.

     * @return an array where each index represents a station ID and

     * the value is the walkable distance in minutes.

     */

    private int[] getWalkableDistance(int destinationStopId) {

// initialize the walk distance array for all stations.

        int[] walkableDistance = new int[timeTable.stations().size()];

        for (int i = 0; i < timeTable.stations().size(); i++) {

            walkableDistance[i] = -1; // Default value indicating no walkable distance.

        }


// compute the distance to walk to the destination

        int range = timeTable.transfers().arrivingAt(timeTable.stationId(destinationStopId));

        for (int transferIndice = PackedRange.startInclusive(range);

             transferIndice < PackedRange.endExclusive(range);

             transferIndice++) {


            walkableDistance[timeTable.transfers().depStationId(transferIndice)] = timeTable.transfers().minutes(transferIndice);

        }

        return walkableDistance;

    }

}
