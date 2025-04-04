package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Event builder in iCalendar format
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class IcalBuilder {

    private final StringBuilder sb = new StringBuilder();
    private final List<Component> openComponents = new ArrayList<>();

    private void appendLine(String line) {
        int lineLength = line.length();
        int index = 0;
        boolean firstLine = true;

        while (index < lineLength) {
            int remainingChars = lineLength - index;
            int charsToTake = firstLine ? Math.min(75, remainingChars) : Math.min(74, remainingChars);
            int endIndex = index + charsToTake;

            // EDIT 3.2: Printing IcalConverter uses \n for each leg
            // which is interpreted as an actual new line
            // we changed it to a double \\n to "escape" and print the \n
            // here then we need to treat "\\n" as 2 char instead of 3 in a line
            if (endIndex < lineLength - 1 && line.substring(endIndex - 1, endIndex + 1).equals("\\n")) {
                endIndex--;
            }

            String part = line.substring(index, endIndex);
            if (!firstLine) {
                sb.append(" ");
            }
            sb.append(part).append("\r\n"); // Use CRLF instead of LF

            index = endIndex;
            firstLine = false;
        }
    }

    /**
     * Adds to the event being built a line
     * whose name and value are those given, taking care to "fold" the line as needed to respect
     * the constraint that no line of iCalendar data should exceed 75 characters.
     *
     * @param name  name of the "attribute" of the event in iCal format (SUMMARY, DESCRIPTION, etc.)
     * @param value the value assigned to the attribute
     * @return the event being built
     */
    public IcalBuilder add(Name name, String value) {
        String line = name.name() + ":" + value;
        appendLine(line);
        return this;
    }

    /**
     * Adds to the event being built a line
     * whose name is the one given and the value is the textual representation of the
     * date/time given, in the format specified in §2.1.6
     *
     * @param name     name of the "attribute" of the event in iCal format (DTSTART, DTEND, etc.)
     * @param dateTime the date and time to assign to the attribute
     * @return the event being built
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        // this time the hours is 2 digits HH instead of H (noticable when 0h... urgh)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String formattedDate = dateTime.format(formatter);
        add(name, formattedDate);
        return this;
    }

    /**
     * Starts a component by adding a line whose
     * name is BEGIN and the value is the name of the component given.
     *
     * @param component
     * @return begins a component
     */
    public IcalBuilder begin(Component component) {
        openComponents.add(component);
        add(Name.BEGIN, component.name());
        return this;
    }

    /**
     * Ends the last component that was previously started by begin but
     * not yet ended by a previous call to end, or throws an IllegalArgumentException if there are
     * none (see programming tips).
     *
     * @return ends the last opened component
     */
    public IcalBuilder end() {
        Preconditions.checkArgument(!openComponents.isEmpty());
        Component lastComponent = openComponents.removeLast();
        add(Name.END, lastComponent.name());
        return this;
    }

    /**
     * Returns the character string in iCalendar format representing the
     * event being built, or throws an IllegalArgumentException if a component that was started by
     * a call to begin has not, at this stage, been ended by a call to end.
     *
     * @return String in iCalendar format
     */
    public String build() {
        Preconditions.checkArgument(openComponents.isEmpty());
        return sb.toString();
    }

    /**
     * Calendar and the events inside a calendar
     */
    public enum Component {
        VCALENDAR,
        VEVENT
    }

    /**
     * Name of a line with the following values
     */
    public enum Name {
        BEGIN, // This marks the beginning of a component
        END, // This marks the end of a component
        PRODID, // The identifier for the product that created the calendar
        VERSION, // The version of the iCalendar format
        UID, // random 32 hex characters uniquely identifying the event
        DTSTAMP, // which gives the date and time WHEN THE EVENT WAS CREATED (NOT THAT OF THE EVENT ITSELF!)
        DTSTART, // The start date and time of the event
        DTEND, // The end date and time of the event
        SUMMARY, // A brief summary or title of the event
        DESCRIPTION // A detailed description of the event (as per format given using FormatterFr)
    }

}
