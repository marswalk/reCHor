package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Converts a Journey to an iCalendar format.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */

public final class JourneyIcalConverter {

    // Private so no instantiation (as with FormatterFr)
    private JourneyIcalConverter() {
    }

    /**
     * Converts a Journey to an iCalendar format.
     *
     * @param journey the Journey to convert
     * @return the String which is the iCalendar for the Journey
     */
    public static String toIcalendar(Journey journey) {
        StringJoiner description = new StringJoiner("\\n");
        for (Journey.Leg leg : journey.legs()) {
            // must use case instead of using the two methods taking different parameters of formatLeg
            // as that way you’d be passing in a Journey.Leg (the interface type).
            // Because method overload resolution in Java is performed at compile time based on the declared type,
            // the compiler wouldn’t know which specific overload to call.
            switch (leg) {
                case Journey.Leg.Foot f -> description.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> description.add(FormatterFr.formatLeg(t));
            }
        }

        return new IcalBuilder()
                .begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now())
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime())
                .add(IcalBuilder.Name.SUMMARY, journey.depStop().name() + " → " + journey.arrStop().name())
                .add(IcalBuilder.Name.DESCRIPTION, description.toString())
                .end() // Ends VEVENT
                .end() // Ends VCALENDAR
                .build();
    }
}