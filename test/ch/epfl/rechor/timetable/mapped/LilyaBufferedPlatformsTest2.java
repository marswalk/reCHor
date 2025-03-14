package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.HexFormat;

public class LilyaBufferedPlatformsTest2 {

    @Test
    public void testSize() {
        // Chaque enregistrement pour BufferedPlatforms occupe 4 octets (2 pour le nom, 2 pour le stationId)
        // Pour 3 enregistrements, on doit avoir 3 * 4 = 12 octets.
        String hex = "00 00 00 00 00 01 00 02 00 03 00 04";
        byte[] bytes = HexFormat.ofDelimiter(" ").parseHex(hex);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        // La table de chaînes peut être arbitraire ici.
        List<String> stringTable = List.of("PlatformA", "PlatformB", "PlatformC", "PlatformD", "PlatformE");
        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        assertEquals(3, bp.size(), "Le buffer devrait contenir 3 enregistrements.");
    }

    @Test
    public void testNameAndStationId() {
        // Créer 2 enregistrements :
        // Record 0 :
        //   - NAME_ID = 0 (00 00)
        //   - STATION_ID = 0 (00 00)
        // Record 1 :
        //   - NAME_ID = 1 (00 01)
        //   - STATION_ID = 2 (00 02)
        String hex = "00 00 00 00 00 01 00 02";
        byte[] bytes = HexFormat.ofDelimiter(" ").parseHex(hex);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> stringTable = List.of("PlatformA", "PlatformB", "PlatformC", "PlatformD");
        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        // Vérification pour le premier enregistrement.
        assertEquals("PlatformA", bp.name(0));
        assertEquals(0, bp.stationId(0));
        // Pour le second enregistrement.
        assertEquals("PlatformB", bp.name(1));
        assertEquals(2, bp.stationId(1));
    }

    @Test
    public void testInvalidBufferCapacity() {
        // La taille d'un enregistrement est 4, donc la capacité doit être multiple de 4.
        ByteBuffer buffer = ByteBuffer.allocate(10); // 10 n'est pas multiple de 4.
        List<String> stringTable = List.of("A", "B");
        assertThrows(IllegalArgumentException.class, () -> new BufferedPlatforms(stringTable, buffer));
    }

    @Test
    public void testIndexOutOfBounds() {
        // Créer un buffer pour 1 enregistrement (4 octets)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        // Remplir le record : NAME_ID = 0, STATION_ID = 1.
        buffer.putShort(0, (short) 0);
        buffer.putShort(2, (short) 1);
        List<String> stringTable = List.of("PlatformA", "PlatformB", "PlatformC");
        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        // L'accès au deuxième enregistrement (indice 1) doit lever une exception.
        assertThrows(IndexOutOfBoundsException.class, () -> bp.name(1));
        assertThrows(IndexOutOfBoundsException.class, () -> bp.stationId(1));
    }
}
