package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SachaBufferedRoutesTest {

    // size() returns expected size
    @Test
    void sizeReturnsExpectedSize() {
        List<String > stringTable = List.of("");
        for (int n = 0; n < 100; n++) {
            byte[] testBytes = new byte[3*n];
            ByteBuffer testBuffer = ByteBuffer.wrap(testBytes);
            assertEquals(n, new BufferedRoutes(stringTable, testBuffer).size());
        }
    }

    // IndexOutOfBounds is thrown as expected for vehicle() and name()
    @Test
    void vehicleAndNameThrowsExceptionAsExpected() {
        List<String > stringTable = List.of("");
        ByteBuffer testBuffer = ByteBuffer.wrap(new byte[3]);
        BufferedRoutes routes = new BufferedRoutes(stringTable, testBuffer);
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(1));
    }

    // vehicle() returns expected values
    @Test
    void vehicleReturnsExpectedValues() {
        List<String > stringTable = List.of("R1");
        ByteBuffer testBuffer;
        // bytes to store
        for (byte i = 0; i < 7; i++) {
            testBuffer = ByteBuffer.wrap(new byte[] {0, 0, i});
            BufferedRoutes routes = new BufferedRoutes(stringTable, testBuffer);
            assertEquals(Vehicle.ALL.get(i), routes.vehicle(0));
        }
    }

    // name() returns expected names
    @Test
    void nameReturnsExpectedValues() {
        List<String> stringTable = List.of("R1", "R2", "R3", "R4", "R5");

        // Allocate a ByteBuffer of the exact required size (5 * 3 = 15 bytes)
        ByteBuffer testBuffer = ByteBuffer.allocate(5 * 3);

        // Fill the buffer
        for (byte i = 0; i < 5; i++) {
            testBuffer.put((byte) 0)
                    .put(i)
                    .put((byte) ((i+1)*10));
        }
        testBuffer.flip();
        BufferedRoutes routes = new BufferedRoutes(stringTable, testBuffer);
        for (int i = 0; i < 5; i++) {
            assertEquals(stringTable.get(i), routes.name(i));
        }
    }
}