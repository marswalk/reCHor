package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class JourneyExtractorTest {

    private Profile profile;
    private int depStationId;
    private int arrStationId;
    private TimeTable timeTable;
    private LocalDate date;

    @BeforeEach
    public void setUp() throws IOException {
        System.out.println("\n==== Setting up ====");
        // Set up the environment with the timetable data and journey parameters
        System.out.println("Loading timetable data...");
        timeTable = FileTimeTable.in(Path.of("timetable"));
        System.out.println("✓ Loaded timetable successfully");

        date = LocalDate.of(2025, Month.MARCH, 18);
        arrStationId = 11486; // Gruyères
        depStationId = 7872;  // Ecublens VD, EPFL
        System.out.println("Test parameters: date=" + date + ", departure=" + depStationId +
                " (" + timeTable.stations().name(depStationId) + "), arrival=" +
                arrStationId + " (" + timeTable.stations().name(arrStationId) + ")");

        // Read the profile from the provided file
        System.out.println("Reading profile from file...");
        profile = readProfile(timeTable, date, arrStationId);
        System.out.println("✓ Profile loaded successfully");
    }

    //iterate through every station id to find the id of Renens VD, gare
    private int findAnyStationId(String stationName) {
        for (int i = 0; i < timeTable.stations().size(); i++) {
            if (timeTable.stations().name(i).equals(stationName)) {
                System.out.println("Found " + stationName + " at station ID: " + i);
                return i;
            }
        }
        return -1; // Not found
    }

    @Test
    public void testJourneyExtraction() throws IOException {
        System.out.println("\n==== Starting Journey Extraction Test ====");

        // Extract journeys using JourneyExtractor
        System.out.println("Extracting journeys...");
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
        System.out.println("Found " + journeys.size() + " journeys");

        // Basic verification
        assertNotNull(journeys, "Extracted journeys should not be null");
        assertFalse(journeys.isEmpty(), "There should be at least one journey extracted");
        assertTrue(journeys.size() >= 33, "Expected at least 33 journeys, got " + journeys.size());
    }

    @Test
    public void testStartingOnFoot() throws IOException {
        System.out.println("\n==== Starting Test for Journey Requiring Walking Leg First ====");

        depStationId = findAnyStationId("Renens VD, gare"); // Renens VD, gare

        System.out.println("Test parameters: date=" + date + ", departure=" + depStationId +
                " (" + timeTable.stations().name(depStationId) + "), arrival=" +
                arrStationId + " (" + timeTable.stations().name(arrStationId) + ")");

        // Extract journeys using JourneyExtractor
        System.out.println("Extracting journeys...");
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
        System.out.println("Found " + journeys.size() + " journeys");

        // Verify if the first journey has a walking leg
        Journey firstJourney = journeys.get(0);
        System.out.println("Examining journey at index 0:");
        System.out.println("- Departure: " + firstJourney.depTime() + " from " + firstJourney.depStop().name() +
                (firstJourney.depStop().platformName() != null ? " (" + firstJourney.depStop().platformName() + ")" : ""));
        System.out.println("- Arrival: " + firstJourney.arrTime() + " at " + firstJourney.arrStop().name() +
                (firstJourney.arrStop().platformName() != null ? " (" + firstJourney.arrStop().platformName() + ")" : ""));
        System.out.println("- Legs: " + firstJourney.legs().size());
        for (int i = 0; i < firstJourney.legs().size(); i++) {
            Journey.Leg leg = firstJourney.legs().get(i);
            if (leg instanceof Journey.Leg.Transport t) {
                System.out.println("  Leg " + i + " (Transport): " + t.depTime() + " " +
                        t.depStop().name() + " → " + t.arrTime() + " " +
                        t.arrStop().name() + " (" + t.route() + " to " + t.destination() + ")");
            } else if (leg instanceof Journey.Leg.Foot f) {
                System.out.println("  Leg " + i + " (Foot): " + f.depTime() + " " +
                        f.depStop().name() + " → " + f.arrTime() + " " +
                        f.arrStop().name() + (f.isTransfer() ? " (Transfer)" : ""));
            }
        }
        assertInstanceOf(Journey.Leg.Foot.class, firstJourney.legs().getFirst(), "The first leg of the first journey should be a walking leg");
    }

    @Test
    public void testJourney32Extraction() throws IOException {
        System.out.println("\n==== Starting Journey Extraction Test ====");

        try {
            // Extract journeys using JourneyExtractor
            System.out.println("Extracting journeys...");
            List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
            System.out.println("Found " + journeys.size() + " journeys");

            // Basic verification
            assertNotNull(journeys, "Extracted journeys should not be null");
            assertFalse(journeys.isEmpty(), "There should be at least one journey extracted");
            assertTrue(journeys.size() >= 33, "Expected at least 33 journeys, got " + journeys.size());

            // Verify journey at index 32 (as mentioned in the example)
            Journey journey = journeys.get(32);
            System.out.println("Examining journey at index 32:");
            System.out.println("- Departure: " + journey.depTime() + " from " + journey.depStop().name() +
                    (journey.depStop().platformName() != null ? " (" + journey.depStop().platformName() + ")" : ""));
            System.out.println("- Arrival: " + journey.arrTime() + " at " + journey.arrStop().name() +
                    (journey.arrStop().platformName() != null ? " (" + journey.arrStop().platformName() + ")" : ""));
            System.out.println("- Legs: " + journey.legs().size());

            for (int i = 0; i < journey.legs().size(); i++) {
                Journey.Leg leg = journey.legs().get(i);
                if (leg instanceof Journey.Leg.Transport t) {
                    System.out.println("  Leg " + i + " (Transport): " + t.depTime() + " " +
                            t.depStop().name() + " → " + t.arrTime() + " " +
                            t.arrStop().name() + " (" + t.route() + " to " + t.destination() + ")");
                } else if (leg instanceof Journey.Leg.Foot f) {
                    System.out.println("  Leg " + i + " (Foot): " + f.depTime() + " " +
                            f.depStop().name() + " → " + f.arrTime() + " " +
                            f.arrStop().name() + (f.isTransfer() ? " (Transfer)" : ""));
                }
            }

            System.out.println("You should check if this journey matches the one shown on Schinz's screenshot!");
            assertNotNull(journey, "Journey at index 32 should not be null");

            // Verify journey date
            assertEquals(date, journey.depTime().toLocalDate(), "Journey should be on the specified date");
            // Verify journey structure - check if legs exist
            assertFalse(journey.legs().isEmpty(), "Journey should have at least one leg");

            // Verify start and end stations
            System.out.println("Verifying start and end stations...");
            int firstStationId = getFirstStationId(journey, timeTable);
            int lastStationId = getLastStationId(journey, timeTable);
            System.out.println("- First station ID: " + firstStationId + " (expected " + depStationId + ")");
            System.out.println("- Last station ID: " + lastStationId + " (expected " + arrStationId + ")");

            assertEquals(depStationId, firstStationId, "Journey should start at Ecublens VD, EPFL");
            assertEquals(arrStationId, lastStationId, "Journey should end at Gruyères");

            // Verify alternating leg types (transport and walking)
            verifyAlternatingLegTypes(journey);

            // Check journey times
            assertTrue(journey.depTime().isBefore(journey.arrTime()),
                    "Departure time should be before arrival time");
        } catch (Exception e) {
            System.out.println("ERROR: Exception occurred during journey extraction");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to fail the test
        }
    }

    @Test
    public void testIcalConversionForAllJourneysIsInCorrectFormat() throws IOException {
        System.out.println("\n==== Starting iCalendar Conversion Test ====");


        // Extract journeys using JourneyExtractor
        System.out.println("Extracting journeys...");
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
        System.out.println("Found " + journeys.size() + " journeys");

        // Verify iCalendar conversion for each journey
        for (Journey journey : journeys) {
            String icalEvent = JourneyIcalConverter.toIcalendar(journey);
            System.out.println("iCalendar event for journey " + journey.depTime() + " → " + journey.arrTime() + ":");
            System.out.println(icalEvent);
            assertNotNull(icalEvent, "iCalendar event should not be null");
            assertTrue(icalEvent.contains("BEGIN:VCALENDAR"), "iCalendar should start with BEGIN:VCALENDAR");
            assertTrue(icalEvent.contains("VERSION:2.0"), "iCalendar should contain VERSION:2.0");
            assertTrue(icalEvent.contains("PRODID:ReCHor"), "iCalendar should contain PRODID:ReCHor");
            assertTrue(icalEvent.contains("BEGIN:VEVENT"), "iCalendar should contain BEGIN:VEVENT");
            assertTrue(icalEvent.contains("END:VEVENT"), "iCalendar should contain END:VEVENT");
            assertTrue(icalEvent.contains("END:VCALENDAR"), "iCalendar should end with END:VCALENDAR");
        }
    }

    @Test
    public void testJourney32MatchesExactlyWithICalExampleIn2_1_8() throws IOException {
        System.out.println("\n==== Starting Journey 32 iCalendar Test ====");

        // Extract journeys using JourneyExtractor
        System.out.println("Extracting journeys...");
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
        System.out.println("Found " + journeys.size() + " journeys");

        // Verify journey at index 32
        Journey journey = journeys.get(32);
        System.out.println("Examining journey at index 32:");
        System.out.println("- Departure: " + journey.depTime() + " from " + journey.depStop().name() +
                (journey.depStop().platformName() != null ? " (" + journey.depStop().platformName() + ")" : ""));
        System.out.println("- Arrival: " + journey.arrTime() + " at " + journey.arrStop().name() +
                (journey.arrStop().platformName() != null ? " (" + journey.arrStop().platformName() + ")" : ""));
        System.out.println("- Legs: " + journey.legs().size());

        // Convert to iCalendar format
        String icalEvent = JourneyIcalConverter.toIcalendar(journey);
        System.out.println("iCalendar event for journey 32:");
        System.out.println(icalEvent);

        // Check if the iCalendar event matches the expected output
        assertNotNull(icalEvent, "iCalendar event should not be null");

        // Check DESCRIPTION matches exactly
        String[] lines = icalEvent.split("\r?\n");
        int lineNumber = 9;
        assertEquals("DESCRIPTION:16h13 Ecublens VD, EPFL → Renens VD, gare (arr. 16h19)\\ntrajet ", lines[lineNumber++]);
        assertEquals(" à pied (3 min)\\n16h26 Renens VD (voie 4) → Lausanne (arr. 16h33 voie 5)\\nc", lines[lineNumber++]);
        assertEquals(" hangement (5 min)\\n16h40 Lausanne (voie 1) → Romont FR (arr. 17h13 voie 2)", lines[lineNumber++]);
        assertEquals(" \\nchangement (3 min)\\n17h22 Romont FR (voie 1) → Bulle (arr. 17h41 voie 2)", lines[lineNumber++]);
        assertEquals(" \\nchangement (3 min)\\n17h50 Bulle (voie 4) → Gruyères (arr. 17h57 voie 2)", lines[lineNumber++]);

    }

    // Helper method to read profile from the provided text file
    private Profile readProfile(TimeTable timeTable, LocalDate date, int arrStationId) throws IOException {
        Path path = Path.of("test/ch/epfl/rechor/journey/"+"profile_"  + date +  "_"  + arrStationId +  ".txt");
        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB = new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(","))
                    frontB.add(Long.parseLong(t, 16));
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }

    // Helper method to get the first station ID of a journey
    private int getFirstStationId(Journey journey, TimeTable timeTable) {
        // Get the name of the departure stop
        String stopName = journey.depStop().name();
        // Find the station ID by name using the stations interface
        for (int i = 0; i < timeTable.stations().size(); i++) {
            if (timeTable.stations().name(i).equals(stopName)) {
                return i;
            }
        }

        return -1; // Not found
    }

    // Helper method to get the last station ID of a journey
    private int getLastStationId(Journey journey, TimeTable timeTable) {
        // Get the name of the arrival stop
        String stopName = journey.arrStop().name();
        // Find the station ID by name
        for (int i = 0; i < timeTable.stations().size(); i++) {
            if (timeTable.stations().name(i).equals(stopName)) {
                return i;
            }
        }
        return -1; // Not found
    }

    // Helper method to verify alternating leg types
    private void verifyAlternatingLegTypes(Journey journey) {
        for (int i = 0; i < journey.legs().size() - 1; i++) {
            if (journey.legs().get(i) instanceof Journey.Leg.Transport) {
                assertTrue(journey.legs().get(i + 1) instanceof Journey.Leg.Foot,
                        "Transport leg should be followed by a walking leg");
            } else {
                assertTrue(journey.legs().get(i + 1) instanceof Journey.Leg.Transport,
                        "Walking leg should be followed by a transport leg");
            }
        }
    }


}
