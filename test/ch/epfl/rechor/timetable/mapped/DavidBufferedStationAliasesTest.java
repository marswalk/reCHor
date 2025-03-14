package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DavidBufferedStationAliasesTest {


    private BufferedStationAliases stations;


    @BeforeEach
    void setUp() {
        // Table des noms des gares
        List<String> stringTable = List.of("lozanne","Lausanne Gare", "EPFL","Ecublens VD EPFL", "2A","Voie 2A");


        // Chaîne hexadécimale représentant le buffer des stations
        String hexData = "00 00 00 01 " +  // Gare A
                "00 02 00 03 " +  // Gare B
                "00 04 00 05 ";


        // Conversion de la chaîne hexadécimale en tableau d'octets
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex(hexData.replace("\n", "").replace("//.*", "").trim());


        // Création du ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(bytes);


        // Initialisation de BufferedStations
        stations = new BufferedStationAliases(stringTable, buffer);
    }




    @Test
    void testAliasesName() {
        assertEquals("lozanne", stations.alias(0));
        assertEquals("EPFL", stations.alias(1));
        assertEquals("2A", stations.alias(2));
    }




    @Test
    void testStationName() {
        assertEquals("Lausanne Gare", stations.stationName(0));
        assertEquals("Ecublens VD EPFL", stations.stationName(1));
        assertEquals("Voie 2A", stations.stationName(2));
    }




    @Test
    void testInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> stations.stationName(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.stationName(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.alias(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.alias(3));
    }
}
