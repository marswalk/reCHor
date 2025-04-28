package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Router and CachedTimeTable classes.
 */
public class RouterTest {

    private TimeTable timeTable;
    private LocalDate date;
    private int depStationId;  // Ecublens VD, EPFL
    private int arrStationId;  // Gruyères
    private Router router;

    @BeforeEach
    public void setup() throws IOException {
        // Use CachedTimeTable to wrap the FileTimeTable
        timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));
        date = LocalDate.of(2025, Month.MARCH, 18); // Tuesday
        depStationId = stationId(timeTable.stations(), "Ecublens VD, EPFL");
        arrStationId = stationId(timeTable.stations(), "Gruyères");
        router = new Router(timeTable);
    }

    @Test
    public void testRouterCreation() {
        assertNotNull(router);
        assertThrows(NullPointerException.class, () -> new Router(null));
    }

    @Test
    public void testProfileMethod() {
        assertThrows(NullPointerException.class, () -> router.profile(null, arrStationId));

        Profile profile = router.profile(date, arrStationId);
        assertNotNull(profile);
        assertEquals(timeTable, profile.timeTable());
        assertEquals(date, profile.date());
        assertEquals(arrStationId, profile.arrStationId());

        // Check that the profile contains data for the departure station
        ParetoFront frontForDep = profile.forStation(depStationId);
        assertNotNull(frontForDep);
//        assertFalse(frontForDep.isEmpty(), "Profile should contain journeys from departure station");
    }

    @Test
    public void testCachedTimeTable() throws IOException {
        // Create a new TimeTable to test caching behavior
        TimeTable originalTimeTable = FileTimeTable.in(Path.of("timetable"));
        TimeTable cachedTimeTable = new CachedTimeTable(originalTimeTable);

        // First call should load the data
        Connections connections1 = cachedTimeTable.connectionsFor(date);
        Trips trips1 = cachedTimeTable.tripsFor(date);

        assertNotNull(connections1);
        assertNotNull(trips1);

        // Second call with the same date should use cached data
        Connections connections2 = cachedTimeTable.connectionsFor(date);
        Trips trips2 = cachedTimeTable.tripsFor(date);

        assertSame(connections1, connections2, "Should return the same cached Connections object");
        assertSame(trips1, trips2, "Should return the same cached Trips object");

        // Call with a different date should load new data
        LocalDate differentDate = date.plusDays(1);
        Connections connections3 = cachedTimeTable.connectionsFor(differentDate);
        Trips trips3 = cachedTimeTable.tripsFor(differentDate);

        assertNotSame(connections1, connections3, "Should return different Connections object for different date");
        assertNotSame(trips1, trips3, "Should return different Trips object for different date");
    }

    @Test
    public void testJourney32MatchesExpected() throws IOException {
        // This test reproduces the one from the project guide
        long tStart = System.nanoTime();

        Profile profile = router.profile(date, arrStationId);
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);

        assertTrue(journeys.size() > 32, "There should be at least 33 journeys");

        Journey journey = journeys.get(32);
        String icalEvent = JourneyIcalConverter.toIcalendar(journey);

        // Verify the journey properties
        assertEquals("Ecublens VD, EPFL", journey.depStop().name());
        assertEquals("Gruyères", journey.arrStop().name());

        // Check that the iCal contains the expected components
        assertNotNull(icalEvent);
        assertTrue(icalEvent.contains("BEGIN:VCALENDAR"));
        assertTrue(icalEvent.contains("VERSION:2.0"));
        assertTrue(icalEvent.contains("PRODID:ReCHor"));
        assertTrue(icalEvent.contains("BEGIN:VEVENT"));

        // Check for the specific description as described in the project guide
        assertTrue(icalEvent.contains("DESCRIPTION:16h13 Ecublens VD, EPFL → Renens VD, gare"));
        assertTrue(icalEvent.contains("trajet à pied"));
        assertTrue(icalEvent.contains("Renens VD (voie 4) → Lausanne"));
        assertTrue(icalEvent.contains("Lausanne (voie 1) → Romont FR"));
        assertTrue(icalEvent.contains("Romont FR (voie 1) → Bulle"));
        assertTrue(icalEvent.contains("Bulle (voie 4) → Gruyères"));

        // Measure performance
        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
        assertTrue(elapsed < 10.0, "Performance should be reasonable");
    }

    // Helper method to find a station ID by name
    private int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Station not found: " + stationName);
    }
}