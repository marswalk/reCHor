package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.util.List;

public class MyBufferedStationsTest2 {

    /**
     * Crée un ByteBuffer pour n enregistrements de 10 octets chacun.
     */
    private ByteBuffer createBuffer(int n) {
        return ByteBuffer.allocate(n * 10);
    }

    @Test
    public void testSize() {
        // Créer un buffer pour 2 enregistrements (2 * 10 = 20 octets)
        ByteBuffer bb = createBuffer(2);
        List<String> stringTable = List.of("StationA", "StationB");
        BufferedStations bs = new BufferedStations(stringTable, bb);
        assertEquals(2, bs.size(), "Le buffer devrait contenir 2 enregistrements.");
    }

    @Test
    public void testName() {
        // Créer un buffer pour 2 enregistrements (20 octets)
        ByteBuffer bb = createBuffer(2);
        // Écriture des indices de chaîne dans le champ NAME_ID (champ 0, U16)
        bb.putShort(0, (short) 0);   // Enregistrement 0, champ NAME_ID à l'offset 0
        bb.putShort(10, (short) 2);    // Enregistrement 1, champ NAME_ID à l'offset 10
        List<String> stringTable = List.of("StationA", "StationB", "StationC", "StationD");
        BufferedStations bs = new BufferedStations(stringTable, bb);
        assertEquals("StationA", bs.name(0));
        assertEquals("StationC", bs.name(1));
    }

    @Test
    public void testLongitude() {
        ByteBuffer bb = createBuffer(2);
        // Pour station 0, l'offset du champ LONGITUDE (champ 1) est 2 (0-1: name, 2-5: longitude).
        bb.putInt(2, 1048576);   // 1048576 * (360/2^32) = 1048576 * (360/4294967296) = 360/4096 = 0.087890625 degrés.
        // Pour station 1, l'enregistrement commence à 10, donc champ LONGITUDE à offset 12.
        bb.putInt(12, -2097152); // -2097152 * (360/2^32) = -2 * 1048576 * (360/4294967296) = -0.17578125 degrés.
        // Remplissage des autres champs (name et latitude) avec 0.
        bb.putShort(0, (short) 0);   // station 0 : name
        bb.putInt(6, 0);             // station 0 : latitude
        bb.putShort(10, (short) 0);  // station 1 : name
        bb.putInt(16, 0);            // station 1 : latitude

        List<String> stringTable = List.of("MojanaLagoat");
        BufferedStations bs = new BufferedStations(stringTable, bb);
        double delta = 1e-6;
        assertEquals(0.087890625, bs.longitude(0), delta, "Longitude station 0 doit être 0.087890625 degré.");
        assertEquals(-0.17578125, bs.longitude(1), delta, "Longitude station 1 doit être -0.17578125 degré.");
    }

    @Test
    public void testLatitude() {
        ByteBuffer bb = createBuffer(2);
        // Pour station 0, l'offset du champ LATITUDE (champ 2) est 6 (0-1: name, 2-5: longitude, 6-9: latitude).
        bb.putInt(6, 524288);   // 524288 * (360/2^32) = 524288 * (360/4294967296) = 360/8192 = 0.0439453125 degrés.
        // Pour station 1, l'enregistrement commence à 10, donc champ LATITUDE à offset 16.
        bb.putInt(16, -524288); // -524288 * (360/2^32) = -0.0439453125 degrés.
        // Remplissage minimal pour les autres champs.
        bb.putShort(0, (short) 0);   // station 0 : name
        bb.putInt(2, 0);             // station 0 : longitude
        bb.putShort(10, (short) 0);  // station 1 : name
        bb.putInt(12, 0);            // station 1 : longitude

        List<String> stringTable = List.of("SamyLeGoat");
        BufferedStations bs = new BufferedStations(stringTable, bb);
        double delta = 1e-6;
        assertEquals(0.0439453125, bs.latitude(0), delta, "Latitude station 0 doit être 0.0439453125 degré.");
        assertEquals(-0.0439453125, bs.latitude(1), delta, "Latitude station 1 doit être -0.0439453125 degré.");
    }

    @Test
    public void testInvalidBufferCapacity() {
        // Créer un buffer dont la capacité n'est pas un multiple de 10 (par exemple, 15 octets)
        ByteBuffer bb = ByteBuffer.allocate(15);
        List<String> stringTable = List.of("StationA");
        assertThrows(IllegalArgumentException.class, () -> new BufferedStations(stringTable, bb));
    }

    @Test
    public void testIndexOutOfBounds() {
        // Créer un buffer pour 1 enregistrement.
        ByteBuffer bb = createBuffer(1);
        bb.putShort(0, (short) 0);
        bb.putInt(2, 0);
        bb.putInt(6, 0);
        List<String> stringTable = List.of("StationA");
        BufferedStations bs = new BufferedStations(stringTable, bb);
        // Accès à un enregistrement inexistant doit lever IndexOutOfBoundsException.
        assertThrows(IndexOutOfBoundsException.class, () -> bs.name(1));
        assertThrows(IndexOutOfBoundsException.class, () -> bs.longitude(1));
        assertThrows(IndexOutOfBoundsException.class, () -> bs.latitude(1));
    }
}
