package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SachaBufferedTripsTest {

    // size() returns expected size
    @Test
    void sizeReturnsExpectedSize() {
        List<String> stringTable = List.of("");
        for (int n = 0; n < 100; n++) {
            byte[] testBytes = new byte[4 * n]; // Each trip entry is 4 bytes
            ByteBuffer testBuffer = ByteBuffer.wrap(testBytes);
            assertEquals(n, new BufferedTrips(stringTable, testBuffer).size());
        }
    }

    // IndexOutOfBounds is thrown as expected for routeId() and destination()
    @Test
    void routeIdAndDestinationThrowsExceptionAsExpected() {
        List<String> stringTable = List.of("Destination1");
        ByteBuffer testBuffer = ByteBuffer.wrap(new byte[4]);
        BufferedTrips trips = new BufferedTrips(stringTable, testBuffer);
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(1));
    }

    // routeId() returns expected values
    @Test
    void routeIdReturnsExpectedValues() {
        // use putShort for storing U16 and put for U8 suppose
        List<String> stringTable = List.of("Destination1", "Destination2");
        ByteBuffer testBuffer = ByteBuffer.allocate(8); // 2 trips * 4 bytes each
        testBuffer.putShort((short) 10).putShort((short) 0); // First trip
        // ROUTE_ID = 10 (0, 10)
        // DESTINATION_ID = 0 (0, 0)
        testBuffer.putShort((short) 20).putShort((short) 1); // Second trip
        // ROUTE_ID = 20 (0, 20)
        // DESTINATION_ID = 1 (0, 1)
        testBuffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, testBuffer);
        assertEquals(10, trips.routeId(0));
        assertEquals(20, trips.routeId(1));
    }

    // destination() returns expected names
    @Test
    void destinationReturnsExpectedValues() {
        List<String> stringTable = List.of("Destination1", "Destination2", "Destination3");
        byte[] testBytes = {
                0, 5, 0, 2,  // First trip: routeId = 5, destination index = 2 (5,2 would be U8)
                0, 15, 0, 1  // Second trip: routeId = 15, destination index = 1
        };
        ByteBuffer testBuffer = ByteBuffer.wrap(testBytes);

        BufferedTrips trips = new BufferedTrips(stringTable, testBuffer);
        assertEquals("Destination3", trips.destination(0));
        // because the first trip has destination index 2
        assertEquals("Destination2", trips.destination(1));
        // because the second trip has destination index 1
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(2));
    }

    // Test for empty buffer
    @Test
    void emptyBufferReturnsZeroSize() {
        List<String> stringTable = List.of();
        ByteBuffer testBuffer = ByteBuffer.allocate(0);
        BufferedTrips trips = new BufferedTrips(stringTable, testBuffer);
        assertEquals(0, trips.size());
    }

    // Test for large data set
    @Test
    void largeDataSetTest() {
        List<String> stringTable = List.of("D0", "D1", "D2", "D3", "D4");
        ByteBuffer testBuffer = ByteBuffer.allocate(4000); // 1000 trips * 4 bytes each
        for (int i = 0; i < 1000; i++) {
            testBuffer.putShort((short) (i * 2)).putShort((short) (i % stringTable.size()));
        }
        testBuffer.flip();
        BufferedTrips trips = new BufferedTrips(stringTable, testBuffer);
        assertEquals(1000, trips.size());
        assertEquals("D0", trips.destination(0));
        assertEquals("D4", trips.destination(4));
    }

    // Test for last index
    @Test
    void lastIndexTest() {
        List<String> stringTable = List.of("Destination1", "Destination2");
        ByteBuffer testBuffer = ByteBuffer.allocate(8);
        testBuffer.putShort((short) 30).putShort((short) 0); // First trip
        testBuffer.putShort((short) 40).putShort((short) 1); // Last trip
        testBuffer.flip();
        BufferedTrips trips = new BufferedTrips(stringTable, testBuffer);
        assertEquals(40, trips.routeId(trips.size() - 1));
    }
}