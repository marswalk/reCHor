package ch.epfl.rechor.timetable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a timetable with cached date-dependent data (connections and trips).
 */
public class CachedTimeTable implements TimeTable {
    private final TimeTable underlying;
    private LocalDate cachedDate;
    private Connections cachedConnections;
    private Trips cachedTrips;

    /**
     * Constructs a CachedTimeTable with the given underlying timetable.
     *
     * @param underlying the underlying timetable
     * @throws NullPointerException if the underlying timetable is null
     */
    public CachedTimeTable(TimeTable underlying) {
        this.underlying = Objects.requireNonNull(underlying);
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        Objects.requireNonNull(date);

        if (!date.equals(cachedDate)) {
            cachedConnections = underlying.connectionsFor(date);
            cachedDate = date;
        }

        return cachedConnections;
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        Objects.requireNonNull(date);

        if (!date.equals(cachedDate) || cachedTrips == null) {
            cachedTrips = underlying.tripsFor(date);
            cachedDate = date;
        }

        return cachedTrips;
    }

    @Override
    public Stations stations() {
        return underlying.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return underlying.platforms();
    }

    @Override
    public Routes routes() {
        return underlying.routes();
    }

    @Override
    public Transfers transfers() {
        return underlying.transfers();
    }

    @Override
    public int stationId(int stopId) {
        return underlying.stationId(stopId);
    }

    @Override
    public String platformName(int stopId) {
        return underlying.platformName(stopId);
    }

    @Override
    public boolean isStationId(int stopId) {
        return underlying.isStationId(stopId);
    }

    @Override
    public boolean isPlatformId(int stopId) {
        return underlying.isPlatformId(stopId);
    }
}