package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DavidBufferedTripsTest {
    private static BufferedTrips exampleBufferedTrips() {
        List<String> stringTable = new ArrayList<String>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");

        byte[] bytes = hexFormat.parseHex("00 05 00 04 00 02 00 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new BufferedTrips(stringTable, buffer);
    }

    @Test
    void routeIdThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedTrips().routeId(id);
        });
    }

    @Test
    void routeIdThrowsIfIdGreaterOrEqualToSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedTrips().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedTrips().routeId(id);
        });
    }

    @Test
    void aliasWorksWithTrivialCase1() {
        int expected = 5;
        assertEquals(expected, exampleBufferedTrips().routeId(0));
    }

    @Test
    void aliasWorksWithTrivialCase2() {
        int expected = 2;
        assertEquals(expected, exampleBufferedTrips().routeId(1));
    }

    @Test
    void destinationThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedTrips().destination(id);
        });
    }

    @Test
    void stationNameThrowsIfIdGreaterOrEqualToSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedTrips().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedTrips().destination(id);
        });
    }

    @Test
    void stationNameWorksWithTrivialCase() {
        String expected = "Ins";
        assertEquals(expected, exampleBufferedTrips().destination(1));
    }

    @Test
    void sizeWorksWithTrivialCase() {
        int expected = 2;
        assertEquals(expected, exampleBufferedTrips().size());
    }

    @Test
    void sizeWorksIfEmpty() {
        int expected = 0;
        List<String> stringTable = new ArrayList<String>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        BufferedTrips emptyBufferedTrips = new BufferedTrips(stringTable, buffer);

        assertEquals(expected, emptyBufferedTrips.size());
    }
}
