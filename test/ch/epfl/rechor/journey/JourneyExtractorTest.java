package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class JourneyExtractorTest {

    @Test
    public void testJourneyExtractionMatchesProfileFile() throws IOException {
        System.out.println("==== Starting Journey Extraction Test ====");

        // Set up the environment with the timetable data and journey parameters
        TimeTable timeTable = FileTimeTable.in(Path.of("timetable"));
        System.out.println("TimeTable loaded successfully");

        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrStationId = 11486; // Gruyères
        int depStationId = 7872;  // Ecublens VD, EPFL
        System.out.println("Test parameters: date=" + date + ", departure=" + depStationId +
                " (" + timeTable.stations().name(depStationId) + "), arrival=" +
                arrStationId + " (" + timeTable.stations().name(arrStationId) + ")");

        // Read the profile from the provided file
        System.out.println("Reading profile from file...");
        Profile profile = readProfile(timeTable, date, arrStationId);
        System.out.println("Profile loaded successfully");

        try {
            // Extract journeys using JourneyExtractor
            System.out.println("Extracting journeys...");
            List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
            System.out.println("Found " + journeys.size() + " journeys");

            // Verify journey connections against profile
            verifyJourneysMatchProfile(journeys, profile, depStationId);

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

            // Verify iCalendar conversion
            String icalEvent = JourneyIcalConverter.toIcalendar(journey);
            assertNotNull(icalEvent, "iCalendar event should not be null");

            // Check iCalendar structure
            System.out.println("Verifying iCalendar structure...");
            System.out.println(icalEvent);
            assertTrue(icalEvent.contains("BEGIN:VCALENDAR"), "iCalendar should start with BEGIN:VCALENDAR");
            assertTrue(icalEvent.contains("VERSION:2.0"), "iCalendar should contain VERSION:2.0");
            assertTrue(icalEvent.contains("PRODID:ReCHor"), "iCalendar should contain PRODID:ReCHor");
            assertTrue(icalEvent.contains("BEGIN:VEVENT"), "iCalendar should contain BEGIN:VEVENT");

            // Check summary (start and end stations)
            String summary = "SUMMARY:Ecublens VD, EPFL → Gruyères";
            assertTrue(icalEvent.contains(summary), "iCalendar should contain correct SUMMARY");

            // Check date/time format
            String formattedDate = date.toString().replace("-", "");
            assertTrue(icalEvent.contains("DTSTART:" + formattedDate),
                    "iCalendar should contain correct DTSTART date");
            assertTrue(icalEvent.contains("DTEND:" + formattedDate),
                    "iCalendar should contain correct DTEND date");

            // Verify event has description with journey details
            assertTrue(icalEvent.contains("DESCRIPTION:"), "iCalendar should contain DESCRIPTION");

            // Check closing tags
            assertTrue(icalEvent.contains("END:VEVENT"), "iCalendar should contain END:VEVENT");
            assertTrue(icalEvent.contains("END:VCALENDAR"), "iCalendar should end with END:VCALENDAR");
        } catch (Exception e) {
            System.out.println("ERROR: Exception occurred during journey extraction");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to fail the test
        }
    }

    // Helper method to read profile from the provided text file
    private Profile readProfile(TimeTable timeTable, LocalDate date, int arrStationId) throws IOException {
        Path path = Path.of("test/ch/epfl/rechor/journey/"+"profile_" + date + "_" + arrStationId + ".txt");
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

    private void verifyJourneysMatchProfile(List<Journey> journeys, Profile profile, int depStationId) {
        // Get the pareto front for the departure station
        ParetoFront front = profile.forStation(depStationId);

        // Create a list to track which criteria we've matched to journeys
        boolean[] matchedCriteria = new boolean[front.size()];
        AtomicInteger matchCount = new AtomicInteger();

        // First check: number of journeys should match number of criteria in the profile
        System.out.println("Checking journey count...");
        System.out.println("- Profile contains: " + front.size() + " journeys");
        System.out.println("- Extracted: " + journeys.size() + " journeys");

        // For each journey, find its matching criteria in the pareto front
        for (Journey journey : journeys) {
            // Extract journey properties to match against criteria
            int depMins = journey.depTime().getHour() * 60 + journey.depTime().getMinute();
            int arrMins = journey.arrTime().getHour() * 60 + journey.arrTime().getMinute();
            int changes = countTransfers(journey);

            // Find matching criteria in the pareto front
            AtomicBoolean found = new AtomicBoolean(false);
            int[] index = {0};
            front.forEach(criteria -> {
                if (!matchedCriteria[index[0]] &&
                        PackedCriteria.depMins(criteria) == depMins &&
                        PackedCriteria.arrMins(criteria) == arrMins &&
                        PackedCriteria.changes(criteria) == changes) {

                    matchedCriteria[index[0]] = true;
                    found.set(true);
                    matchCount.getAndIncrement();
                }
                index[0]++;
            });

            assertTrue(found.get(), "Journey not found in profile: dep=" + depMins + ", arr=" + arrMins + ", changes=" + changes);
        }

        assertEquals(front.size(), matchCount.get(), "Not all profile criteria were matched to journeys");

        System.out.println("All " + matchCount + " journeys match profile criteria");
    }

    private int countTransfers(Journey journey) {
        // Count transport legs, subtract 1 to get transfers
        int transportLegs = 0;
        for (Journey.Leg leg : journey.legs()) {
            if (leg instanceof Journey.Leg.Transport) {
                transportLegs++;
            }
        }
        return transportLegs - 1;
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
