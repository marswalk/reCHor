package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;

public class LilyaStructuredBufferTest2 {

    @Test
    public void testSizeCalculationForPlatformsTable() {
        // Structure pour une table avec deux champs U16 (2 octets chacun)
        int NOM = 0;
        int GARE_PARENT = 1;
        Structure structure = new Structure(
                Structure.field(NOM, Structure.FieldType.U16),
                Structure.field(GARE_PARENT, Structure.FieldType.U16)
        );
        // Créer un buffer de 12 octets => 12 / 4 = 3 enregistrements.
        ByteBuffer buffer = ByteBuffer.allocate(12);
        StructuredBuffer sb = new StructuredBuffer(structure, buffer);
        assertEquals(3, sb.size());
    }

    @Test
    public void testGetU8() {
        int FIELD0 = 0;
        Structure structure = new Structure(
                Structure.field(FIELD0, Structure.FieldType.U8)
        );
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(0, (byte) 10);
        buffer.put(1, (byte) 20);
        buffer.put(2, (byte) 30);
        buffer.put(3, (byte) 40);
        buffer.put(4, (byte) 50);
        StructuredBuffer sb = new StructuredBuffer(structure, buffer);
        assertEquals(5, sb.size());
        assertEquals(10, sb.getU8(FIELD0, 0));
        assertEquals(20, sb.getU8(FIELD0, 1));
        assertEquals(30, sb.getU8(FIELD0, 2));
        assertEquals(40, sb.getU8(FIELD0, 3));
        assertEquals(50, sb.getU8(FIELD0, 4));
    }

    @Test
    public void testGetU16() {
        int FIELD0 = 0;
        Structure structure = new Structure(
                Structure.field(FIELD0, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.putShort(0, (short) 100);
        buffer.putShort(2, (short) 200);
        buffer.putShort(4, (short) 300);
        StructuredBuffer sb = new StructuredBuffer(structure, buffer);
        assertEquals(3, sb.size());
        assertEquals(100, sb.getU16(FIELD0, 0));
        assertEquals(200, sb.getU16(FIELD0, 1));
        assertEquals(300, sb.getU16(FIELD0, 2));
    }

    @Test
    public void testGetS32() {
        int FIELD0 = 0;
        Structure structure = new Structure(
                Structure.field(FIELD0, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(0, -100);
        buffer.putInt(4, 1000);
        StructuredBuffer sb = new StructuredBuffer(structure, buffer);
        assertEquals(2, sb.size());
        assertEquals(-100, sb.getS32(FIELD0, 0));
        assertEquals(1000, sb.getS32(FIELD0, 1));
    }

    @Test
    public void testInvalidBufferSize() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(10);
        assertThrows(IllegalArgumentException.class, () -> new StructuredBuffer(structure, buffer));
    }
}
