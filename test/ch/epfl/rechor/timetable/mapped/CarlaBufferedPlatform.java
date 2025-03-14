package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarlaBufferedPlatform {

    @Test
    void worksWithExampleData() {
        // Table des chaînes avec les noms des quais
        List<String> stringTable = List.of(
                "70",    // index 0
                "1AB",   // index 1
                "A"      // index 2
        );

        // Création du buffer avec les données des quais
        byte[] platformData = {
                // Quai "70" de la gare 0
                0x00, 0x00,  // NAME_ID = 0 ("70")
                0x00, 0x00,  // STATION_ID = 0

                // Quai "1AB" de la gare 1
                0x00, 0x01,  // NAME_ID = 1 ("1AB")
                0x00, 0x01,  // STATION_ID = 1

                // Quai "A" de la gare 2
                0x00, 0x02,  // NAME_ID = 2 ("A")
                0x00, 0x02   // STATION_ID = 2
        };

        BufferedPlatforms platforms = new BufferedPlatforms(
                stringTable,
                ByteBuffer.wrap(platformData)
        );

        assertEquals(3, platforms.size(), "Le nombre de quais devrait être 3");

        // Test du premier quai
        assertEquals("70", platforms.name(0));
        assertEquals(0, platforms.stationId(0));

        // Test du deuxième quai
        assertEquals("1AB", platforms.name(1));
        assertEquals(1, platforms.stationId(1));

        // Test du troisième quai
        assertEquals("A", platforms.name(2));
        assertEquals(2, platforms.stationId(2));
    }

    @Test
    void handlesEmptyData() {
        List<String> stringTable = List.of();
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, emptyBuffer);
        assertEquals(0, platforms.size(), "Une table vide devrait avoir une taille de 0");
    }

    @Test
    void handlesSinglePlatformMultipleStations() {
        List<String> stringTable = List.of("1");
        byte[] data = {
                0x00, 0x00,  // NAME_ID = 0 ("1")
                (byte)0xFF, (byte)0xFF   // STATION_ID = 65535 (max U16)
        };

        BufferedPlatforms platforms = new BufferedPlatforms(
                stringTable,
                ByteBuffer.wrap(data)
        );

        assertEquals("1", platforms.name(0));
        assertEquals(65535, platforms.stationId(0));
    }

    @Test
    void throwsForInvalidIndices() {
        List<String> stringTable = List.of("1", "2");
        byte[] data = {
                0x00, 0x00,  // NAME_ID = 0
                0x00, 0x00   // STATION_ID = 0
        };

        BufferedPlatforms platforms = new BufferedPlatforms(
                stringTable,
                ByteBuffer.wrap(data)
        );

        assertAll("Vérification des index invalides",
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> platforms.name(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> platforms.name(platforms.size())),
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> platforms.stationId(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> platforms.stationId(platforms.size()))
        );
    }

    @Test
    void handlesMalformedData() {
        List<String> stringTable = List.of("Test");

        // Données incomplètes (3 octets au lieu de 4)
        byte[] malformedData = {0x00, 0x00, 0x00};

        assertThrows(IllegalArgumentException.class, () -> {
            new BufferedPlatforms(stringTable, ByteBuffer.wrap(malformedData));
        }, "Des données malformées devraient lever une IllegalArgumentException");
    }

    @Test
    void handlesSpecialPlatformNames() {
        List<String> stringTable = List.of(
                "1-2",      // Quai combiné
                "3A/B",     // Quai avec sous-sections
                "BUS",      // Arrêt de bus
                "TGV-1"     // Quai TGV
        );

        byte[] data = {
                0x00, 0x00,  // NAME_ID = 0
                0x00, 0x01,  // STATION_ID = 1
                0x00, 0x01,  // NAME_ID = 1
                0x00, 0x01,  // STATION_ID = 1
                0x00, 0x02,  // NAME_ID = 2
                0x00, 0x01,  // STATION_ID = 1
                0x00, 0x03,  // NAME_ID = 3
                0x00, 0x01   // STATION_ID = 1
        };

        BufferedPlatforms platforms = new BufferedPlatforms(
                stringTable,
                ByteBuffer.wrap(data)
        );

        assertEquals("1-2", platforms.name(0));
        assertEquals("3A/B", platforms.name(1));
        assertEquals("BUS", platforms.name(2));
        assertEquals("TGV-1", platforms.name(3));
    }

    @Test
    void handlesMultiplePlatformsPerStation() {
        List<String> stringTable = List.of("1", "2", "3", "4");
        byte[] data = {
                // Tous les quais appartiennent à la même gare (ID=5)
                0x00, 0x00,  // NAME_ID = 0
                0x00, 0x05,  // STATION_ID = 5
                0x00, 0x01,  // NAME_ID = 1
                0x00, 0x05,  // STATION_ID = 5
                0x00, 0x02,  // NAME_ID = 2
                0x00, 0x05,  // STATION_ID = 5
                0x00, 0x03,  // NAME_ID = 3
                0x00, 0x05   // STATION_ID = 5
        };

        BufferedPlatforms platforms = new BufferedPlatforms(
                stringTable,
                ByteBuffer.wrap(data)
        );

        // Vérifie que tous les quais ont le même STATION_ID
        for (int i = 0; i < platforms.size(); i++) {
            assertEquals(5, platforms.stationId(i),
                    "Tous les quais devraient appartenir à la station 5");
        }
    }

    @Test
    public void testStructuredBuffer() {
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 05 00 04 00 02 00 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U16)
        );

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        assertEquals(2, structuredBuffer.size());
        assertEquals(5, structuredBuffer.getU16(0, 0)); // Losanna
        assertEquals(4, structuredBuffer.getU16(1, 0)); // Lausanne
        assertEquals(2, structuredBuffer.getU16(0, 1)); // Anet
        assertEquals(3, structuredBuffer.getU16(1, 1)); // Ins
    }

    @Test
    public void testBufferedStations() {
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 04 04 B6 CA 14 21 14 1F A1 00 06 04 DC CC 12 21 18 DA 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        List<String> stringTable = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals(2, stations.size());
        assertEquals("Lausanne", stations.name(0));
        assertEquals(6.629092, stations.longitude(0), 0.000001);
        assertEquals(46.516792, stations.latitude(0), 0.000001);

        assertEquals("Palézieux", stations.name(1));
        assertEquals(6.837875, stations.longitude(1), 0.000001);
        assertEquals(46.542764, stations.latitude(1), 0.000001);
    }


}