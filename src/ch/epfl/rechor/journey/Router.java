package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;

public record Router(TimeTable timeTable) {

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

            if (walkableDistance[arrStationId] != -1) {
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

    private int[] getWalkableDistance(int destinationStopId) {
        int[] walkableDistance = new int[timeTable.stations().size()];
        for (int i = 0; i < timeTable.stations().size(); i++) {
            walkableDistance[i] = -1;
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