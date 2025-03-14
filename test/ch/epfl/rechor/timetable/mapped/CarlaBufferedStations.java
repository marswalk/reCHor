package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class CarlaBufferedStations {

    @Test
    void testSize() {
        List<String> stringList = Arrays.asList("Station A", "Station B", "Station C");

        // Define the structure with proper fields
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),  // Field 0: U16 for the name (index)
                Structure.field(1, Structure.FieldType.S32),  // Field 1: S32 for longitude
                Structure.field(2, Structure.FieldType.S32)   // Field 2: S32 for latitude
        );

        // Prepare a ByteBuffer with data (3 elements, each with a U16, S32, S32)
        ByteBuffer byteBuffer = ByteBuffer.allocate(3 * structure.totalSize());
        byteBuffer.putShort(0, (short) 0);  // String index for first station "Station A"
        byteBuffer.putInt(2, 1000);  // Longitude for the first station
        byteBuffer.putInt(6, 2000);  // Latitude for the first station
        byteBuffer.putShort(8, (short) 1);  // String index for second station "Station B"
        byteBuffer.putInt(10, 3000);  // Longitude for the second station
        byteBuffer.putInt(14, 4000);  // Latitude for the second station
        byteBuffer.putShort(16, (short) 2);  // String index for third station "Station C"
        byteBuffer.putInt(18, 5000);  // Longitude for the third station
        byteBuffer.putInt(22, 6000);  // Latitude for the third station

        // Wrap the buffer to the correct position
        byteBuffer.flip();

        // Pass the structure and byte buffer to BufferedStations
        BufferedStations bufferedStations = new BufferedStations(stringList, byteBuffer);

        // Test that the size method returns the correct number of stations
        assertEquals(3, bufferedStations.size(), "Size of buffered stations should be 3");
    }

//    @Test
//    void testName() {
//        List<String> stringList = Arrays.asList("Station A", "Station B", "Station C");
//
//        // Define the structure with proper fields
//        Structure structure = new Structure(
//                Structure.field(0, Structure.FieldType.U16),  // Field 0: U16 for the name (index)
//                Structure.field(1, Structure.FieldType.S32),  // Field 1: S32 for longitude
//                Structure.field(2, Structure.FieldType.S32)   // Field 2: S32 for latitude
//        );
//
//        // Prepare ByteBuffer with data
//        ByteBuffer byteBuffer = ByteBuffer.allocate(3 * structure.totalSize());
//        byteBuffer.putShort(0, (short) 0);  // Station A
//        byteBuffer.putInt(2, 1000);  // Longitude
//        byteBuffer.putInt(6, 2000);  // Latitude
//        byteBuffer.putShort(8, (short) 1);  // Station B
//        byteBuffer.putInt(10, 3000);  // Longitude
//        byteBuffer.putInt(14, 4000);  // Latitude
//        byteBuffer.putShort(16, (short) 2);  // Station C
//        byteBuffer.putInt(18, 5000);  // Longitude
//        byteBuffer.putInt(22, 6000);  // Latitude
//        byteBuffer.flip();
//
//        // Pass the structure and byte buffer to BufferedStations
//        BufferedStations bufferedStations = new BufferedStations(stringList, byteBuffer);
//
//        // Test that the name method returns the correct station name
//        assertEquals("Station A", bufferedStations.name(0), "Name of station 0 should be Station A");
//        assertEquals("Station B", bufferedStations.name(1), "Name of station 1 should be Station B");
//        assertEquals("Station C", bufferedStations.name(2), "Name of station 2 should be Station C");
//    }

