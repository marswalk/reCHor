package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.journey.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.nio.ByteBuffer;
import java.util.Random;


import static org.junit.jupiter.api.Assertions.*;


public class DavidStructureTest {


    private Structure structure;
    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;
    private static final int LARGE_TEST_SIZE = 1000;


    @BeforeEach
    void setUp() {
        structure = new Structure(
                Structure.field(ALIAS_ID, Structure.FieldType.U16),
                Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
        );
    }


    @Test
    void testTotalSize() {
        assertEquals(4, structure.totalSize()); // 2 champs U16 (2 bytes chacun)
    }


    @Test
    void testOffsetBasic() {
        assertEquals(0, structure.offset(ALIAS_ID, 0));
        assertEquals(2, structure.offset(STATION_NAME_ID, 0));
        assertEquals(4, structure.offset(ALIAS_ID, 1));
        assertEquals(6, structure.offset(STATION_NAME_ID, 1));
    }


    @Test
    void testOffsetWithLargeIndexes() {
        assertEquals(4000, structure.offset(ALIAS_ID, 1000));
        assertEquals(4002, structure.offset(STATION_NAME_ID, 1000));
    }


    @Test
    void testOffsetOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(2, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(-1, 0));
    }


    @Test
    void testInvalidFieldOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(0, Structure.FieldType.U16)
        ));
    }






    @Test
    void testRandomizedOffsets() {
        Random random = new Random();
        for (int i = 0; i < LARGE_TEST_SIZE; i++) {
            int randomIndex = random.nextInt(10000);
            assertEquals(randomIndex * 4, structure.offset(ALIAS_ID, randomIndex));
            assertEquals(randomIndex * 4 + 2, structure.offset(STATION_NAME_ID, randomIndex));
        }
    }


    @Test
    void testLargeStructureCreation() {
        Structure largeStructure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(7, largeStructure.totalSize());
    }
}


































class MyyStructureTest {
    @Test
    void offsetWorksExample() {
        Structure STRUCTURE = new Structure(
                Structure.field(0, Structure.FieldType.U16), Structure.field(1, Structure.FieldType.U16));
        int offset = STRUCTURE.offset(1,18);
        System.out.println(offset);
    }


    @Test
    void totalsizeWorks() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U8),
                Structure.field(2, Structure.FieldType.U16)
        );
        Structure structure1 = new Structure();
        int expectedSize = 0;
        Random random = new Random();
        for (int i = 0; i < 10; i++) {


        }




        assertEquals(5, structure.totalSize());
        assertEquals(0, structure1.totalSize());
        assertEquals(0, structure1.totalSize());


    }


    @Test
    void singleU8FieldHasSizeOne() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8)
        );
        assertEquals(1, structure.totalSize());
    }


    @Test
    void singleU16FieldHasSizeTwo() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16)
        );
        assertEquals(2, structure.totalSize());
    }


    @Test
    void multipleU8FieldsSumCorrectly() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U8),
                Structure.field(2, Structure.FieldType.U8)
        );
        assertEquals(3, structure.totalSize());
    }


    @Test
    void mixedU8AndU16Fields() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U8),
                Structure.field(2, Structure.FieldType.U8),
                Structure.field(3, Structure.FieldType.U16)
        );
        assertEquals(6, structure.totalSize());
    }


    @Test
    void mixedU8andU16ansS32() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.U16)
        );
        assertEquals(7, structure.totalSize());
    }


    @Test
    void fieldDoesntWorkIfTypeNull(){
        assertThrows(NullPointerException.class, () -> new Structure.Field(2,null));
    }


    @Test
    void fieldWith2SameIndexInStructure(){
        assertThrows(IllegalArgumentException.class, () -> new Structure(new Structure.Field(0, Structure.FieldType.U16),
                new Structure.Field(1, Structure.FieldType.U8), new Structure.Field(1, Structure.FieldType.U8),
                new Structure.Field(3, Structure.FieldType.U8),
                new Structure.Field(4, Structure.FieldType.U8), new Structure.Field(5, Structure.FieldType.U8),
                new Structure.Field(6, Structure.FieldType.U8),new Structure.Field(7, Structure.FieldType.U8),
                new Structure.Field(8, Structure.FieldType.U8),new Structure.Field(9, Structure.FieldType.U8),
                new Structure.Field(10, Structure.FieldType.U8),new Structure.Field(11, Structure.FieldType.U8)));
    }


    @Test
    void fieldWithIndexNotInOrderInStructure(){
        assertThrows(IllegalArgumentException.class, () -> new Structure(new Structure.Field(0, Structure.FieldType.U16),
                new Structure.Field(1, Structure.FieldType.U8), new Structure.Field(2, Structure.FieldType.U8),
                new Structure.Field(3, Structure.FieldType.U8),
                new Structure.Field(4, Structure.FieldType.U8), new Structure.Field(5, Structure.FieldType.U8),
                new Structure.Field(6, Structure.FieldType.U8),new Structure.Field(7, Structure.FieldType.U8),
                new Structure.Field(9, Structure.FieldType.U8),new Structure.Field(8, Structure.FieldType.U8),
                new Structure.Field(10, Structure.FieldType.U8),new Structure.Field(11, Structure.FieldType.U8)));
    }


    @Test
    void testFieldCreation() {
        Structure.Field field = Structure.field(0, Structure.FieldType.U8);
        assertEquals(0, field.index());
        assertEquals(Structure.FieldType.U8, field.type());
    }


    @Test
    void testStructureCreationValidOrder() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(7, structure.totalSize());
    }


    @Test
    void testStructureCreationInvalidOrder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure(
                    Structure.field(1, Structure.FieldType.U8),
                    Structure.field(0, Structure.FieldType.U16)
            );
        });
    }


    @Test
    void testTotalSize() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U8)
        );
        assertEquals(2, structure.totalSize());
    }


    @Test
    void testOffsetValid() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
        assertEquals(3, structure.offset(2, 0));
        assertEquals(7, structure.offset(0, 1));
    }


    @Test
    void testOffsetInvalidFieldIndex() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(2, 0));
    }
    // C'est chat qui m'a fait ce test mais je sais pas si il est juste donc voila
    @Test
    void testOffsetLoop() {
        for (int numFields = 1; numFields <= 5; numFields++) {
            Structure.Field[] fields = new Structure.Field[numFields];
            int expectedSize = 0;


            for (int i = 0; i < numFields; i++) {
                Structure.FieldType type = switch (i % 3) {
                    case 0 -> Structure.FieldType.U8;
                    case 1 -> Structure.FieldType.U16;
                    default -> Structure.FieldType.S32;
                };
                fields[i] = Structure.field(i, type);
                expectedSize += type.size();
            }
            System.out.println(expectedSize);
            Structure structure = new Structure(fields);
            assertEquals(expectedSize, structure.totalSize());


            for (int fieldIndex = 0; fieldIndex < numFields; fieldIndex++) {
                for (int elementIndex = 0; elementIndex < 10; elementIndex++) {
                    int expectedOffset = structure.offset(fieldIndex, 0) + (elementIndex * structure.totalSize());
                    assertEquals(structure.offset(fieldIndex, elementIndex), expectedOffset);
                }
            }
        }
    }
}


















