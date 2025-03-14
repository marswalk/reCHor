package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import static org.junit.jupiter.api.Assertions.*;

public class LiadStructuredBuffer {
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
    void u8repetitionBd() {
        int n = 100;
        int inByteLength = 1;
        for (int i = 1; i < n; i++) {
            Structure.Field[] fields = new Structure.Field[i];
            for (int j = 0; j < i; j++) {
                fields[j] = Structure.field(j, Structure.FieldType.U8);
            }
            Structure structure = new Structure(fields);
//            System.out.printf("structure size: %d\n", structure.totalSize());
            int[] byteAsInt = new int[i];
            for (int j = 0; j < i; j++) {
                byteAsInt[j] = j;
            }
            byte[] bytes = toByte(byteAsInt);
//            System.out.println("bytes " + Arrays.toString(bytes));
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(1, structuredBuffer.size());
//            System.out.println(structuredBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("i: %d; j: %d\n", i, j);
//                if (i == 2 && j == 1) {
//                    System.out.println(buffer.get(1));
//                }
                System.out.println(structuredBuffer);
                int v = structuredBuffer.getU8(j, 0);
                assertEquals(j, structuredBuffer.getU8(j, 0));
            }
        }
    }

    @Test
    void u16repetitionBd() {
        int n = 100;
        int inByteLength = 2;
        int offset = 1;
        for (int i = 1; i < n; i++) {
            Structure.Field[] fields = new Structure.Field[i];
            for (int j = 0; j < i; j++) {
                fields[j] = Structure.field(j, Structure.FieldType.U16);
            }
            Structure structure = new Structure(fields);
//            System.out.printf("structure size: %d\n", structure.totalSize());
            int[] byteAsInt = new int[i * inByteLength];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * inByteLength +offset] = j;
            }
            byte[] bytes = toByte(byteAsInt);
//            System.out.println("bytes " + Arrays.toString(bytes));
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(1, structuredBuffer.size());
//            System.out.println(structuredBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("i: %d; j: %d\n", i, j);
//                if (i == 2 && j == 1) {
//                    System.out.println(buffer.get(1));
//                }
//                System.out.println(structuredBuffer);
                assertEquals(j, structuredBuffer.getU16(j, 0));
            }
        }
    }

    @Test
    void S32repetitionBd() {
        int n = 100;
        int inByteLength = 4;
        int offset = 3;
        for (int i = 1; i < n; i++) {
            Structure.Field[] fields = new Structure.Field[i];
            for (int j = 0; j < i; j++) {
                fields[j] = Structure.field(j, Structure.FieldType.S32);
            }
            Structure structure = new Structure(fields);
//            System.out.printf("structure size: %d\n", structure.totalSize());
            int[] byteAsInt = new int[i * inByteLength];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * inByteLength +offset] = j;
            }
            byte[] bytes = toByte(byteAsInt);
//            System.out.println("bytes " + Arrays.toString(bytes));
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(1, structuredBuffer.size());
//            System.out.println(structuredBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("i: %d; j: %d\n", i, j);
//                if (i == 2 && j == 1) {
//                    System.out.println(buffer.get(1));
//                }
//                System.out.println(structuredBuffer);
                assertEquals(j, structuredBuffer.getS32(j, 0));
            }
        }
    }
}
