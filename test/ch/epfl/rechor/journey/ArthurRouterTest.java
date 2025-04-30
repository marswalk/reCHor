package ch.epfl.rechor.journey;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

/**
 * Test manuel de l'algorithme CSA et de l'export iCalendar.
 * Recherche un trajet direct (sans changements à pied)
 * entre EPFL et Renens VD, le 1er avril 2025.
 */
public final class ArthurRouterTest {
    static int stationId(TimeTable tt, String stationName) {
        var stations = tt.stations();
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Station introuvable : " + stationName);
    }

    public static void main(String[] args) throws IOException {
        long tStart = System.nanoTime();

        TimeTable timeTable =
                new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));

        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int depStationId = stationId(timeTable, "Ecublens VD, EPFL");
        int arrStationId = stationId(timeTable, "Gruyères");
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


