package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.nio.ByteBuffer;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;


public class DavidAliasTest2 {
    private static final HexFormat HEX_FORMAT = HexFormat.ofDelimiter(" ");


    @Test
    void testValidAliases() {
        byte[] bytes = HEX_FORMAT.parseHex("00 05 00 03 00 04 00 04 00 02 00 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> stringTable = List.of("", "", "Ins", "Lausanne", "Soleure", "Losanna");
        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


        assertEquals("Losanna", aliases.alias(0));
        assertEquals("Soleure", aliases.alias(1));
        assertEquals("Ins", aliases.alias(2));
        assertEquals("Lausanne", aliases.stationName(0));
        assertEquals("Soleure", aliases.stationName(1));
        assertEquals("Lausanne", aliases.stationName(2));
    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 100})
    void testAliasIndexOutOfBounds(int id) {
        byte[] bytes = HEX_FORMAT.parseHex("00 05 00 04 00 02 00 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> stringTable = List.of("", "", "Ins", "Lausanne", "Soleure", "Losanna");
        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


        assertThrows(IndexOutOfBoundsException.class, () -> aliases.alias(id));
    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 100})
    void testStationNameIndexOutOfBounds(int id) {
        byte[] bytes = HEX_FORMAT.parseHex("00 05 00 04 00 02 00 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> stringTable = List.of("", "", "Ins", "Lausanne", "Soleure", "Losanna");
        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


        assertThrows(IndexOutOfBoundsException.class, () -> aliases.stationName(id));
    }


    @Test
    void testEmptyBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);
        List<String> stringTable = List.of();
        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


        assertThrows(IndexOutOfBoundsException.class, () -> aliases.alias(0));
        assertThrows(IndexOutOfBoundsException.class, () -> aliases.stationName(0));
    }


    @Test
    void testRandomAccessWithinBounds() {
        byte[] bytes = HEX_FORMAT.parseHex("00 05 00 04 00 02 00 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> stringTable = List.of("", "", "Ins", "Lausanne", "Soleure", "Losanna");
        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);
        Random random = new Random();


        for (int i = 0; i < 1000; i++) {
            int id = random.nextInt(2); // Génère un id entre 0 et 1


            assertDoesNotThrow(() -> aliases.alias(id));
            assertDoesNotThrow(() -> aliases.stationName(id));
        }
    }
}
