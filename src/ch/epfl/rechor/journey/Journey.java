package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Represents a journey composed of multiple legs (foot legs and transport legs).
 * Ensures valid chronological and sequential ordering of legs.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 *
 */
public record Journey(List<Leg> legs) {

    /**
     * A leg of a journey, which can be a foot leg or a transport leg.
     */
    public sealed interface Leg {
        /**
         * Returns the departure stop of the leg.
         *
         * @return the departure stop
         */
        Stop depStop();

        /**
         * Returns the departure time of the leg.
         *
         * @return the departure time
         */
        LocalDateTime depTime();

        /**
         * Returns the arrival stop of the leg.
         *
         * @return the arrival stop
         */
        Stop arrStop();

        /**
         * Returns the arrival time of the leg.
         *
         * @return the arrival time
         */
        LocalDateTime arrTime();

        /**
         * Returns the list of intermediate stops for this leg.
         *
         * @return the list of intermediate stops
         */
        List<IntermediateStop> intermediateStops();

        /**
         * Returns the duration of this leg.
         *
         * @return the duration of the leg
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Represents an intermediate stop between departure and arrival.
         */
        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {
            /**
             * Constructs an IntermediateStop.
             *
             * @param stop the stop
             * @param arrTime the arrival time
             * @param depTime the departure time
             * @throws NullPointerException if stop, arrTime, or depTime is null
             * @throws IllegalArgumentException if depTime is before arrTime
             */
            public IntermediateStop {
                Objects.requireNonNull(stop, "Stop must not be null");
                Preconditions.checkArgument(!(depTime.isBefore(arrTime)));
            }
        }

        /**
         * Represents a transport leg of a journey, specifying vehicle, route, and destination.
         */
        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {
            /**
             * Constructs a Transport leg.
             *
             * @param depStop the departure stop
             * @param depTime the departure time
             * @param arrStop the arrival stop
             * @param arrTime the arrival time
             * @param intermediateStops the list of intermediate stops
             * @param vehicle the vehicle
             * @param route the route
             * @param destination the destination
             * @throws NullPointerException if any parameter is null
             * @throws IllegalArgumentException if arrTime is before depTime
             */
            public Transport {
                Objects.requireNonNull(depStop, "Departure stop must not be null");
                Objects.requireNonNull(depTime, "Departure time must not be null");
                Objects.requireNonNull(arrStop, "Arrival stop must not be null");
                Objects.requireNonNull(arrTime, "Arrival time must not be null");
                Objects.requireNonNull(vehicle, "Vehicle must not be null");
                Objects.requireNonNull(route, "Route must not be null");
                Objects.requireNonNull(destination, "Destination must not be null");
                Preconditions.checkArgument(!(arrTime.isBefore(depTime)));
                intermediateStops = List.copyOf(intermediateStops);
            }
        }

        /**
         * Represents a foot leg of a journey, possibly used for transfers.
         */
        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {
            /**
             * Constructs a Foot leg.
             *
             * @param depStop the departure stop
             * @param depTime the departure time
             * @param arrStop the arrival stop
             * @param arrTime the arrival time
             * @throws NullPointerException if any parameter is null
             * @throws IllegalArgumentException if arrTime is before depTime
             */
            public Foot {
                Objects.requireNonNull(depStop, "Departure stop must not be null");
                Objects.requireNonNull(depTime, "Departure time must not be null");
                Objects.requireNonNull(arrStop, "Arrival stop must not be null");
                Objects.requireNonNull(arrTime, "Arrival time must not be null");
                Preconditions.checkArgument(!(arrTime.isBefore(depTime)));
            }

            /**
             * Returns an empty list of intermediate stops for a foot leg.
             *
             * @return an empty list of intermediate stops
             */
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Checks if the foot leg is a transfer (same station).
             *
             * @return true if it is a transfer, false otherwise
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }

    /**
     * Constructs a Journey.
     *
     * @param legs the list of legs
     * @throws IllegalArgumentException if legs is empty, or if legs are not in valid chronological and sequential order
     */
    public Journey {
        Preconditions.checkArgument(!legs.isEmpty());
        for (int i = 1; i < legs.size(); i++) {
            Leg previous = legs.get(i - 1);
            Leg current = legs.get(i);

            Preconditions.checkArgument(!current.depTime().isBefore(previous.arrTime()));
            Preconditions.checkArgument(previous.arrStop().equals(current.depStop()));
            Preconditions.checkArgument((previous instanceof Leg.Foot) != (current instanceof Leg.Foot));
        }
        legs = List.copyOf(legs);
    }

    /**
     * Returns the departure stop of the journey.
     *
     * @return the departure stop
     */
    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    /**
     * Returns the arrival stop of the journey.
     *
     * @return the arrival stop
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    /**
     * Returns the departure time of the journey.
     *
     * @return the departure time
     */
    public LocalDateTime depTime() {
        return legs.get(0).depTime();
    }

    /**
     * Returns the arrival time of the journey.
     *
     * @return the arrival time
     */
    public LocalDateTime arrTime() {
        return legs.get(legs.size() - 1).arrTime();
    }

    /**
     * Returns the duration of the journey.
     *
     * @return the duration of the journey
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }
}