package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SachaBufferedConnectionsTest {

    // size() returns expected size
    @Test
    void sizeReturnsExpectedSize() {
        for (int n = 0; n < 100; n++) {
            ByteBuffer testBuffer = ByteBuffer.allocate(12 * n); // Each connection entry is 12 bytes
            ByteBuffer succBuffer = ByteBuffer.allocate(4 * n); // Each next connection index is 4 bytes
            assertEquals(n, new BufferedConnections(testBuffer, succBuffer).size());
        }
    }

    // IndexOutOfBounds is thrown as expected for all methods
    @Test
    void methodsThrowExceptionAsExpected() {
        ByteBuffer testBuffer = ByteBuffer.allocate(12);
        ByteBuffer succBuffer = ByteBuffer.allocate(4);
        BufferedConnections connections = new BufferedConnections(testBuffer, succBuffer);

        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depMins(1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrMins(1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripPos(1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(1));
    }

    // depStopId(), depMins(), arrStopId(), arrMins(), tripId(), and tripPos() return expected values
    @Test
    void connectionFieldsReturnExpectedValues() {
        ByteBuffer testBuffer = ByteBuffer.allocate(12);
        testBuffer.putShort((short) 100) // DEP_STOP_ID
                .putShort((short) 120) // DEP_MINUTES
                .putShort((short) 200) // ARR_STOP_ID
                .putShort((short) 150) // ARR_MINUTES
                .putInt(0x000A005);   // TRIP_POS_ID (trip ID = 10, position = 5)
        testBuffer.flip();

        ByteBuffer succBuffer = ByteBuffer.allocate(4);
        succBuffer.putInt(2); // Next connection index
        succBuffer.flip();

        BufferedConnections connections = new BufferedConnections(testBuffer, succBuffer);
        assertEquals(100, connections.depStopId(0));
        assertEquals(120, connections.depMins(0));
        assertEquals(200, connections.arrStopId(0));
        assertEquals(150, connections.arrMins(0));
        assertEquals(160, connections.tripId(0));
        assertEquals(5, connections.tripPos(0));
        assertEquals(2, connections.nextConnectionId(0));
    }

    // Test for empty buffer
    @Test
    void emptyBufferReturnsZeroSize() {
        ByteBuffer testBuffer = ByteBuffer.allocate(0);
        ByteBuffer succBuffer = ByteBuffer.allocate(0);
        BufferedConnections connections = new BufferedConnections(testBuffer, succBuffer);
        assertEquals(0, connections.size());
    }

    // Test for large data set
    @Test
    void largeDataSetTest() {
        int numConnections = 1000;
        ByteBuffer testBuffer = ByteBuffer.allocate(12 * numConnections);
        ByteBuffer succBuffer = ByteBuffer.allocate(4 * numConnections);
        for (int i = 0; i < numConnections; i++) {
            testBuffer.putShort((short) (i * 2))  // DEP_STOP_ID
                    .putShort((short) (i * 3))  // DEP_MINUTES
                    .putShort((short) (i * 4))  // ARR_STOP_ID
                    .putShort((short) (i * 5))  // ARR_MINUTES
                    .putInt((i << 8) | (i % 256)); // TRIP_POS_ID (trip index = i, position = i % 256)
            succBuffer.putInt(i + 1); // Next connection index
        }
        testBuffer.flip();
        succBuffer.flip();

        BufferedConnections connections = new BufferedConnections(testBuffer, succBuffer);
        assertEquals(numConnections, connections.size());
        assertEquals(0, connections.depStopId(0));
        assertEquals(3, connections.depMins(1));
        assertEquals(8, connections.arrStopId(2));
        assertEquals(15, connections.arrMins(3));
        assertEquals(2, connections.tripId(2));
        assertEquals(2, connections.tripPos(2));
        assertEquals(3, connections.nextConnectionId(2));
    }
}