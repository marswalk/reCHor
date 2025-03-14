package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarlaBufferedStationAliases {
    @Test
    void worksWithExampleData() {
        // Table des chaînes avec exemples de l'énoncé
        List<String> stringTable = List.of(
                "Losanna", "Lausanne",    // 0, 1
                "Soleure", "Solothurn",   // 2, 3
                "Anet", "Ins"            // 4, 5
        );

        // Création du buffer avec quelques paires (nom alternatif, nom original)
        byte[] aliasData = {
                // Losanna -> Lausanne
                0x00, 0x00,  // ALIAS_ID = 0 (Losanna)
                0x00, 0x01,  // STATION_NAME_ID = 1 (Lausanne)

                // Soleure -> Solothurn
                0x00, 0x02,  // ALIAS_ID = 2 (Soleure)
                0x00, 0x03,  // STATION_NAME_ID = 3 (Solothurn)

                // Anet -> Ins
                0x00, 0x04,  // ALIAS_ID = 4 (Anet)
                0x00, 0x05   // STATION_NAME_ID = 5 (Ins)
        };

        BufferedStationAliases aliases = new BufferedStationAliases(
                stringTable,
                ByteBuffer.wrap(aliasData)
        );

        assertEquals(3, aliases.size(), "Le nombre d'alias devrait être 3");

        // Test des paires nom alternatif -> nom original
        assertEquals("Losanna", aliases.alias(0));
        assertEquals("Lausanne", aliases.stationName(0));

        assertEquals("Soleure", aliases.alias(1));
        assertEquals("Solothurn", aliases.stationName(1));

        assertEquals("Anet", aliases.alias(2));
        assertEquals("Ins", aliases.stationName(2));
    }

    @Test
    void handlesEmptyData() {
        List<String> stringTable = List.of("Test");
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);

        BufferedStationAliases aliases = new BufferedStationAliases(
                stringTable,
                emptyBuffer
        );

        assertEquals(0, aliases.size(), "Une table vide devrait avoir une taille de 0");
    }

    @Test
    void handlesMaximumStringTableIndex() {
        // Crée une table de chaînes avec l'index maximum possible pour U16 (65535)
        List<String> largeStringTable = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {
            largeStringTable.add("Station" + i);
        }

        byte[] data = {
                (byte)0xFF, (byte)0xFF,  // ALIAS_ID = 65535
                (byte)0xFF, (byte)0xFF   // STATION_NAME_ID = 65535
        };

        BufferedStationAliases aliases = new BufferedStationAliases(
                largeStringTable,
                ByteBuffer.wrap(data)
        );

        assertEquals("Station65535", aliases.alias(0));
        assertEquals("Station65535", aliases.stationName(0));
    }

    @Test
    void throwsForInvalidIndices() {
        List<String> stringTable = List.of("Test1", "Test2");
        byte[] data = {
                0x00, 0x00,  // ALIAS_ID = 0
                0x00, 0x01   // STATION_NAME_ID = 1
        };

        BufferedStationAliases aliases = new BufferedStationAliases(
                stringTable,
                ByteBuffer.wrap(data)
        );

        assertAll("Vérification des index invalides",
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> aliases.alias(-1),
                        "Index négatif devrait lever une exception"),

                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> aliases.alias(aliases.size()),
                        "Index égal à size() devrait lever une exception"),

                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> aliases.stationName(-1),
                        "Index négatif devrait lever une exception"),

                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> aliases.stationName(aliases.size()),
                        "Index égal à size() devrait lever une exception")
        );
    }

    @Test
    void handlesMalformedData() {
        List<String> stringTable = List.of("Test");

        // Données incomplètes (3 octets au lieu de 4)
        byte[] malformedData = {0x00, 0x00, 0x00};

        assertThrows(IllegalArgumentException.class, () -> {
            new BufferedStationAliases(stringTable, ByteBuffer.wrap(malformedData));
        }, "Des données malformées devraient lever une IllegalArgumentException");
    }

    @Test
    void handlesNonAsciiNames() {
        List<String> stringTable = List.of(
                "Zürich", "Zürich HB",
                "Genève", "Genève-Aéroport",
                "München", "Munich"
        );

        byte[] data = {
                0x00, 0x00,  // Zürich -> Zürich HB
                0x00, 0x01,
                0x00, 0x02,  // Genève -> Genève-Aéroport
                0x00, 0x03,
                0x00, 0x04,  // München -> Munich
                0x00, 0x05
        };

        BufferedStationAliases aliases = new BufferedStationAliases(
                stringTable,
                ByteBuffer.wrap(data)
        );

        assertEquals("Zürich", aliases.alias(0));
        assertEquals("Zürich HB", aliases.stationName(0));
        assertEquals("Genève", aliases.alias(1));
        assertEquals("Genève-Aéroport", aliases.stationName(1));
        assertEquals("München", aliases.alias(2));
        assertEquals("Munich", aliases.stationName(2));
    }

//    @Test
//    void handlesNullArguments() {
//        List<String> stringTable = List.of("Test");
//        ByteBuffer buffer = ByteBuffer.wrap(new byte[4]);
//
//        assertAll("Test des arguments null",
//                () -> assertThrows(NullPointerException.class, () -> {
//                    new BufferedStationAliases(null, buffer);
//                }, "stringTable null devrait lever une NullPointerException"),
//
//                () -> assertThrows(NullPointerException.class, () -> {
//                    new BufferedStationAliases(stringTable, null);
//                }, "buffer null devrait lever une NullPointerException")
//        );
//    }
}