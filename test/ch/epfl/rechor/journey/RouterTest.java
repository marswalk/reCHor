package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

public class RouterTest {

    /**
     * Finds the ID of a station by its name.
     *
     * @param stations    the stations data
     * @param stationName the name of the station to find
     * @return the ID of the station
     * @throws IllegalArgumentException if no station with the given name is found
     */
    static int stationId(Stations stations, String stationName) {
        for (int id = 0; id < stations.size(); id++) {
            if (stations.name(id).equals(stationName)) {
                return id;
            }
        }
        throw new IllegalArgumentException("Station not found: " + stationName);
    }

    public static void main(String[] args) throws IOException {
        long tStart = System.nanoTime();

        TimeTable timeTable =
                new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int depStationId = stationId(stations, "Ecublens VD, EPFL");
        int arrStationId = stationId(stations, "Gruyères");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);
        Journey journey = JourneyExtractor
                .journeys(profile, depStationId)
                .get(32);
        System.out.println(JourneyIcalConverter.toIcalendar(journey));

        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
}
