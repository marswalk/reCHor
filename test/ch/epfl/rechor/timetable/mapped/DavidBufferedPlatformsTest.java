package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DavidBufferedPlatformsTest {


    private BufferedPlatforms stations;


    @BeforeEach
    void setUp() {
        // Table des noms des gares
        List<String> stringTable = List.of("Voie 1", "quai B","Voie 2A");


        // Chaîne hexadécimale représentant le buffer des stations
        String hexData = "00 00 00 00 " +  // Gare A
                "00 01 00 03 " +  // Gare B
                "00 02 00 05 ";


        // Conversion de la chaîne hexadécimale en tableau d'octets
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex(hexData.replace("\n", "").replace("//.*", "").trim());


        // Création du ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(bytes);


        // Initialisation de BufferedStations
        stations = new BufferedPlatforms(stringTable, buffer);
    }




    @Test
    void testName() {
        assertEquals("Voie 1", stations.name(0));
        assertEquals("quai B", stations.name(1));
        assertEquals("Voie 2A", stations.name(2));
    }




    @Test
    void testStationId() {
        assertEquals(0, stations.stationId(0));
        assertEquals(3, stations.stationId(1));
        assertEquals(5, stations.stationId(2));
    }




    @Test
    void testInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.stationId(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.stationId(3));
    }
}