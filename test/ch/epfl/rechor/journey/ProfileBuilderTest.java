package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class ProfileBuilderTest {

    private TimeTable timeTable;
    private LocalDate date;
    private int arrStationId;
    private Profile.Builder builder;
    private long packedValue;
    private long tripPackedValue;

    @BeforeEach
    public void setUp() throws IOException {
        System.out.println("\n==== SETTING UP ====");

        // Set up the TimeTable from the provided timetable directory
        System.out.println("[SETUP] Loading timetable data...");
        timeTable = FileTimeTable.in(Path.of("timetable"));
        date = LocalDate.of(2025, Month.MARCH, 18);
        arrStationId = 11486; // Gruyères
        System.out.println("✓ Loaded timetable successfully");
        System.out.println("✓ Test date: " + date);
        System.out.println("✓ Arrival station ID: " + arrStationId + " (Gruyères)");

        // Create a Profile.Builder
        System.out.println("[SETUP] Creating Profile.Builder...");
        builder = new Profile.Builder(timeTable, date, arrStationId);
        System.out.println("✓ Profile.Builder created successfully");

        // Example values for testing
        packedValue = 0x552b298408159611L; // Example value from documentation
        tripPackedValue = 0xFEDCBA0987654321L; // Sample value
    }

    @Test
    @DisplayName("Test initial state of Profile.Builder")
    public void testInitialState() {
        System.out.println("\n==== TEST: INITIAL STATE ====");

        // Test initial state - forStation should return null for all stations
        System.out.println("[TEST] Checking initial state for stations...");
        ParetoFront.Builder initialStationBuilder = builder.forStation(0);
        System.out.println("Initial station builder for station 0: " + initialStationBuilder);
        assertNull(initialStationBuilder, "Initial station builder should be null");
        System.out.println("✓ Initial station builder is null as expected");

        // Test initial state - forTrip should return null for all trips
        System.out.println("[TEST] Checking initial state for trips...");
        ParetoFront.Builder initialTripBuilder = builder.forTrip(0);
        System.out.println("Initial trip builder for trip 0: " + initialTripBuilder);
        assertNull(initialTripBuilder, "Initial trip builder should be null");
        System.out.println("✓ Initial trip builder is null as expected");
    }

    @Test
    @DisplayName("Test setting and retrieving station ParetoFront builders")
    public void testStationBuilders() {
        System.out.println("\n==== TEST: STATION BUILDERS ====");

        // Create and set ParetoFront.Builder for a station
        System.out.println("[TEST] Setting ParetoFront.Builder for station 0...");
        ParetoFront.Builder stationFrontBuilder = new ParetoFront.Builder();
        System.out.println("Adding packed value to ParetoFront: 0x" + Long.toHexString(packedValue).toUpperCase());
        stationFrontBuilder.add(packedValue);

        System.out.println("Setting builder for station 0");
        builder.setForStation(0, stationFrontBuilder);

        // Verify that the builder was correctly set
        ParetoFront.Builder retrievedBuilder = builder.forStation(0);
        System.out.println("Retrieved builder for station 0: " + retrievedBuilder);
        assertSame(stationFrontBuilder, retrievedBuilder,
                "forStation should return the exact builder instance that was set");
        System.out.println("✓ Retrieved builder matches the one we set");
    }

    @Test
    @DisplayName("Test setting and retrieving trip ParetoFront builders")
    public void testTripBuilders() {
        System.out.println("\n==== TEST: TRIP BUILDERS ====");

        // Create and set ParetoFront.Builder for a trip
        System.out.println("[TEST] Setting ParetoFront.Builder for trip 0...");
        ParetoFront.Builder tripFrontBuilder = new ParetoFront.Builder();
        System.out.println("Adding packed value to trip ParetoFront: 0x" +
                Long.toHexString(tripPackedValue).toUpperCase());
        tripFrontBuilder.add(tripPackedValue);

        System.out.println("Setting builder for trip 0");
        builder.setForTrip(0, tripFrontBuilder);

        // Verify that the trip builder was correctly set
        ParetoFront.Builder retrievedTripBuilder = builder.forTrip(0);
        System.out.println("Retrieved builder for trip 0: " + retrievedTripBuilder);
        assertSame(tripFrontBuilder, retrievedTripBuilder,
                "forTrip should return the exact builder instance that was set");
        System.out.println("✓ Retrieved trip builder matches the one we set");
    }

    @Test
    @DisplayName("Test building a Profile from a Profile.Builder")
    public void testBuildProfile() {
        System.out.println("\n==== TEST: BUILD PROFILE ====");

        // Setup builder with station data
        ParetoFront.Builder stationFrontBuilder = new ParetoFront.Builder();
        stationFrontBuilder.add(packedValue);
        builder.setForStation(0, stationFrontBuilder);

        // Build the Profile
        System.out.println("[TEST] Building Profile from builder...");
        Profile profile = builder.build();
        System.out.println("✓ Profile built successfully");

        // Basic verification
        assertNotNull(profile, "Built profile should not be null");
    }

    @Test
    @DisplayName("Test that Profile has the correct properties")
    public void testProfileProperties() {
        System.out.println("\n==== TEST: PROFILE PROPERTIES ====");

        // Setup builder with station data
        ParetoFront.Builder stationFrontBuilder = new ParetoFront.Builder();
        stationFrontBuilder.add(packedValue);
        builder.setForStation(0, stationFrontBuilder);

        // Build the profile
        Profile profile = builder.build();

        // Verify that the Profile has the correct properties
        System.out.println("[TEST] Verifying Profile properties...");

//        System.out.println("Expected timeTable: " + timeTable);
//        System.out.println("Actual timeTable: " + profile.timeTable());
        assertEquals(timeTable, profile.timeTable(), "Profile should have the correct timeTable");
        System.out.println("✓ TimeTable is correctly set");

        System.out.println("Expected date: " + date);
        System.out.println("Actual date: " + profile.date());
        assertEquals(date, profile.date(), "Profile should have the correct date");
        System.out.println("✓ Date is correctly set");

        System.out.println("Expected arrival station ID: " + arrStationId);
        System.out.println("Actual arrival station ID: " + profile.arrStationId());
        assertEquals(arrStationId, profile.arrStationId(), "Profile should have the correct arrival station ID");
        System.out.println("✓ Arrival station ID is correctly set");
    }

    @Test
    @DisplayName("Test that ParetoFront contains the expected values")
    public void testParetoFrontContents() {
        System.out.println("\n==== TEST: PARETO FRONT CONTENTS ====");

        // Setup builder with station data
        ParetoFront.Builder stationFrontBuilder = new ParetoFront.Builder();
        stationFrontBuilder.add(packedValue);
        builder.setForStation(0, stationFrontBuilder);

        // Build the profile
        Profile profile = builder.build();

        // Verify that the ParetoFront for station 0 is not EMPTY
        System.out.println("[TEST] Verifying ParetoFront for station 0...");
        ParetoFront front = profile.forStation(0);
        System.out.println("ParetoFront for station 0: " + front);
        System.out.println("Empty ParetoFront: " + ParetoFront.EMPTY);
        assertNotSame(ParetoFront.EMPTY, front, "ParetoFront for station 0 should not be EMPTY");
        System.out.println("✓ ParetoFront for station 0 is not EMPTY");

        // Verify that the ParetoFront for station 0 contains the added value
        System.out.println("[TEST] Checking if ParetoFront contains the expected value...");
        List<Long> foundValues = new ArrayList<>();
        System.out.println("Looking for value: 0x" + Long.toHexString(packedValue).toUpperCase());
        System.out.println("Values in ParetoFront:");
        front.forEach(value -> {
            System.out.println("  - 0x" + Long.toHexString(value).toUpperCase());
            foundValues.add(value);
        });
        boolean containsValue = foundValues.contains(packedValue);
        System.out.println("Contains expected value: " + containsValue);
        assertTrue(containsValue, "ParetoFront should contain the added value");
        System.out.println("✓ ParetoFront contains the expected value");
    }

    @Test
    @DisplayName("Test that unset stations have EMPTY ParetoFronts")
    public void testEmptyParetoFronts() {
        System.out.println("\n==== TEST: EMPTY PARETO FRONTS ====");

        // Setup builder with station data (only for station 0)
        ParetoFront.Builder stationFrontBuilder = new ParetoFront.Builder();
        stationFrontBuilder.add(packedValue);
        builder.setForStation(0, stationFrontBuilder);

        // Build the profile
        Profile profile = builder.build();

        // Verify that unset stations have EMPTY ParetoFronts
        System.out.println("[TEST] Checking unset stations...");
        ParetoFront unsetFront = profile.forStation(1);
        System.out.println("ParetoFront for unset station 1: " + unsetFront);
        System.out.println("Empty ParetoFront: " + ParetoFront.EMPTY);
        assertSame(ParetoFront.EMPTY, unsetFront, "ParetoFront for unset station should be EMPTY");
        System.out.println("✓ ParetoFront for unset station is EMPTY");
    }

    @Test
    @DisplayName("Test exception handling for invalid indices")
    public void testExceptionHandling() {
        System.out.println("\n==== TEST: EXCEPTION HANDLING ====");

        // Test exception handling for invalid indices
        System.out.println("[TEST] Testing exception handling for invalid indices...");
        ParetoFront.Builder dummyBuilder = new ParetoFront.Builder();

        System.out.println("Testing negative station index in forStation...");
        assertThrows(IndexOutOfBoundsException.class, () -> builder.forStation(-1),
                "Negative station index should throw IndexOutOfBoundsException");
        System.out.println("✓ Caught expected exception for forStation(-1)");

        System.out.println("Testing negative station index in setForStation...");
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setForStation(-1, dummyBuilder),
                "Negative station index should throw IndexOutOfBoundsException");
        System.out.println("✓ Caught expected exception for setForStation(-1, builder)");

        System.out.println("Testing negative trip index in forTrip...");
        assertThrows(IndexOutOfBoundsException.class, () -> builder.forTrip(-1),
                "Negative trip index should throw IndexOutOfBoundsException");
        System.out.println("✓ Caught expected exception for forTrip(-1)");

        System.out.println("Testing negative trip index in setForTrip...");
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setForTrip(-1, dummyBuilder),
                "Negative trip index should throw IndexOutOfBoundsException");
        System.out.println("✓ Caught expected exception for setForTrip(-1, builder)");
    }

    @Test
    @DisplayName("Test connections and trips methods")
    public void testConnectionsAndTrips() {
        System.out.println("\n==== TEST: CONNECTIONS AND TRIPS ====");

        // Build a profile
        Profile profile = builder.build();

        // Test connections and trips methods of Profile
        System.out.println("[TEST] Testing connections and trips methods...");

        System.out.println("Profile connections: " + profile.connections());
        assertNotNull(profile.connections(), "Profile connections should not be null");
        System.out.println("✓ Profile connections is not null");

        System.out.println("Profile trips: " + profile.trips());
        assertNotNull(profile.trips(), "Profile trips should not be null");
        System.out.println("✓ Profile trips is not null");
    }
}
