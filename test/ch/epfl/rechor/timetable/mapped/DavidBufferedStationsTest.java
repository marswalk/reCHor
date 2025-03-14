package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;


import static org.junit.jupiter.api.Assertions.*;


class DavidBufferedStationsTest {


    private List<String> stringTable;
    private ByteBuffer byteBuffer;
    private BufferedStations bufferedStations;


    @BeforeEach
    void setUp() {
        stringTable = List.of("Lausanne", "Palézieux", "Genève", "Zurich", "Bern");


        Structure stationStructure = new Structure(
                Structure.field(0, Structure.FieldType.U16), // NAME_ID (index de chaîne)
                Structure.field(1, Structure.FieldType.S32), // Longitude
                Structure.field(2, Structure.FieldType.S32)  // Latitude
        );


        // Création d'un buffer avec 3 gares
        byteBuffer = ByteBuffer.allocate(10 * 3); // 10 octets par gare (3 gares)


        // Gare 1: Lausanne (index 0), Longitude: 6.629092, Latitude: 46.516792
        byteBuffer.putShort((short) 0);  // Index "Lausanne"
        byteBuffer.putInt(79088148);     // Longitude en unité anonyme
        byteBuffer.putInt(46516792);     // Latitude en unité anonyme


        // Gare 2: Palézieux (index 1), Longitude: 6.837875, Latitude: 46.542764
        byteBuffer.putShort((short) 1);
        byteBuffer.putInt(88378750);
        byteBuffer.putInt(46542764);


        // Gare 3: Genève (index 2), Longitude: 6.143158, Latitude: 46.204391
        byteBuffer.putShort((short) 2);
        byteBuffer.putInt(61431580);
        byteBuffer.putInt(46204391);


        byteBuffer.rewind();
        bufferedStations = new BufferedStations(stringTable, byteBuffer);
    }


    @Test
    void testValidConstruction() {
        assertNotNull(bufferedStations);
        assertEquals(3, bufferedStations.size(), "Le nombre de gares doit être 3");
    }


    @Test
    void testName() {
        assertEquals("Lausanne", bufferedStations.name(0));
        assertEquals("Palézieux", bufferedStations.name(1));
        assertEquals("Genève", bufferedStations.name(2));
    }


    @Test
    void testLongitude() {
        double expectedLongitude1 = 79088148 * (360.0 / (1L << 32));
        double expectedLongitude2 = 88378750 * (360.0 / (1L << 32));
        double expectedLongitude3 = 61431580 * (360.0 / (1L << 32));


        assertEquals(expectedLongitude1, bufferedStations.longitude(0), 1e-6);
        assertEquals(expectedLongitude2, bufferedStations.longitude(1), 1e-6);
        assertEquals(expectedLongitude3, bufferedStations.longitude(2), 1e-6);
    }


    @Test
    void testLatitude() {
        double expectedLatitude1 = 46516792 * (360.0 / (1L << 32));
        double expectedLatitude2 = 46542764 * (360.0 / (1L << 32));
        double expectedLatitude3 = 46204391 * (360.0 / (1L << 32));


        assertEquals(expectedLatitude1, bufferedStations.latitude(0), 1e-6);
        assertEquals(expectedLatitude2, bufferedStations.latitude(1), 1e-6);
        assertEquals(expectedLatitude3, bufferedStations.latitude(2), 1e-6);
    }




    @Test
    void testIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.name(3));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.longitude(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.longitude(3));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.latitude(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.latitude(3));
    }


    @Test
    void testEmptyStringTable() {
        List<String> emptyTable = List.of();
        BufferedStations emptyStations = new BufferedStations(emptyTable, byteBuffer);


        assertThrows(IndexOutOfBoundsException.class, () -> emptyStations.name(0));
    }


    @Test
    void testRandomStations() {
        Random random = new Random();
        int numStations = 1000;


        // Création d'une table de 1000 chaînes aléatoires
        List<String> randomNames = random.ints(numStations, 0, Integer.MAX_VALUE)
                .mapToObj(Integer::toString)
                .toList();


        // Création d'un buffer pour 1000 gares
        ByteBuffer randomBuffer = ByteBuffer.allocate(10 * numStations);


        for (int i = 0; i < numStations; i++) {
            randomBuffer.putShort((short) i); // Index de la chaîne
            randomBuffer.putInt(random.nextInt()); // Longitude
            randomBuffer.putInt(random.nextInt()); // Latitude
        }
        randomBuffer.rewind();


        BufferedStations randomBufferedStations = new BufferedStations(randomNames, randomBuffer);


        assertEquals(numStations, randomBufferedStations.size());


        for (int i = 0; i < numStations; i++) {
            assertEquals(randomNames.get(i), randomBufferedStations.name(i));
        }
    }


    @Test
    void testAllPossibleU16Names() {
        int numNames = 65536; // Toutes les valeurs possibles de U16


        List<String> names = IntStream.range(0, numNames)
                .mapToObj(i -> "Station" + i)
                .toList();


        ByteBuffer testBuffer = ByteBuffer.allocate(10 * numNames);


        for (int i = 0; i < numNames; i++) {
            testBuffer.putShort((short) i);
            testBuffer.putInt(0); // Longitude fictive
            testBuffer.putInt(0); // Latitude fictive
        }
        testBuffer.rewind();


        BufferedStations stations = new BufferedStations(names, testBuffer);


        for (int i = 0; i < numNames; i++) {
            assertEquals("Station" + i, stations.name(i));
        }
    }




    @Test
    void testExample() {
        List<String> stringList = List.of("0", "1", "2", "3", "Lausanne", "Palezieux");


        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 04 04 b6 ca 14 21 14 1f a1 00 06 04 dc cc 12 21 18 da 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);


        BufferedStations bufferedStations = new BufferedStations(stringList, buffer);


        assertEquals("Lausanne", bufferedStations.name(0));
        // Je suis le GOOOOOAAAAT des échecs (ps: pas moi, Gartin59)
        assertEquals(6.629092, (Math.round(bufferedStations.longitude(0)*1_000_000))/1_000_000.0);
    }


}
