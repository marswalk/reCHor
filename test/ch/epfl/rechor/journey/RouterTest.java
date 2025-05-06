package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.timetable.Connections;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class RouterTest {

    static int stationId(Stations stations, String stationName) {
        // Parcourir pour chercher l’index
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                System.out.println("Station: " + stationName + " index trouvé : " + i);
                return i;
            }
        }
        return -1;

    }

    public static void main(String[] args) throws IOException {
        long tStart = System.nanoTime();

        TimeTable timeTable =
                new CachedTimeTable(FileTimeTable.in(Path.of("timetable/rechor_timetable_16/timetable")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.APRIL, 15);
        int depStationId = stationId(stations, "Ecublens VD, EPFL");
        int arrStationId = stationId(stations, "Oberriet SG, Rathaus");

        // Connections used for debug - print basic inputs
        Connections conns = timeTable.connectionsFor(date);
        System.out.printf("Debug: date=%s depStationId=%d arrStationId=%d stations=%d connections=%d%n",
                date, depStationId, arrStationId, stations.size(), conns.size());

        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);

        List<Journey> journeys;
        try {
            journeys = JourneyExtractor.journeys(profile, depStationId);
            System.out.printf("Debug: journeys extracted=%d%n", journeys.size());
        } catch (Exception e) {
            System.err.printf("Debug: error extracting journeys for depStationId=%d%n", depStationId);
            e.printStackTrace();
            return;
        }

        Journey journey = journeys.get(32);
        System.out.println(JourneyIcalConverter.toIcalendar(journey));

        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
}
