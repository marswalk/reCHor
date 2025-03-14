package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;


public class DavidBufferedStationsTest2 {


    private BufferedStations stations;




    @BeforeEach
    void setUp() {
        // Table des noms des gares
        List<String> stringTable = List.of("Gare A", "Gare B", "Gare C");


        // Chaîne hexadécimale représentant le buffer des stations
        String hexData = "00 00 00 25 4F 1B 05 CC 4B 63 " +  // Gare A
                "00 01 00 4E 55 A7 05 5B D1 53 " +  // Gare B
                "00 02 00 77 85 D2 05 2D 5A 82";


        // Conversion de la chaîne hexadécimale en tableau d'octets
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex(hexData.replace("\n", "").replace("//.*", "").trim());


        // Création du ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(bytes);


        // Initialisation de BufferedStations
        stations = new BufferedStations(stringTable, buffer);
    }




    @Test
    void testName() {
        assertEquals("Gare A", stations.name(0));
        assertEquals("Gare B", stations.name(1));
        assertEquals("Gare C", stations.name(2));
    }




    @Test
    void testLongitude() {
        assertEquals(0x00254f1b * Math.scalb(360, -32), stations.longitude(0), 1e-4);
        assertEquals(0x004e55a7 * Math.scalb(360, -32), stations.longitude(1), 1e-4);
        assertEquals(0x007785d2 * Math.scalb(360, -32), stations.longitude(2), 1e-4);
    }




    @Test
    void testLatitude() {
        assertEquals(0x05CC4B63 * Math.scalb(360, -32), stations.latitude(0), 1e-4);
        assertEquals(0x055BD153 * Math.scalb(360, -32), stations.latitude(1), 1e-4);
        assertEquals(0x052D5A82 * Math.scalb(360, -32), stations.latitude(2), 1e-4);
    }




    @Test
    void testInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.longitude(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.latitude(3));
    }




    @Test
    void nameThrowsExceptionForInvalidId() {
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(3));
    }




    @Test
    void longitudeThrowsExceptionForInvalidId() {
        assertThrows(IndexOutOfBoundsException.class, () -> stations.longitude(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.longitude(3));
    }




    @Test
    void latitudeThrowsExceptionForInvalidId() {
        assertThrows(IndexOutOfBoundsException.class, () -> stations.latitude(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.latitude(3));
    }




    @Test
    void sizeReturnsCorrectNumberOfStations() {
        assertEquals(3, stations.size());
    }
}
