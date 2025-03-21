package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SachaBufferedTransfersTest {

    // size() returns expected size
    @Test
    void sizeReturnsExpectedSize() {
        for (int n = 1; n < 100; n++) {
            byte[] testBytes = new byte[5*n];
            ByteBuffer testBuffer = ByteBuffer.wrap(testBytes);
            assertEquals(n, new BufferedTransfers(testBuffer).size());
        }
    }

    // IndexOutOfBounds is thrown as expected for all methods
    @Test
    void methodsThrowExceptionAsExpected() {
        ByteBuffer testBuffer = ByteBuffer.allocate(5);
        BufferedTransfers transfers = new BufferedTransfers(testBuffer);

        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(9999));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(1, 2));
    }

    // depStationId(), minutes() return expected values
    @Test
    void transferFieldsReturnExpectedValues() {
        ByteBuffer testBuffer = ByteBuffer.allocate(5);
        testBuffer.putShort((short) 10) // DEP_STATION_ID
                .putShort((short) 20) // ARR_STATION_ID
                .put((byte) 5);      // TRANSFER_MINUTES (5 minutes)
        testBuffer.flip();

        BufferedTransfers transfers = new BufferedTransfers(testBuffer);
        assertEquals(10, transfers.depStationId(0));
        assertEquals(5, transfers.minutes(0));
    }

//    // Test for empty buffer
//    @Test
//    void emptyBufferReturnsZeroSize() {
//        ByteBuffer testBuffer = ByteBuffer.allocate(0);
//        assertThrows(IllegalArgumentException.class, () -> new BufferedTransfers(testBuffer));
//    }
    // Test for large data set
    @Test
    void largeDataSetTest() {
        int numTransfers = 1000;
        ByteBuffer testBuffer = ByteBuffer.allocate(5 * numTransfers);
        for (int i = 0; i < numTransfers; i++) {
            testBuffer.putShort((short) (i * 2))  // DEP_STATION_ID
                    .putShort((short) (i * 3))  // ARR_STATION_ID
                    .put((byte) (i % 60)); // TRANSFER_MINUTES
        }
        // 0, 0, 0
        // 2, 3, 1
        // 4, 6, 2
        testBuffer.flip();

        BufferedTransfers transfers = new BufferedTransfers(testBuffer);
        assertEquals(numTransfers, transfers.size());
        assertEquals(0, transfers.depStationId(0));
        assertEquals(2, transfers.depStationId(1));
        assertEquals(2, transfers.minutes(2));
    }

    // Test for minutesBetween()
    @Test
    void minutesBetweenReturnsExpectedValues() {
        ByteBuffer testBuffer = ByteBuffer.allocate(10);
        testBuffer.putShort((short) 10).putShort((short) 20).put((byte) 5); // First transfer
        testBuffer.putShort((short) 15).putShort((short) 20).put((byte) 8); // Second transfer
        testBuffer.flip();

        BufferedTransfers transfers = new BufferedTransfers(testBuffer);
        assertEquals(5, transfers.minutesBetween(10, 20));
        assertEquals(8, transfers.minutesBetween(15, 20));
    }

    // Test for last index
    @Test
    void lastIndexTest() {
        ByteBuffer testBuffer = ByteBuffer.allocate(5 * 2);
        testBuffer.putShort((short) 50).putShort((short) 60).put((byte) 7); // First transfer
        testBuffer.putShort((short) 70).putShort((short) 80).put((byte) 9); // Last transfer
        testBuffer.flip();

        BufferedTransfers transfers = new BufferedTransfers(testBuffer);
        assertEquals(7, transfers.minutes(0));
        assertEquals(9, transfers.minutes(1));
    }
}