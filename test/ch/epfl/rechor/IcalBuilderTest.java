package ch.epfl.rechor;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class IcalBuilderTest {

    /**
     * Test a normal calendar building scenario.
     * Verifies that all components and attributes are added correctly and
     * that build() returns the expected multi-line iCalendar format.
     */
    @Test
    public void testNormalCalendarBuild() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        builder.add(IcalBuilder.Name.PRODID, "TestProd");
        builder.add(IcalBuilder.Name.VERSION, "2.0");
        builder.begin(IcalBuilder.Component.VEVENT);
        builder.add(IcalBuilder.Name.SUMMARY, "Event Summary");
        // Using a specific date/time to test the formatting
        LocalDateTime dt = LocalDateTime.of(2025, 2, 25, 10, 30, 0);
        builder.add(IcalBuilder.Name.DTSTART, dt);
        builder.end(); // Closes VEVENT
        builder.end(); // Closes VCALENDAR
        String output = builder.build();

        String expected = ""
                + "BEGIN:VCALENDAR\n"
                + "PRODID:TestProd\n"
                + "VERSION:2.0\n"
                + "BEGIN:VEVENT\n"
                + "SUMMARY:Event Summary\n"
                + "DTSTART:20250225T103000\n"
                + "END:VEVENT\n"
                + "END:VCALENDAR\n";
        assertEquals(expected, output);
    }

    /**
     * Test a border case for line folding where the line length is exactly 75 characters.
     * This ensures that no folding occurs.
     */
    @Test
    public void testNoFoldingAt75Characters() {
        IcalBuilder builder = new IcalBuilder();
        // "SUMMARY:" is 8 characters; to reach total 75, add 67 characters.
        String value = "A".repeat(67);
        builder.add(IcalBuilder.Name.SUMMARY, value);
        String output = builder.build();

        String expected = "SUMMARY:" + value + "\n";
        assertEquals(expected, output);
    }

    /**
     * Test a border case for line folding where the line length exceeds 75 characters.
     * The output should be folded into two or more lines.
     */
    @Test
    public void testFoldingForOver75Characters() {
        IcalBuilder builder = new IcalBuilder();
        // "SUMMARY:" is 8 characters; to exceed 75 by 1, we need 76 total: 68-character value.
        String value = "B".repeat(68);
        builder.add(IcalBuilder.Name.SUMMARY, value);
        String output = builder.build();

        // The folding logic: first 75 characters on line1, then the remainder prefixed with a space.
        String originalLine = "SUMMARY:" + value; // Total length 76.
        String firstPart = originalLine.substring(0, 75);
        String remainder = originalLine.substring(75);
        String expected = firstPart + "\n" + " " + remainder + "\n";
        assertEquals(expected, output);
    }

    /**
     * Test calling end() when no component has been started.
     * Should throw an IllegalArgumentException.
     */
    @Test
    public void testEndWithoutBegin() {
        IcalBuilder builder = new IcalBuilder();
        assertThrows(IllegalArgumentException.class, () -> {
            builder.end();
        });
    }

    /**
     * Test calling build() when there is an open component that has not been closed.
     * This should also throw an IllegalArgumentException.
     */
    @Test
    public void testBuildWithOpenComponent() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        // Not calling end() for the started component.
        assertThrows(IllegalArgumentException.class, () -> {
            builder.build();
        });
    }
}