//    @Test
//    void testLongitude() {
//        List<String> stringList = Arrays.asList("Station A", "Station B", "Station C");
//
//        // Define the structure with proper fields
//        Structure structure = new Structure(
//                Structure.field(0, Structure.FieldType.U16),  // Field 0: U16 for the name (index)
//                Structure.field(1, Structure.FieldType.S32),  // Field 1: S32 for longitude
//                Structure.field(2, Structure.FieldType.S32)   // Field 2: S32 for latitude
//        );
//
//        // Prepare ByteBuffer with data
//        ByteBuffer byteBuffer = ByteBuffer.allocate(3 * structure.totalSize());
//        byteBuffer.putShort(0, (short) 0);  // Station A
//        byteBuffer.putInt(2, 1000);  // Longitude (before scaling)
//        byteBuffer.putInt(6, 2000);  // Latitude
//        byteBuffer.putShort(8, (short) 1);  // Station B
//        byteBuffer.putInt(10, 3000);  // Longitude
//        byteBuffer.putInt(14, 4000);  // Latitude
//        byteBuffer.putShort(16, (short) 2);  // Station C
//        byteBuffer.putInt(18, 5000);  // Longitude
//        byteBuffer.putInt(22, 6000);  // Latitude
//        byteBuffer.flip();
//
//        // Pass the structure and byte buffer to BufferedStations
//        BufferedStations bufferedStations = new BufferedStations(stringList, byteBuffer);
//
//        // Test that the longitude method returns the correct longitude after scaling
//        assertEquals(1000 * BufferedStations.CONVERSION, bufferedStations.longitude(0),
//                "Longitude for station 0 should be scaled correctly");
//        assertEquals(3000 * BufferedStations.CONVERSION, bufferedStations.longitude(1),
//                "Longitude for station 1 should be scaled correctly");
//        assertEquals(5000 * BufferedStations.CONVERSION, bufferedStations.longitude(2),
//                "Longitude for station 2 should be scaled correctly");
//    }
//
//    @Test
//    void testLatitude() {
//        List<String> stringList = Arrays.asList("Station A", "Station B", "Station C");
//
//        // Define the structure with proper fields
//        Structure structure = new Structure(
//                Structure.field(0, Structure.FieldType.U16),  // Field 0: U16 for the name (index)
//                Structure.field(1, Structure.FieldType.S32),  // Field 1: S32 for longitude
//                Structure.field(2, Structure.FieldType.S32)   // Field 2: S32 for latitude
//        );
//
//        // Prepare ByteBuffer with data
//        ByteBuffer byteBuffer = ByteBuffer.allocate(3 * structure.totalSize());
//        byteBuffer.putShort(0, (short) 0);  // Station A
//        byteBuffer.putInt(2, 1000);  // Longitude
//        byteBuffer.putInt(6, 2000);  // Latitude
//        byteBuffer.putShort(8, (short) 1);  // Station B
//        byteBuffer.putInt(10, 3000);  // Longitude
//        byteBuffer.putInt(14, 4000);  // Latitude
//        byteBuffer.putShort(16, (short) 2);  // Station C
//        byteBuffer.putInt(18, 5000);  // Longitude
//        byteBuffer.putInt(22, 6000);  // Latitude
//        byteBuffer.flip();
//
//        // Pass the structure and byte buffer to BufferedStations
//        BufferedStations bufferedStations = new BufferedStations(stringList, byteBuffer);
//
//        // Test that the latitude method returns the correct latitude after scaling
//        assertEquals(2000 * BufferedStations.CONVERSION, bufferedStations.latitude(0),
//                "Latitude for station 0 should be scaled correctly");
//        assertEquals(4000 * BufferedStations.CONVERSION, bufferedStations.latitude(1),
//                "Latitude for station 1 should be scaled correctly");
//        assertEquals(6000 * BufferedStations.CONVERSION, bufferedStations.latitude(2),
//                "Latitude for station 2 should be scaled correctly");
//    }


    @Test
    public void testBufferedStationsWithValidData() {
        // Données aplaties pour les gares (comme dans l'énoncé)
        byte[] stationData = new byte[]{
                0x00, 0x04,  // NAME_ID = 4 (index dans stringTable)
                0x04, (byte) 0xB6, (byte) 0xCA, 0x14,  // LON = 0x04B6CA14 = 79088148
                0x21, 0x14, 0x1F, (byte) 0xA1,  // LAT = 0x21141FA1 = 1945668001

                0x00, 0x06,  // NAME_ID = 6 (index dans stringTable)
                0x04, (byte) 0xDC, (byte) 0xCC, 0x12,  // LON = 0x04DCCC12 = 81607826
                0x21, 0x18, (byte) 0xDA, 0x03   // LAT = 0x2118DA03 = 1945901571
        };

        // Création du ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(stationData);

        // Table des chaînes pour la correspondance des noms
        List<String> stringTable = List.of("Genève", "Zurich", "Bern", "Lausanne", "Lausanne", "Fribourg", "Palézieux");

        // Création de l'instance de BufferedStations
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        // Vérifications pour Lausanne
        assertEquals("Lausanne", stations.name(0));
        assertEquals(6.629092, stations.longitude(0), 0.000001);
        assertEquals(46.516792, stations.latitude(0), 0.000001);

        // Vérifications pour Palézieux
        assertEquals("Palézieux", stations.name(1));
        assertEquals(6.837875, stations.longitude(1), 0.000001);
        assertEquals(46.542764, stations.latitude(1), 0.000001);
    }

    @Test
    public void testBufferedStationsWithInvalidData() {
        // Données avec valeurs corrompues (ex: index de nom hors limites)
        byte[] badData = new byte[]{
                0x00, (byte) 0xFF,  // NAME_ID = 255 (ce qui est hors limites)
                0x04, 0x00, 0x00, 0x00,  // LON = 0x04000000 (valeur suspecte)
                0x21, 0x00, 0x00, 0x00   // LAT = 0x21000000 (valeur suspecte)
        };

        ByteBuffer buffer = ByteBuffer.wrap(badData);
        List<String> stringTable = List.of("Genève", "Zurich", "Bern");

        // Vérification que la création ne plante pas
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        // Vérification que l'accès à un nom invalide déclenche une exception
        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(0));

        // Vérification que les coordonnées renvoient une valeur (même si elle est étrange)
        assertTrue(Double.isFinite(stations.longitude(0)));
        assertTrue(Double.isFinite(stations.latitude(0)));
    }
    @Test
    void bufferedStationsWorksWithExample() {
        // Création de la table des chaînes
        List<String> stringTable = List.of(
                "", "", "", "", "Lausanne", "", "Palézieux"  // indices 0-6
        );

        // Création du buffer avec les données de l'exemple
        byte[] stationsData = new byte[] {
                // Première gare (Lausanne)
                0x00, 0x04,                                  // nameIndex = 4
                0x04, (byte)0xb6, (byte)0xca, 0x14,         // longitude = 0x04b6ca14
                0x21, 0x14, 0x1f, (byte)0xa1,               // latitude = 0x21141fa1

                // Deuxième gare (Palézieux)
                0x00, 0x06,                                  // nameIndex = 6
                0x04, (byte)0xdc, (byte)0xcc, 0x12,         // longitude = 0x04dccc12
                0x21, 0x18, (byte)0xda, 0x03                // latitude = 0x2118da03
        };

        ByteBuffer buffer = ByteBuffer.wrap(stationsData);
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        // Test de la taille
        assertEquals(2, stations.size(), "Le nombre de gares devrait être 2");

        // Tests pour Lausanne (index 0)
        assertEquals("Lausanne", stations.name(0),
                "Le nom de la première gare devrait être Lausanne");
        assertEquals(6.629092, stations.longitude(0), 1e-6,
                "La longitude de Lausanne est incorrecte");
        assertEquals(46.516792, stations.latitude(0), 1e-6,
                "La latitude de Lausanne est incorrecte");

        // Tests pour Palézieux (index 1)
        assertEquals("Palézieux", stations.name(1),
                "Le nom de la deuxième gare devrait être Palézieux");
        assertEquals(6.837875, stations.longitude(1), 1e-6,
                "La longitude de Palézieux est incorrecte");
        assertEquals(46.542764, stations.latitude(1), 1e-6,
                "La latitude de Palézieux est incorrecte");
    }

    @Test
    void bufferedStationsHandlesInvalidIndex() {
        List<String> stringTable = List.of("Lausanne", "Palézieux");
        byte[] emptyData = new byte[0];
        ByteBuffer emptyBuffer = ByteBuffer.wrap(emptyData);

        BufferedStations stations = new BufferedStations(stringTable, emptyBuffer);

        assertThrows(IndexOutOfBoundsException.class,
                () -> stations.name(-1),
                "Un index négatif devrait lever une exception");

        assertThrows(IndexOutOfBoundsException.class,
                () -> stations.name(stations.size()),
                "Un index égal à size() devrait lever une exception");
    }

    @Test
    void bufferedStationsHandlesEmptyData() {
        List<String> stringTable = List.of();
        byte[] emptyData = new byte[0];
        ByteBuffer emptyBuffer = ByteBuffer.wrap(emptyData);

        BufferedStations stations = new BufferedStations(stringTable, emptyBuffer);

        assertEquals(0, stations.size(),
                "Une table vide devrait avoir une taille de 0");
    }

    @Test
    void testMaxValues() {
        List<String> stringTable = List.of("Station");
        byte[] data = new byte[] {
                0x00, 0x00,                                  // nameIndex = 0
                (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,  // longitude max
                (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF   // latitude max
        };

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertTrue(Double.isFinite(stations.longitude(0)),
                "La longitude maximale devrait être un nombre fini");
        assertTrue(Double.isFinite(stations.latitude(0)),
                "La latitude maximale devrait être un nombre fini");
    }

    @Test
    void testMinValues() {
        List<String> stringTable = List.of("Station");
        byte[] data = new byte[] {
                0x00, 0x00,  // nameIndex = 0
                0x00, 0x00, 0x00, 0x00,  // longitude min
                0x00, 0x00, 0x00, 0x00   // latitude min
        };

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertTrue(Double.isFinite(stations.longitude(0)),
                "La longitude minimale devrait être un nombre fini");
        assertTrue(Double.isFinite(stations.latitude(0)),
                "La latitude minimale devrait être un nombre fini");
    }

    @Test
    void testLargeNumberOfStations() {
        // Créer une grande table de chaînes
        List<String> stringTable = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            stringTable.add("Station" + i);
        }

        // Créer des données pour 1000 stations
        byte[] data = new byte[1000 * 10]; // 10 octets par station
        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (int i = 0; i < 1000; i++) {
            buffer.putShort((short)i);  // nameIndex
            buffer.putInt(i);           // longitude
            buffer.putInt(i);           // latitude
        }
        buffer.flip();

        BufferedStations stations = new BufferedStations(stringTable, buffer);
        assertEquals(1000, stations.size(),
                "Devrait gérer un grand nombre de stations");
    }

    @Test
    void testMalformedDataThrowsException() {
        List<String> stringTable = List.of("Station");

        // Données incomplètes (seulement 9 octets au lieu de 10)
        byte[] incompleteData = new byte[] {
                0x00, 0x00,  // nameIndex (U16)
                0x00, 0x00, 0x00, 0x00,  // longitude (S32)
                0x00, 0x00, 0x00         // latitude (incomplète, manque 1 octet)
        };

        ByteBuffer buffer = ByteBuffer.wrap(incompleteData);

        // Vérifie que la création de BufferedStations lance une exception
        assertThrows(IllegalArgumentException.class,
                () -> new BufferedStations(stringTable, buffer),
                "Doit lever une exception si les données sont incomplètes"
        );
    }

    @Test
    void testStringTableLimits() {
        // Table des chaînes avec valeur maximale d'index U16
        List<String> largeStringTable = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {  // 2^16
            largeStringTable.add("Station" + i);
        }

        byte[] data = new byte[] {
                (byte)0xFF, (byte)0xFF,  // nameIndex max (65535)
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations stations = new BufferedStations(largeStringTable, buffer);

        assertEquals("Station65535", stations.name(0),
                "Devrait gérer l'index maximum de la table des chaînes");
    }

    @Test
    void testNonAsciiStationNames() {
        List<String> stringTable = List.of("Zürich HB", "Genève", "Château-d'Œx");
        byte[] data = new byte[] {
                0x00, 0x00,  // nameIndex = 0
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,

                0x00, 0x01,  // nameIndex = 1
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,

                0x00, 0x02,  // nameIndex = 2
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals("Zürich HB", stations.name(0),
                "Devrait gérer les caractères avec umlaut");
        assertEquals("Genève", stations.name(1),
                "Devrait gérer les caractères accentués");
        assertEquals("Château-d'Œx", stations.name(2),
                "Devrait gérer les caractères spéciaux");
    }

    @Test
    void testBoundaryCoordinates() {
        List<String> stringTable = List.of("Station");
        byte[] data = new byte[] {
                // Station à l'équateur
                0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,  // longitude 0
                0x00, 0x00, 0x00, 0x00,  // latitude 0

                // Station au méridien de Greenwich
                0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,  // longitude 0
                0x20, 0x00, 0x00, 0x00   // latitude quelconque
        };

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals(0.0, stations.longitude(0), 1e-6,
                "Devrait gérer la longitude 0");
        assertEquals(0.0, stations.latitude(0), 1e-6,
                "Devrait gérer la latitude 0");
    }
}