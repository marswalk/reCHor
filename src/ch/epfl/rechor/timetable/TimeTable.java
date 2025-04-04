package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Represents a complete public transport timetable
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface TimeTable {

    /**
     * @return indexed stations data for the timetable
     */
    Stations stations();

    /**
     * @return indexed station aliases (same as stations() but with aliases)
     */
    StationAliases stationAliases();

    /**
     * @return indexed track/platforms data of the timetable
     */
    Platforms platforms();

    /**
     * @return indexed routes data of the schedule
     */
    Routes routes();

    /**
     * @return indexed transfers data of the schedule
     */
    Transfers transfers();

    /**
     * @param date date to query
     * @return trips active on given date (indexed by route)
     */
    Trips tripsFor(LocalDate date);

    /**
     * @param date date to query
     * @return connections active on given date （again indexed)
     */
    Connections connectionsFor(LocalDate date);

    // Default methods

    /**
     * Checks if the given stop ID represents a station (and not platform)
     *
     * <p>
     * Note:
     * If such an index is less than the number of stations existing in the timetable,
     * then it represents a station index. Otherwise, it represents a track or platform index,
     * which can be calculated by subtracting the number of stations existing in the timetable
     * from the stop index.
     * <p>
     * For example, if there are 1000 stations and 2000 tracks or platforms, stop index 500
     * represents station index 500, while stop index 1700 represents track/platform index 700.
     * </p>
     *
     * @param stopId stop ID to check
     * @return true if ID represents a station
     */
    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    /**
     * Checks if given stop ID represents a platform (and not station)
     *
     * <p>
     * Note:
     * If such an index is less than the number of stations existing in the timetable,
     * then it represents a station index. Otherwise, it represents a track or platform index,
     * which can be calculated by subtracting the number of stations existing in the timetable
     * from the stop index.
     * <p>
     * For example, if there are 1000 stations and 2000 tracks or platforms, stop index 500
     * represents station index 500, while stop index 1700 represents track/platform index 700.
     * </p>
     *
     * @param stopId stop ID to check
     * @return true if ID represents a platform
     */
    default boolean isPlatformId(int stopId) {
        return !isStationId(stopId);
    }

    /**
     * Gets the station ID for the given stop ID
     *
     * <p>
     * Note:
     * stationId is the same as stopId if it is a station, otherwise (if it is a platform/track), it therefore
     * represents a track/platform index which can be calculated by subtracting the number of stations existing
     * in the timetable from the stop index (stopId).
     * </p>
     *
     * @param stopId stop ID to convert
     * @return station ID for this stop (same if station)
     */
    default int stationId(int stopId) {
        return isStationId(stopId) ? stopId : platforms().stationId(stopId - stations().size());
    }

    /**
     * @param stopId stop ID to check
     * @return platform/track name or null for stations
     */
    default String platformName(int stopId) {
        return isStationId(stopId) ? null :
                platforms().name(stopId - stations().size());
    }
}