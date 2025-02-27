package ch.epfl.rechor.journey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class JourneyIcalConverterTest {

    @Test
    void toIcalendarConvertsJourneyCorrectly() {
        // Create all necessary stops with appropriate names and platform names
        Stop epfl = new Stop("Ecublens VD, EPFL", null, 0, 0);
        Stop renensGare = new Stop("Renens VD, gare", null, 0, 0);
        Stop renensVoie4 = new Stop("Renens VD", "4", 0, 0);
        Stop lausanneArr = new Stop("Lausanne", "5", 0, 0);
        Stop lausanneDep = new Stop("Lausanne", "1", 0, 0);
        Stop romontVoie2 = new Stop("Romont FR", "2", 0, 0);
        Stop romontVoie1 = new Stop("Romont FR", "1", 0, 0);
        Stop bulleArr = new Stop("Bulle", "2", 0, 0);
        Stop bulleDep = new Stop("Bulle", "4", 0, 0);
        Stop gruyeres = new Stop("Gruyères", "2", 0, 0);

        // Create journey legs
        LocalDateTime leg1Dep = LocalDateTime.of(2025, 2, 18, 16, 13);
        LocalDateTime leg1Arr = LocalDateTime.of(2025, 2, 18, 16, 19);
        Journey.Leg.Transport leg1 = new Journey.Leg.Transport(
                epfl, leg1Dep, renensGare, leg1Arr, List.of(), Vehicle.METRO, "R1", "Dest1");

        LocalDateTime foot1Dep = leg1Arr;
        LocalDateTime foot1Arr = foot1Dep.plusMinutes(3);
        Journey.Leg.Foot footLeg1 = new Journey.Leg.Foot(renensGare, foot1Dep, renensVoie4, foot1Arr);

        LocalDateTime leg2Dep = LocalDateTime.of(2025, 2, 18, 16, 26);
        LocalDateTime leg2Arr = LocalDateTime.of(2025, 2, 18, 16, 33);
        Journey.Leg.Transport leg2 = new Journey.Leg.Transport(
                renensVoie4, leg2Dep, lausanneArr, leg2Arr, List.of(), Vehicle.TRAIN, "R2", "Dest2");

        LocalDateTime foot2Dep = leg2Arr;
        LocalDateTime foot2Arr = foot2Dep.plusMinutes(5);
        Journey.Leg.Foot footLeg2 = new Journey.Leg.Foot(lausanneArr, foot2Dep, lausanneDep, foot2Arr);

        LocalDateTime leg3Dep = LocalDateTime.of(2025, 2, 18, 16, 40);
        LocalDateTime leg3Arr = LocalDateTime.of(2025, 2, 18, 17, 13);
        Journey.Leg.Transport leg3 = new Journey.Leg.Transport(
                lausanneDep, leg3Dep, romontVoie2, leg3Arr, List.of(), Vehicle.TRAIN, "R3", "Dest3");

        LocalDateTime foot3Dep = leg3Arr;
        LocalDateTime foot3Arr = foot3Dep.plusMinutes(3);
        Journey.Leg.Foot footLeg3 = new Journey.Leg.Foot(romontVoie2, foot3Dep, romontVoie1, foot3Arr);

        LocalDateTime leg4Dep = LocalDateTime.of(2025, 2, 18, 17, 22);
        LocalDateTime leg4Arr = LocalDateTime.of(2025, 2, 18, 17, 41);
        Journey.Leg.Transport leg4 = new Journey.Leg.Transport(
                romontVoie1, leg4Dep, bulleArr, leg4Arr, List.of(), Vehicle.TRAIN, "R4", "Dest4");

        LocalDateTime foot4Dep = leg4Arr;
        LocalDateTime foot4Arr = foot4Dep.plusMinutes(3);
        Journey.Leg.Foot footLeg4 = new Journey.Leg.Foot(bulleArr, foot4Dep, bulleDep, foot4Arr);

        LocalDateTime leg5Dep = LocalDateTime.of(2025, 2, 18, 17, 50);
        LocalDateTime leg5Arr = LocalDateTime.of(2025, 2, 18, 17, 57);
        Journey.Leg.Transport leg5 = new Journey.Leg.Transport(
                bulleDep, leg5Dep, gruyeres, leg5Arr, List.of(), Vehicle.TRAIN, "R5", "Dest5");

        List<Journey.Leg> legs = List.of(leg1, footLeg1, leg2, footLeg2, leg3, footLeg3, leg4, footLeg4, leg5);
        Journey journey = new Journey(legs);

        String ical = JourneyIcalConverter.toIcaldendar(journey);

        String[] lines = ical.split("\r?\n"); // Split lines considering both CRLF and LF

        // Expected line indices and values
        System.out.println(ical);
        int lineIdx = 0;
        assertEquals("BEGIN:VCALENDAR", lines[lineIdx++]);
        assertEquals("VERSION:2.0", lines[lineIdx++]);
        assertEquals("PRODID:reCHor", lines[lineIdx++]);
        assertEquals("BEGIN:VEVENT", lines[lineIdx++]);

        // Validate UID format (source: Deepseek R1)
        assertTrue(Pattern.matches(
                "^UID:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                lines[lineIdx++]));

        // Validate DTSTAMP format
        assertTrue(Pattern.matches("^DTSTAMP:\\d{8}T\\d{6}$", lines[lineIdx++]));

        assertEquals("DTSTART:20250218T161300", lines[lineIdx++]);
        assertEquals("DTEND:20250218T175700", lines[lineIdx++]);
        assertEquals("SUMMARY:Ecublens VD, EPFL → Gruyères", lines[lineIdx++]);

        // Validate DESCRIPTION content
        assertEquals("DESCRIPTION:16h13 Ecublens VD, EPFL → Renens VD, gare (arr. 16h19)\\ntrajet ", lines[lineIdx++]);
        assertEquals(" à pied (3 min)\\n16h26 Renens VD (voie 4) → Lausanne (arr. 16h33 voie 5)\\nc", lines[lineIdx++]);
        assertEquals(" hangement (5 min)\\n16h40 Lausanne (voie 1) → Romont FR (arr. 17h13 voie 2)", lines[lineIdx++]);
        assertEquals(" \\nchangement (3 min)\\n17h22 Romont FR (voie 1) → Bulle (arr. 17h41 voie 2)", lines[lineIdx++]);
        assertEquals(" \\nchangement (3 min)\\n17h50 Bulle (voie 4) → Gruyères (arr. 17h57 voie 2)", lines[lineIdx++]);

        assertEquals("END:VEVENT", lines[lineIdx++]);
        assertEquals("END:VCALENDAR", lines[lineIdx]);
    }
}