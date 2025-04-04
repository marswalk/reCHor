package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an immutable journey profile for a specific timetable, date, and arrival station.
 * <p>
 * A profile contains Pareto frontiers for all stations in the timetable with respect to
 * the destination station. Each frontier contains optimal journey criteria tuples.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record Profile(
        TimeTable timeTable,
        LocalDate date,
        int arrStationId,
        List<ParetoFront> stationFront) {

    /**
     * Constructs a new Profile with the specified parameters.
     * <p>
     * The list of Pareto frontiers is defensively copied to ensure immutability.
     *
     * @param timeTable    the timetable to which this profile corresponds
     * @param date         the date to which this profile corresponds
     * @param arrStationId the index of the arrival/destination station
     * @param stationFront the list of Pareto frontiers for all stations
     * @throws NullPointerException if any parameter is null
     */
    public Profile {
        Objects.requireNonNull(stationFront);

        // Defensive copy to ensure immutability
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Returns the connections for this profile.
     * <p>
     * These are the connections from the timetable for the profile's date.
     *
     * @return the connections for this profile
     */
    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    /**
     * Returns the trips for this profile.
     * <p>
     * These are the trips from the timetable for the profile's date.
     *
     * @return the trips for this profile
     */
    public Trips trips() {
        return timeTable.tripsFor(date);
    }

    /**
     * Returns the Pareto front for the specified station.
     *
     * @param stationId the index of the station
     * @return the Pareto front for the specified station
     * @throws IndexOutOfBoundsException if the station index is invalid
     */
    public ParetoFront forStation(int stationId) {
        return stationFront.get(stationId);
    }

    public static final class Builder {
        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;
        private final ParetoFront.Builder[] stationBuilders;
        private final ParetoFront.Builder[] tripBuilders;

        /**
         * Constructs a Profile builder for the given timetable, date and destination station.
         * <p>
         * The builder initializes empty arrays to store Pareto front builders for stations and trips.
         *
         * @param timeTable    the timetable to which this profile corresponds
         * @param date         the date to which this profile corresponds
         * @param arrStationId the index of the arrival/destination station
         * @throws NullPointerException if timetable or date is null
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;

            // Initialize arrays to store Pareto front builders for stations and trips
            // All elements are initially null
            this.stationBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        /**
         * Returns the Pareto front builder for the specified station.
         *
         * @param stationId the index of the station
         * @return the Pareto front builder for the station, or null if not set
         * @throws IndexOutOfBoundsException if the station index is invalid
         */
        public ParetoFront.Builder forStation(int stationId) {
            return stationBuilders[stationId];
        }

        /**
         * Associates the given Pareto front builder with the specified station.
         *
         * @param stationId the index of the station
         * @param builder   the Pareto front builder to associate with the station
         * @throws IndexOutOfBoundsException if the station index is invalid
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {
            stationBuilders[stationId] = builder;
        }

        /**
         * Returns the Pareto front builder for the specified trip.
         *
         * @param tripId the index of the trip
         * @return the Pareto front builder for the trip, or null if not set
         * @throws IndexOutOfBoundsException if the trip index is invalid
         */
        public ParetoFront.Builder forTrip(int tripId) {
            return tripBuilders[tripId];
        }

        /**
         * Associates the given Pareto front builder with the specified trip.
         *
         * @param tripId  the index of the trip
         * @param builder the Pareto front builder to associate with the trip
         * @throws IndexOutOfBoundsException if the trip index is invalid
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            tripBuilders[tripId] = builder;
        }

        /**
         * Builds and returns a simple profile without the Pareto frontiers for trips.
         * <p>
         * For any station that doesn't have an associated Pareto front builder,
         * an empty Pareto front is used.
         *
         * @return the built Profile instance
         */
        public Profile build() {
            List<ParetoFront> stationFronts = new ArrayList<>(stationBuilders.length);

            for (ParetoFront.Builder builder : stationBuilders) {
                stationFronts.add(builder != null ? builder.build() : ParetoFront.EMPTY);
            }

            return new Profile(timeTable, date, arrStationId, stationFronts);
        }
    }
}