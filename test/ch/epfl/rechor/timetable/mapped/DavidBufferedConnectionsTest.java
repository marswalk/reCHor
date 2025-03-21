package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DavidBufferedConnectionsTest {
    private static BufferedConnections exampleBufferedConnections() {
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 10 00 20 00 06 00 80 10 01 01 01");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] succBytes = hexFormat.parseHex("00 00 00 08");
        ByteBuffer succBuffer = ByteBuffer.wrap(succBytes);
        return new BufferedConnections(buffer, succBuffer);
    }

    @Test
    void depStopIdThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().depStopId(id);
        });
    }

    @Test
    void nameThrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().depStopId(id);
        });
    }

    @Test
    void depStopIdWorksWithTrivialCase() {
        int expected = 16;
        assertEquals(expected, exampleBufferedConnections().depStopId(0));
    }

    @Test
    void depMinsThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().depMins(id);
        });
    }

    @Test
    void demMinsThrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().depMins(id);
        });
    }

    @Test
    void depMinsWorksWithTrivialCase() {
        int expected = 32;
        assertEquals(expected, exampleBufferedConnections().depMins(0));
    }

    @Test
    void arrStopIdThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().arrStopId(id);
        });
    }

    @Test
    void arrStopIdThrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().arrStopId(id);
        });
    }

    @Test
    void arrStopIdWorksWithTrivialCase() {
        int expected = 6;
        assertEquals(expected, exampleBufferedConnections().arrStopId(0));
    }

    @Test
    void arrMinsThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().arrMins(id);
        });
    }

    @Test
    void arrMinsThrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().arrMins(id);
        });
    }

    @Test
    void arrMinsWorksWithTrivialCase() {
        int expected = 8 * 16;
        assertEquals(expected, exampleBufferedConnections().arrMins(0));
    }

    @Test
    void tripIdThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().tripId(id);
        });
    }

    @Test
    void tripIdhrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().tripId(id);
        });
    }

    @Test
    void tripIdWorksWithTrivialCase() {
        int expected = 1048833;
        assertEquals(expected, exampleBufferedConnections().tripId(0));
    }

    //lol
    @Test
    void tripPosThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().tripPos(id);
        });
    }

    @Test
    void tripPoshrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().tripPos(id);
        });
    }

    @Test
    void tripPosWorksWithTrivialCase() {
        int expected = 1;
        assertEquals(expected, exampleBufferedConnections().tripPos(0));
    }

    @Test
    void nextConnectionIdThrowsIfIdIsNegative() {
        Random rand = new Random();
        int positiveNumber = rand.nextInt(1, 1000);
        int id = -positiveNumber;
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().nextConnectionId(id);
        });
    }

    @Test
    void nextConnectionIdThrowsIfIdIsGreaterOrEqualThanSize() {
        Random rand = new Random();
        int id = rand.nextInt(exampleBufferedConnections().size(), 1000);
        assertThrows(IndexOutOfBoundsException.class , () -> {
            exampleBufferedConnections().nextConnectionId(id);
        });
    }

    @Test
    void nextConnectionIdWorksWithTrivialCase() {
        int expected = 8;
        assertEquals(expected, exampleBufferedConnections().nextConnectionId(0));
    }

    @Test
    void sizeWorksWithTrivialCase() {
        int expected = 1;
        assertEquals(expected, exampleBufferedConnections().size());
    }

    @Test
    void sizeWorksIfEmpty() {
        int expected = 0;
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] succBytes = hexFormat.parseHex("");
        ByteBuffer succBuffer = ByteBuffer.wrap(succBytes);
        BufferedConnections emptyBufferedConnections = new BufferedConnections(buffer, succBuffer);
        assertEquals(expected, emptyBufferedConnections.size());
    }
}
