package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DavidBufferedRoutesTest {
    private static BufferedRoutes exampleBufferedRoute() {
        List<String> stringTable = new ArrayList<String>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");

        byte[] bytes = hexFormat.parseHex("00 04 00 00 01 02");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new BufferedRoutes(stringTable, buffer);
    }

    @Test
    void nameThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedRoute().name(id);
        });
    }

    @Test
    void nameThrowsIfIdGreaterOrEqualToSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedRoute().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedRoute().name(id);
        });
    }

    @Test
    void nameWorksWithTrivialCase1() {
        String expected = "Lausanne";
        assertEquals(expected, exampleBufferedRoute().name(0));
    }

    @Test
    void vehicleThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedRoute().vehicle(id);
        });
    }

    @Test
    void vehicleThrowsIfIdGreaterOrEqualToSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedRoute().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedRoute().vehicle(id);
        });
    }

    @Test
    void vehicleWorksWithTrivialCase1() {
        Vehicle expected = Vehicle.TRAM;
        assertEquals(expected, exampleBufferedRoute().vehicle(0));
    }

    @Test
    void vehicleWorksWithTrivialCase2() {
        Vehicle expected = Vehicle.TRAIN;
        assertEquals(expected, exampleBufferedRoute().vehicle(1));
    }

    @Test
    void sizeWorksWithTrivialCase() {
        int expected = 2;
        assertEquals(expected, exampleBufferedRoute().size());
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
        BufferedRoutes emptyBufferedRoute = new BufferedRoutes(stringTable, buffer);

        assertEquals(expected, emptyBufferedRoute.size());
    }
}
