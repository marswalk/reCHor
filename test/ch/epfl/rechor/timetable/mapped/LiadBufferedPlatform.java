package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LiadBufferedPlatform {
    static public byte[] toByte(int[] xs) {
        byte[] res = new byte[xs.length];
        for (int i = 0; i < xs.length; i++) {
            res[i] = (byte) xs[i];
        }
        return res;
    }

    public static ByteBuffer byteBufferOfLength(int n) {
        int[] aliasesByteAsInt = new int[n];
        byte[] aliasesByte = toByte(aliasesByteAsInt);
        return ByteBuffer.wrap(aliasesByte);
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


        int[] platfromsByteAsInt = {0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,};
        byte[] platfromsByte = toByte(platfromsByteAsInt);
        ByteBuffer platformsByteBuffer = ByteBuffer.wrap(platfromsByte);
        BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(stringTable, platformsByteBuffer);


        assertEquals(3, bufferedPlatforms.size());
        assertEquals("1", bufferedPlatforms.name(0));
        assertEquals("70", bufferedPlatforms.name(1));
        assertEquals("1", bufferedPlatforms.name(2));
        assertEquals(0, bufferedPlatforms.stationId(0));
        assertEquals(0, bufferedPlatforms.stationId(1));
        assertEquals(1, bufferedPlatforms.stationId(2));

    }


    @Test
    void testSizePlatformsBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");

        int bytePerUnit = 4;
        for (int i = 0; i < 100; i++) {
            ByteBuffer platformsByteBuffer = byteBufferOfLength(i * bytePerUnit);
            BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(stringTable, platformsByteBuffer);
            assertEquals(i, bufferedPlatforms.size());
        }
    }


    @Test
    void testInvalidLengthPlatformsBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");

        int bytePerUnit = 4;
        for (int i = 0; i < 100; i++) {
            for (int j = 1; j < bytePerUnit; j++) {
                ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit + j);
                assertThrows(IllegalArgumentException.class, () -> {
                    BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(stringTable, byteBuffer);
                });
            }
        }
    }

    @Test
    void testNameGettersPlatformsBd() {
        int n = 100;
        List<String> stringTable = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("yolo");
            stringTable.add(sb.toString());
        }

        int bytePerUnit = 4;
        int offset = 1;
        for (int i = 0; i < n; i++) {
            int[] byteAsInt = new int[i * bytePerUnit];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * bytePerUnit + offset] = j;
            }
            byte[] aliasesByte = toByte(byteAsInt);
            ByteBuffer byteBuffer = ByteBuffer.wrap(aliasesByte);

//            System.out.println(Arrays.toString(aliasesByte));

            BufferedPlatforms concreteBuffer = new BufferedPlatforms(stringTable, byteBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("here's my j: %d; size: %d\n", j, bufferedStations.size());
                assertEquals(stringTable.get(j), concreteBuffer.name(j));
            }
        }
    }

    @Test
    void testStationIfGettersPlatformsBd() {
        int n = 100;
        List<String> stringTable = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("yolo");
            stringTable.add(sb.toString());
        }

        int bytePerUnit = 4;
        int offset = 3;
        for (int i = 0; i < n; i++) {
            int[] byteAsInt = new int[i * bytePerUnit];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * bytePerUnit + offset] = j;
            }
            byte[] aliasesByte = toByte(byteAsInt);
            ByteBuffer byteBuffer = ByteBuffer.wrap(aliasesByte);

//            System.out.println(Arrays.toString(aliasesByte));

            BufferedPlatforms concreteBuffer = new BufferedPlatforms(stringTable, byteBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("here's my j: %d; size: %d\n", j, bufferedStations.size());
                assertEquals(j, concreteBuffer.stationId(j));
            }
        }
    }
}
