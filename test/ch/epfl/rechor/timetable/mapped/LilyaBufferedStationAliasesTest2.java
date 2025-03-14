package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.HexFormat;

public class LilyaBufferedStationAliasesTest2 {

    @Test
    public void testSize() {
        // Chaque enregistrement pour BufferedStationAliases occupe 4 octets (2 octets par champ)
        // Pour 2 enregistrements, il faut 2 * 4 = 8 octets.
        String hex = "00 05 00 04 00 02 00 03";
        byte[] bytes = HexFormat.ofDelimiter(" ").parseHex(hex);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> stringTable = List.of("A", "B", "C", "D", "E", "F", "G");
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        assertEquals(2, bsa.size(), "Le buffer devrait contenir 2 enregistrements.");
    }

    @Test
    public void testAliasAndStationName() {
        // Exemple de la consigne pour les noms alternatifs :
        // Hex : "00 05 00 04 00 02 00 03"
        // Record 0 : alias index = 5, station name index = 4.
        // Record 1 : alias index = 2, station name index = 3.
        String hex = "00 05 00 04 00 02 00 03";
        byte[] bytes = HexFormat.ofDelimiter(" ").parseHex(hex);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        // Créons une table de chaînes avec au moins 6 éléments.
        List<String> stringTable = List.of("Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta");
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        // Pour record 0 :
        // alias = index 5 -> "Zeta", stationName = index 4 -> "Epsilon"
        assertEquals("Zeta", bsa.alias(0));
        assertEquals("Epsilon", bsa.stationName(0));
        // Pour record 1 :
        // alias = index 2 -> "Gamma", stationName = index 3 -> "Delta"
        assertEquals("Gamma", bsa.alias(1));
        assertEquals("Delta", bsa.stationName(1));
    }

    @Test
    public void testInvalidBufferCapacity() {
        // Pour BufferedStationAliases, la capacité du buffer doit être un multiple de 4.
        ByteBuffer buffer = ByteBuffer.allocate(6); // 6 n'est pas un multiple de 4.
        List<String> stringTable = List.of("A", "B", "C");
        assertThrows(IllegalArgumentException.class, () -> new BufferedStationAliases(stringTable, buffer));
    }

    @Test
    public void testIndexOutOfBounds() {
        // Créer un buffer pour 1 enregistrement (4 octets)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        // Remplir le record : alias = 5, station name = 4.
        buffer.putShort(0, (short) 5);
        buffer.putShort(2, (short) 4);
        List<String> stringTable = List.of("A", "B", "C", "D", "E", "F");
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        // Accéder au deuxième enregistrement (indice 1) doit lever une exception.
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(1));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(1));
    }
}
