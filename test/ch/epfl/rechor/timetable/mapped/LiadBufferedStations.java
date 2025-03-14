package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LiadBufferedStations {
    static public byte[] toByte(int[] xs) {
        byte[] res = new byte[xs.length];
        for (int i = 0; i < xs.length; i++) {
            res[i] = (byte) xs[i];
        }
        return res;
    }

    @Test
    void givenExempleBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");

        int[] stationsByteAsInt = {0x00, 0x04, 0x04, 0xb6, 0xca, 0x14, 0x21, 0x14, 0x1f, 0xa1, 0x00, 0x06, 0x04, 0xdc, 0xcc, 0x12, 0x21, 0x18, 0xda, 0x03};
        byte[] stationsByte = toByte(stationsByteAsInt);
        ByteBuffer stationsByteBuffer = ByteBuffer.wrap(stationsByte);
        BufferedStations bufferedStations = new BufferedStations(stringTable, stationsByteBuffer);

        assertEquals(2, bufferedStations.size());
        assertEquals("Lausanne", bufferedStations.name(0));
        assertEquals("Palézieux", bufferedStations.name(1));
        assertEquals(6.629092, bufferedStations.longitude(0), 0.001);
        assertEquals(6.837875, bufferedStations.longitude(1), 0.001);
        assertEquals(46.516792, bufferedStations.latitude(0), 0.001);
        assertEquals(46.542764, bufferedStations.latitude(1), 0.001);
    }

    public static ByteBuffer byteBufferOfLength(int n) {
        int[] aliasesByteAsInt = new int[n];
        byte[] aliasesByte = toByte(aliasesByteAsInt);
        return ByteBuffer.wrap(aliasesByte);
    }



    @Test
    void testSizeStationsBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");

        int bytePerUnit = 10;
        for (int i = 0; i < 100; i++) {
            ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit);
            BufferedStations bufferedStations = new BufferedStations(stringTable, byteBuffer);
            assertEquals(i, bufferedStations.size());
        }
    }


    @Test
    void testInvalidLengthStationsBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");

        int bytePerUnit = 10;
        for (int i = 0; i < 100; i++) {
            for (int j = 1; j < bytePerUnit; j++) {
                ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit + j);
                assertThrows(IllegalArgumentException.class, () -> {
                    BufferedStations bufferedStations = new BufferedStations(stringTable, byteBuffer);
                });
            }
        }
    }
}