class StructureTest {


    private Structure structure;


    @BeforeEach
    void setUp() {
        structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.S32)
        );
    }


    @Test
    void testValidConstruction() {
        assertNotNull(structure);
        assertEquals(10, structure.totalSize()); // 2 + 4 + 4 = 10 octets
    }


    @Test
    void testInvalidConstructionOutOfOrderFields() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(0, Structure.FieldType.S32)
        ));
    }


    @Test
    void testOffsetCalculation() {
        for (int i = 0; i < 10; i++) {
            assertEquals(i * 10, structure.offset(0, i)); // Vérifie que chaque élément commence au bon endroit
            assertEquals(i * 10 + 2, structure.offset(1, i));
            assertEquals(i * 10 + 6, structure.offset(2, i));
        }
    }
}




















class MyStructuredBufferCHATTest {


    private StructuredBuffer buffer;
    private Structure structure;
    private ByteBuffer byteBuffer;
    private static final int NUM_ELEMENTS = 1000; // Nombre d'éléments pour les tests aléatoires


    @BeforeEach
    void setUp() {
        structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16),
                new Structure.Field(2, Structure.FieldType.S32)
        );


        byte[] data = new byte[structure.totalSize() * NUM_ELEMENTS];
        Random rand = new Random();
        rand.nextBytes(data);
        byteBuffer = ByteBuffer.wrap(data);
        buffer = new StructuredBuffer(structure, byteBuffer);
    }


    @Test
    void testSize() {
        assertEquals(NUM_ELEMENTS, buffer.size());
    }


    @Test
    void testGetU8Valid() {
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            int expected = Byte.toUnsignedInt(byteBuffer.get(structure.offset(0, i)));
            assertEquals(expected, buffer.getU8(0, i));
        }
    }


    @Test
    void testGetU8LowerBound() {
        assertEquals(Byte.toUnsignedInt(byteBuffer.get(0)), buffer.getU8(0, 0));
    }


    @Test
    void testGetU8UpperBound() {
        int lastIndex = NUM_ELEMENTS - 1;
        assertEquals(Byte.toUnsignedInt(byteBuffer.get(structure.offset(0, lastIndex))), buffer.getU8(0, lastIndex));
    }


    @Test
    void testGetU8NegativeIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getU8(0, -1));
    }


    @Test
    void testGetU8IndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getU8(0, NUM_ELEMENTS));
    }


    @Test
    void testGetU8FieldIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getU8(3, 0));
    }


    @Test
    void testGetU8MaxValue() {
        byteBuffer.put(structure.offset(0, 0), (byte) 0xFF);
        assertEquals(255, buffer.getU8(0, 0));
    }


    @Test
    void testGetU8MinValue() {
        byteBuffer.put(structure.offset(0, 0), (byte) 0x00);
        assertEquals(0, buffer.getU8(0, 0));
    }


    @Test
    void testConstructorInvalidBufferSize() {
        byte[] invalidData = new byte[structure.totalSize() * NUM_ELEMENTS - 1];
        ByteBuffer invalidByteBuffer = ByteBuffer.wrap(invalidData);
        assertThrows(IllegalArgumentException.class, () -> new StructuredBuffer(structure, invalidByteBuffer));
    }
}
