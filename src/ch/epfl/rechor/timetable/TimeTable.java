package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Represents a complete public transport timetable
 */
public interface TimeTable {

    /**
     * @return indexed stations data
     */
    Stations stations();

    /**
     * @return indexed station aliases
     */
    StationAliases stationAliases();

    /**
     * @return indexed platforms data
     */
    Platforms platforms();

    /**
     * @return indexed routes data
     */
    Routes routes();

    /**
     * @return indexed transfers data
     */
    Transfers transfers();

    /**
     * @param date date to query
     * @return trips active on given date
     */
    Trips tripsFor(LocalDate date);

    /**
     * @param date date to query
     * @return connections active on given date
     */
    Connections connectionsFor(LocalDate date);

    // Default methods
    /**
     * @param stopId stop ID to check
     * @return true if ID represents a station
     */
    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    /**
     * @param stopId stop ID to check
     * @return true if ID represents a platform
     */
    default boolean isPlatformId(int stopId) {
        return !isStationId(stopId);
    }

    /**
     * @param stopId stop ID to convert
     * @return station ID for this stop (same if station)
     */
    default int stationId(int stopId) {
        return isStationId(stopId) ? stopId : platforms().stationId(stopId - stations().size());
    }

    /**
     * @param stopId stop ID to check
     * @return platform name or null for stations
     */
    default String platformName(int stopId) {
        return isStationId(stopId) ? null :
                platforms().name(stopId - stations().size());
    }
}