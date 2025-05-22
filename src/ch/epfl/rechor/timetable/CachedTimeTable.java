package ch.epfl.rechor.timetable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a timetable with cached date-dependent data (connections and trips).
 * Improves performance by avoiding reloading data when the same date is requested multiple times.
 */
public class CachedTimeTable implements TimeTable {
    private final TimeTable underlying;
    
    // Simple cache for connections and trips
    private LocalDate cachedConnectionsDate;
    private Connections cachedConnections;
    
    private LocalDate cachedTripsDate;
    private Trips cachedTrips;
    
    // Statistics counters - simple integers since we only care about general stats
    private int connectionCacheHits = 0;
    private int connectionCacheMisses = 0;
    private int tripCacheHits = 0;
    private int tripCacheMisses = 0;

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
     * Gets connections for the specified date, using the cache if available.
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
            connectionCacheHits++;
            return cachedConnections;
        }

        // Cache miss - load connections from underlying timetable
        long startTime = System.currentTimeMillis();
        cachedConnections = underlying.connectionsFor(date);
        cachedConnectionsDate = date;
        connectionCacheMisses++;
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Loaded " + cachedConnections.size() + 
                         " connections for " + date + " in " + duration + "ms");

        return cachedConnections;
    }

    /**
     * Gets trips for the specified date, using the cache if available.
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
            tripCacheHits++;
            return cachedTrips;
        }

        // Cache miss - load trips from underlying timetable
        long startTime = System.currentTimeMillis();
        cachedTrips = underlying.tripsFor(date);
        cachedTripsDate = date;
        tripCacheMisses++;
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Loaded trips for " + date + " in " + duration + "ms");

        return cachedTrips;
    }

    // All other methods delegate to the underlying timetable

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
