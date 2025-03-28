package ch.epfl.rechor.journey;

import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SachaProfileTest {
    private final Path timetablePath = Path.of("timetable");
    private final LocalDate testDate = LocalDate.of(2025, 3, 18);
    private final int stationId = 11486;


    @Test
    void recordConstructorStoresFieldsCorrectly() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        ParetoFront pf = new ParetoFront.Builder().add(0).build(); // Dummy front
        List<ParetoFront> fronts = List.of(pf);
        Profile profile = new Profile(tt, testDate, stationId, fronts);

        // Verify all values are stored and returned correctly
        assertEquals(tt, profile.timeTable());
        assertEquals(testDate, profile.date());
        assertEquals(stationId, profile.arrStationId());
        assertEquals(fronts, profile.stationFront());
    }

    @Test
    void stationFrontIsImmutable() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        List<ParetoFront> mutable = new ArrayList<>();
        mutable.add(ParetoFront.EMPTY); // Add empty front

        Profile profile = new Profile(tt, testDate, stationId, mutable);

        // Try mutating the list returned by Profile
        assertThrows(UnsupportedOperationException.class, () -> {
            profile.stationFront().add(ParetoFront.EMPTY);
        });
    }
    @Test
    void forStationReturnsCorrectFront() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        ParetoFront pf1 = new ParetoFront.Builder().add(0).build(); // front station 0
        ParetoFront pf2 = new ParetoFront.Builder().add(0).build(); // front station 1

        Profile profile = new Profile(tt, testDate, stationId, List.of(pf1, pf2));
        assertEquals(pf1, profile.forStation(0)); // check first station
        assertEquals(pf2, profile.forStation(1)); // check second station
    }

    @Test
    void forStationThrowsIfOutOfBounds() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile profile = new Profile(tt, testDate, stationId, List.of(ParetoFront.EMPTY));

        assertThrows(IndexOutOfBoundsException.class, () -> profile.forStation(-1)); // negative
        assertThrows(IndexOutOfBoundsException.class, () -> profile.forStation(1));  // past end
    }

    @Test
    void connectionsReturnsCorrectValue() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile profile = new Profile(tt, testDate, stationId, List.of(ParetoFront.EMPTY));

        Connections conns = profile.connections();
        assertNotNull(conns);                 // It should not be null
        assertTrue(conns.size() > 0);         // Real data expected to be non-empty
    }

    @Test
    void tripsReturnsCorrectValue() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile profile = new Profile(tt, testDate, stationId, List.of(ParetoFront.EMPTY));

        Trips trips = profile.trips();
        assertNotNull(trips);
        assertTrue(trips.size() > 0);
    }

    @Test
    void builderForStationAndSetForStationWorkCorrectly() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile.Builder builder = new Profile.Builder(tt, testDate, stationId);
        ParetoFront.Builder pfBuilder = new ParetoFront.Builder().add(0);
        builder.setForStation(0, pfBuilder);
        assertEquals(pfBuilder, builder.forStation(0));
    }

    @Test
    void builderForTripAndSetForTripWorkCorrectly() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile.Builder builder = new Profile.Builder(tt, testDate, stationId);
        int tripCount = tt.tripsFor(testDate).size();

        if (tripCount == 0) fail("Timetable has no trips, cannot test setForTrip");

        ParetoFront.Builder tripBuilder = new ParetoFront.Builder().add(0);
        builder.setForTrip(0, tripBuilder);
        assertEquals(tripBuilder, builder.forTrip(0));
    }

    @Test
    void builderThrowsOnInvalidIndexes() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile.Builder builder = new Profile.Builder(tt, testDate, stationId);

        Trips trips = tt.tripsFor(testDate);
        int tripCount = trips.size();
        Stations stations = tt.stations();
        int stationCount = stations.size();

        // Invalid station access
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setForStation(-1, new ParetoFront.Builder()));
        assertThrows(IndexOutOfBoundsException.class, () -> builder.forStation(stationCount + 1));

        // Invalid trip access
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setForTrip(-2, new ParetoFront.Builder()));
        assertThrows(IndexOutOfBoundsException.class, () -> builder.forTrip(tripCount + 1));
    }


    @Test
    void builderBuildCreatesProfileWithDefaultNulls() throws IOException {
        TimeTable tt = FileTimeTable.in(timetablePath);
        Profile.Builder builder = new Profile.Builder(tt, testDate, stationId);

        // Only set index 0, leave others null
        ParetoFront.Builder pfBuilder = new ParetoFront.Builder().add(0);
        builder.setForStation(0, pfBuilder);

        Profile profile = builder.build();

        // Index 0 contains built ParetoFront from builder
        assertNotNull(profile.forStation(0));

        // Index 1 must be EMPTY because it was not set
        assertNotNull(profile.forStation(1));
        assertEquals(ParetoFront.EMPTY, profile.forStation(1));
    }
    @Test
    void decodePackedParetoTupleCorrectly() {
        // Packed value from profile file
        String hex = "552b298408159611";
        long packed = Long.parseUnsignedLong(hex, 16);

        // Extract lowest 8 bits = number of intermediate stops
        int intermediateStops = (int) (packed & 0xFF);
        assertEquals(17, intermediateStops, "Expected 17 intermediate stops");

        // Extract bits 8–31 = connection index
        int connectionIndex = (int) ((packed >>> 8) & 0xFFFFFF);
        assertEquals(529814, connectionIndex, "Expected connection index 529814");

    }

}