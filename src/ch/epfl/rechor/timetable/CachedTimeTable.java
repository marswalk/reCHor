package ch.epfl.rechor.timetable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A timetable implementation that caches date-dependent data (connections and trips)
 * to improve performance by avoiding redundant data loading for the same date.
 * <p>
 * This cache stores data for only one day at a time.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public class CachedTimeTable implements TimeTable {
    private final TimeTable underlying;

    // Simple cache for connections and trips
    private LocalDate cachedConnectionsDate;
    private Connections cachedConnections;

    private LocalDate cachedTripsDate;
    private Trips cachedTrips;

    /**
     * Constructs a CachedTimeTable with the given underlying timetable.
     *
     * @param underlying the underlying timetable
     * @throws NullPointerException if the underlying timetable is null
     */
    public CachedTimeTable(TimeTable underlying) {
        this.underlying = Objects.requireNonNull(underlying, "Underlying timetable cannot be null");
    }

    /**
     * Retrieves connections for the specified date, using the cache if available.
     * <p>
     * If the requested date matches the cached date, the cached connections are returned.
     * Otherwise, the connections are loaded from the underlying timetable and cached.
     *
     * @param date the date for which connections are requested
     * @return connections for the specified date
     * @throws NullPointerException if date is null
     */
    @Override
    public synchronized Connections connectionsFor(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");

        // Return cached connections if available for the requested date
        if (date.equals(cachedConnectionsDate) && cachedConnections != null) {
            return cachedConnections;
        }

        // Cache miss - load connections from underlying timetable
        long startTime = System.currentTimeMillis();
        cachedConnections = underlying.connectionsFor(date);
        cachedConnectionsDate = date;

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Loaded " + cachedConnections.size() +
                " connections for " + date + " in " + duration + "ms");

        return cachedConnections;
    }

    /**
     * Retrieves trips for the specified date, using the cache if available.
     * <p>
     * If the requested date matches the cached date, the cached trips are returned.
     * Otherwise, the trips are loaded from the underlying timetable and cached.
     *
     * @param date the date for which trips are requested
     * @return trips for the specified date
     * @throws NullPointerException if date is null
     */
    @Override
    public synchronized Trips tripsFor(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");

        // Return cached trips if available for the requested date
        if (date.equals(cachedTripsDate) && cachedTrips != null) {
            return cachedTrips;
        }

        // Cache miss - load trips from underlying timetable
        long startTime = System.currentTimeMillis();
        cachedTrips = underlying.tripsFor(date);
        cachedTripsDate = date;

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Loaded trips for " + date + " in " + duration + "ms");

        return cachedTrips;
    }

    // All other methods delegate to the underlying timetable

    /**
     * Retrieves the stations from the underlying timetable.
     *
     * @return the stations
     */
    @Override
    public Stations stations() {
        return underlying.stations();
    }

    /**
     * Retrieves the station aliases from the underlying timetable.
     *
     * @return the station aliases
     */
    @Override
    public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    /**
     * Retrieves the platforms from the underlying timetable.
     *
     * @return the platforms
     */
    @Override
    public Platforms platforms() {
        return underlying.platforms();
    }

    /**
     * Retrieves the routes from the underlying timetable.
     *
     * @return the routes
     */
    @Override
    public Routes routes() {
        return underlying.routes();
    }

    /**
     * Retrieves the transfers from the underlying timetable.
     *
     * @return the transfers
     */
    @Override
    public Transfers transfers() {
        return underlying.transfers();
    }

    /**
     * Retrieves the station ID corresponding to the given stop ID from the underlying timetable.
     *
     * @param stopId the stop ID
     * @return the station ID
     */
    @Override
    public int stationId(int stopId) {
        return underlying.stationId(stopId);
    }

    /**
     * Retrieves the platform name corresponding to the given stop ID from the underlying timetable.
     *
     * @param stopId the stop ID
     * @return the platform name
     */
    @Override
    public String platformName(int stopId) {
        return underlying.platformName(stopId);
    }

    /**
     * Checks if the given stop ID corresponds to a station in the underlying timetable.
     *
     * @param stopId the stop ID
     * @return true if the stop ID corresponds to a station, false otherwise
     */
    @Override
    public boolean isStationId(int stopId) {
        return underlying.isStationId(stopId);
    }

    /**
     * Checks if the given stop ID corresponds to a platform in the underlying timetable.
     *
     * @param stopId the stop ID
     * @return true if the stop ID corresponds to a platform, false otherwise
     */
    @Override
    public boolean isPlatformId(int stopId) {
        return underlying.isPlatformId(stopId);
    }
}
