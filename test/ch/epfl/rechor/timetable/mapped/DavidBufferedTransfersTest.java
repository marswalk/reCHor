package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DavidBufferedTransfersTest {
    private final BufferedTransfers bt;

    private BufferedTransfers exampleBufferedTransfers() {
        StringJoiner sj = new StringJoiner(" ")
                .add("00 10").add("00 03").add("02")
                .add("00 11").add("00 03").add("04")
                .add("00 12").add("00 05").add("06")
                .add("00 13").add("00 05").add("08")
                .add("00 14").add("00 05").add("0a")
                .add("00 15").add("00 07").add("0c")
                .add("00 16").add("00 09").add("0e")
                .add("00 17").add("00 09").add("10")
                .add("00 18").add("00 09").add("12")
                .add("00 19").add("00 09").add("14")
                .add("00 1a").add("00 01").add("16");
        /*
        Ranges for each arrival station :
        00 03 : [0,2[
        00 05 : [2,5[
        00 07 : [5,6[
        00 09 : [6,10[
        00 01 : [10,11[
         */
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex(sj.toString());
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new BufferedTransfers(buffer);
    }

    private DavidBufferedTransfersTest() {
        bt = exampleBufferedTransfers();
    }

    @Test
    void depStationIdReturnsCorrectId() {
        for (int i = 0; i < bt.size(); ++i) {
            assertEquals(16 + i, bt.depStationId(i));
        }
    }

    @Test
    void depStationIdThrowsIfInvalidId() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bt.depStationId(11);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bt.depStationId(-1);
        });
    }

    @Test
    void minutesReturnsCorrectId() {
        for (int i = 0; i < bt.size(); ++i) {
            assertEquals(2*(i+1), bt.minutes(i));
        }
    }

    @Test
    void minutesThrowsIfInvalidId() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bt.minutes(11);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bt.minutes(-1);
        });
    }

    @Test
    void arrivingAtReturnsCorrectRanges() {
        assertEquals(PackedRange.pack(0,2), bt.arrivingAt(3));
        assertEquals(PackedRange.pack(2,5), bt.arrivingAt(5));
        assertEquals(PackedRange.pack(5,6), bt.arrivingAt(7));
        assertEquals(PackedRange.pack(6,10), bt.arrivingAt(9));
        assertEquals(PackedRange.pack(10,11), bt.arrivingAt(1));
    }

    @Test
    void arrivingAtThrowsWhenInvalidId() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        {
            bt.arrivingAt(-1);
        });
        assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        {
            bt.arrivingAt(10); // too large of a station ID (max. ID = 9)
        });
    }

    @Test
    void minutesBetweenWorksWithValidIds() {
        int[] arrivalStations = new int[] {3,3,5,5,5,7,9,9,9,9,1};
        for (int i = 0; i < bt.size(); ++i) {
            assertEquals(2*(i+1), bt.minutesBetween(16+i, arrivalStations[i]));
        }
    }

    @Test
    void minutesBetweenThrowsIfConnectionDoesNotExist() {
        assertThrows(NoSuchElementException.class, () -> {
            bt.minutesBetween(32, 9); // non-existing depStationId
        });
        assertThrows(NoSuchElementException.class, () -> {
            bt.minutesBetween(10, 2); // non-existing arrStationId
        });
        assertThrows(NoSuchElementException.class, () -> {
            bt.minutesBetween(16, 7); // unmatched depStationId and arrStationId
        });
    }

    @Test
    void minutesBetweenThrowsWhenInvalidId() {
        assertThrows(NoSuchElementException.class, () -> {
            bt.minutesBetween(-1, 7);
        });
//        assertThrows(NoSuchElementException.class, () -> {
//            bt.minutesBetween(10, -4);
//        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bt.minutesBetween(10, -4);
        });
        assertThrows(NoSuchElementException.class, () -> {
            bt.minutesBetween(28, 7);
        });
//        assertThrows(NoSuchElementException.class, () -> {
//            bt.minutesBetween(21, 13);
//        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bt.minutesBetween(21, 13);
        });
    }

    @Test
    void sizeReturnsCorrectSizeWhenNonEmpty() {
        assertEquals(11, bt.size());
    }

    @Test
    void sizeReturnsCorrectSizeWhenEmpty() {
        assertEquals(0, new BufferedTransfers(ByteBuffer.wrap(new byte[]{})).size());
    }
}
