package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Journey(List<Leg> legs) {

    public sealed interface Leg {
        // all abstract and public already
        Stop depStop();
        LocalDateTime depTime();
        Stop arrStop();
        LocalDateTime arrTime();
        List<IntermediateStop> intermediateStops();
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime){
            public IntermediateStop {
                Objects.requireNonNull(stop, "Stop must not be null");
                Preconditions.checkArgument(!(arrTime.isBefore(depTime)));
            }
        }

        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {
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
        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {
            public Foot {
                Objects.requireNonNull(depStop, "Departure stop must not be null");
                Objects.requireNonNull(depTime, "Departure time must not be null");
                Objects.requireNonNull(arrStop, "Arrival stop must not be null");
                Objects.requireNonNull(arrTime, "Arrival time must not be null");
                Preconditions.checkArgument(!(arrTime.isBefore(depTime)));
            }
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }

    public Journey {
        Preconditions.checkArgument(!legs.isEmpty());
        
        legs = List.copyOf(legs);
    }

    public Stop depStop() {
        return legs.getFirst().depStop();
    }
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }
}