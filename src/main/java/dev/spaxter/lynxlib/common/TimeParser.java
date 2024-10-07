package dev.spaxter.lynxlib.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Time parser class.
 */
public class TimeParser {

    private static final DateTimeFormatter militaryFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter civilianFormatter = DateTimeFormatter.ofPattern("h:mma");

    /**
     * Get the next occurrence of a given time as a unix timestamp in milliseconds.
     *
     * @param timeString Time string, e.g. "11:00" or "3:00AM"
     * @return Next occurrence of {@code timeString} as a unix timestamp
     * @throws DateTimeParseException Throws if the timeString can not be parsed
     */
    public static long getNextUnixTimestamp(String timeString) throws DateTimeParseException {
        LocalTime time;
        if (timeString.toUpperCase().matches(".*(AM|PM)$")) {
            time = LocalTime.parse(timeString.toUpperCase(), civilianFormatter);
        } else {
            // Otherwise, assume 24-hour format
            time = LocalTime.parse(timeString, militaryFormatter);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime dateTime = LocalDateTime.of(today, time);

        // Check if the time has already passed for today
        if (dateTime.isBefore(LocalDateTime.now())) {
            // Move to the next day
            dateTime = dateTime.plusDays(1);
        }

        Instant nextOccurrence = dateTime.atZone(ZoneId.systemDefault()).toInstant();

        return nextOccurrence.toEpochMilli();
    }

    /**
     * Get the number of milliseconds until the next occurrence of a timestamp.
     *
     * @param timeString Time string, e.g. "11:00" or "3:00AM"
     * @return Milliseconds until next occurrence of {@code timeString}
     * @throws DateTimeParseException Throws if the timeString can not be parsed
     */
    public static long getMillisecondsUntil(String timeString) throws DateTimeParseException {
        long timestamp = TimeParser.getNextUnixTimestamp(timeString);
        long now = System.currentTimeMillis();
        return timestamp - now;
    }
}
