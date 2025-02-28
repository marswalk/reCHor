package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

public final class JourneyIcalConverter {

    // Private so no instantiation (as with FormatterFr)
    private JourneyIcalConverter() {}

    /**
     * Converts a Journey to an iCalendar format.
     *
     * @param journey the Journey to convert
     * @return the String which is the iCalendar for the Journey
     */
    public static String toIcaldendar(Journey journey) {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        builder.add(IcalBuilder.Name.VERSION, "2.0");
        builder.add(IcalBuilder.Name.PRODID, "reCHor");
        builder.begin(IcalBuilder.Component.VEVENT);
        // example: EEBABA70-83B9-4342-A046-BC949F562DC0 (hyphens are in fixed positions, not characters)
        // 2^4^32=2^128 hence 32 hex characters  (as 128 bit value represented)
        builder.add(IcalBuilder.Name.UID, UUID.randomUUID().toString());
        builder.add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now());
        builder.add(IcalBuilder.Name.DTSTART, journey.depTime());
        builder.add(IcalBuilder.Name.DTEND, journey.arrTime());
        builder.add(IcalBuilder.Name.SUMMARY, journey.depStop().name() + " → " + journey.arrStop().name());

        StringJoiner sj = new StringJoiner("\\n");
        for (Journey.Leg leg : journey.legs()) {
            // must use case instead of just passing in leg because apparantly if you tried to call
            // FormatterFr.formatLeg(leg) directly, you’d be passing in a Journey.Leg (the interface type).
            // Because method overload resolution in Java is performed at compile time based on the declared type,
            // the compiler wouldn’t know which specific overload to call.
            switch (leg) {
                case Journey.Leg.Foot f -> sj.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> sj.add(FormatterFr.formatLeg(t));
            }
        }
        builder.add(IcalBuilder.Name.DESCRIPTION, sj.toString());
        builder.end(); // Closes VEVENT
        builder.end(); // Closes VCALENDAR
        return builder.build();
    }
}