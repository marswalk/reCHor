package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides French-specific formatting utilities for times, stops, and journey legs.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class FormatterFr {

    private FormatterFr() {
    }

    /**
     * Formats a duration in hours and minutes (e.g. "2 h 15 min").
     *
     * @param duration the duration to format
     * @return a formatted string representing the duration
     */
    public static String formatDuration(Duration duration) {
        String durationString = "";
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        if (hours > 0) {
            durationString += hours + " h ";
        }
        durationString += minutes + " min";
        return durationString;
    }

    /**
     * Formats a date-time using the pattern H'h'mm (hours and minutes).
     *
     * @param dateTime the date-time to format
     * @return the formatted date-time string
     */
    public static String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H'h'mm");
        return formatter.format(dateTime);
    }

    /**
     * Formats the platform name of a stop as per §2.4.3.
     * If platform name is empty, returns an empty string.
     * Otherwise, if the first character is a digit it is called a voie,
     * else the platform is called a quai.
     *
     * @param stop the stop to format
     * @return formatted platform string or empty if none
     */
    public static String formatPlatformName(Stop stop) {
        String platform = stop.platformName();
        if (platform == null || platform.isBlank()) {
            return "";
        }
        char firstChar = platform.charAt(0);
        if (Character.isDigit(firstChar)) {
            return "voie " + platform;
        } else {
            return "quai " + platform;
        }
    }

    /**
     * Formats a foot leg as described in §2.4.4.
     * If the leg is a transfer (method of Foot which tells if same station), the label "changement" is used,
     * otherwise "trajet à pied", followed by the duration between parentheses.
     *
     * @param footLeg the foot leg to format
     * @return formatted foot leg string
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {
        String type = footLeg.isTransfer() ? "changement" : "trajet à pied";
        return type + " (" + formatDuration(footLeg.duration()) + ")";
    }

    /**
     * Formats a transport leg in French, showing departure, arrival, and platform information.
     *
     * @param leg the transport leg to format
     * @return a formatted string for the transport leg
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatTime(leg.depTime()));
        sb.append(" ");
        sb.append(leg.depStop().name());
        if (leg.depStop().platformName() != null && !leg.depStop().platformName().isBlank()) {
            sb.append(" (");
            sb.append(formatPlatformName(leg.depStop()));
            sb.append(")");
        }
        sb.append(" → ");
        sb.append(leg.arrStop().name());
        sb.append(" (arr. ");
        sb.append(formatTime(leg.arrTime()));
        if (leg.arrStop().platformName() != null && !leg.arrStop().platformName().isBlank()) {
            sb.append(" ");
            sb.append(formatPlatformName(leg.arrStop()));
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Formats the route destination for a transport leg as described in §2.4.6.
     * The format is: "route Direction destination".
     *
     * @param transportLeg the transport leg for which to format the route and destination
     * @return formatted route destination string
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }
}