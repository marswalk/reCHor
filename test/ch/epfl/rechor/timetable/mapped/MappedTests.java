package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static org.junit.jupiter.api.Assertions.*;

public class MappedTests {

    @Test
    void structureCreationWorks() {
        Structure structure = new Structure(
                field(0, U8),
                field(1, U16),
                field(2, S32)
        );
        assertEquals(7, structure.totalSize()); // 1 + 2 + 4 bytes
    }

    @Test
    void structureWithIncorrectFieldOrderThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new Structure(field(1, U8), field(0, U16)));
    }

    @Test
    void structureOffsetCalculationIsCorrect() {
        Structure structure = new Structure(
                field(0, U8),   // offset 0
                field(1, U16),  // offset 1
                field(2, S32)   // offset 3
        );
        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
        assertEquals(3, structure.offset(2, 0));

        // Next element
        assertEquals(7, structure.offset(0, 1));  // 7 = 0 + 1*totalSize
        assertEquals(8, structure.offset(1, 1));  // 8 = 1 + 1*totalSize
        assertEquals(10, structure.offset(2, 1)); // 10 = 3 + 1*totalSize
    }

    @Test
    void structuredBufferCreationWorks() {
        Structure structure = new Structure(field(0, U16));
        ByteBuffer buffer = ByteBuffer.allocate(10);
        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(5, sBuffer.size());
    }

    @Test
    void structuredBufferThrowsForInvalidSize() {
        Structure structure = new Structure(field(0, U16));
        ByteBuffer buffer = ByteBuffer.allocate(3); // Not a multiple of 2
        assertThrows(IllegalArgumentException.class, () ->
                new StructuredBuffer(structure, buffer));
    }

    @Test
    void structuredBufferGetMethodsWorkCorrectly() {
        Structure structure = new Structure(
                field(0, U8),
                field(1, U16),
                field(2, S32)
        );

        ByteBuffer buffer = ByteBuffer.allocate(14); // For 2 elements

        // Set values for first element
        buffer.put(0, (byte) 200);             // U8 (will be unsigned 200)
        buffer.putShort(1, (short) 60000);     // U16 (will be unsigned 60000)
        buffer.putInt(3, -100000);             // S32 (signed -100000)

        // Set values for second element
        buffer.put(7, (byte) -1);              // U8 (will be unsigned 255)
        buffer.putShort(8, (short) -1);        // U16 (will be unsigned 65535)
        buffer.putInt(10, Integer.MAX_VALUE);  // S32 (signed max int)

        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);

        // Test first element
        assertEquals(200, sBuffer.getU8(0, 0));
        assertEquals(60000, sBuffer.getU16(1, 0));
        assertEquals(-100000, sBuffer.getS32(2, 0));

        // Test second element
        assertEquals(255, sBuffer.getU8(0, 1));
        assertEquals(65535, sBuffer.getU16(1, 1));
        assertEquals(Integer.MAX_VALUE, sBuffer.getS32(2, 1));
    }

    @Test
    void bufferedStationsWorksCorrectly() {
        List<String> stringTable = Arrays.asList("Lausanne", "Genève", "Zürich");

        // Create a buffer with 2 stations
        ByteBuffer buffer = ByteBuffer.allocate(20); // 2 * (2 + 4 + 4) bytes

        // Correct conversion factor: multiply by (2^32/360) to convert to integer representation
        double toIntegerRepresentation = (1L << 32) / 360.0;

        // Station 0: Lausanne at 6.63, 46.52
        buffer.putShort(0, (short) 0);                            // NAME_ID: "Lausanne"
        buffer.putInt(2, (int)(6.63 * toIntegerRepresentation));  // LON
        buffer.putInt(6, (int)(46.52 * toIntegerRepresentation)); // LAT

        // Station 1: Genève at 6.14, 46.21
        buffer.putShort(10, (short) 1);                           // NAME_ID: "Genève"
        buffer.putInt(12, (int)(6.14 * toIntegerRepresentation)); // LON
        buffer.putInt(16, (int)(46.21 * toIntegerRepresentation));// LAT

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals(2, stations.size());
        assertEquals("Lausanne", stations.name(0));
        assertEquals(6.63, stations.longitude(0), 0.0001);
        assertEquals(46.52, stations.latitude(0), 0.0001);

        assertEquals("Genève", stations.name(1));
        assertEquals(6.14, stations.longitude(1), 0.0001);
        assertEquals(46.21, stations.latitude(1), 0.0001);
    }

    @Test
    void bufferedStationAliasesWorksCorrectly() {
        List<String> stringTable = Arrays.asList("Lausanne", "Losanna", "Geneva", "Genève");

        // Create a buffer with 2 aliases
        ByteBuffer buffer = ByteBuffer.allocate(8); // 2 * (2 + 2) bytes

        // Alias 0: Losanna -> Lausanne
        buffer.putShort(0, (short) 1);  // ALIAS_ID: "Losanna"
        buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Lausanne"

        // Alias 1: Geneva -> Genève
        buffer.putShort(4, (short) 2);  // ALIAS_ID: "Geneva"
        buffer.putShort(6, (short) 3);  // STATION_NAME_ID: "Genève"

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals(2, aliases.size());
        assertEquals("Losanna", aliases.alias(0));
        assertEquals("Lausanne", aliases.stationName(0));

        assertEquals("Geneva", aliases.alias(1));
        assertEquals("Genève", aliases.stationName(1));
    }

    @Test
    void bufferedPlatformsWorksCorrectly() {
        List<String> stringTable = Arrays.asList("1", "70", "Lausanne", "Zürich");

        // Create a buffer with 2 platforms
        ByteBuffer buffer = ByteBuffer.allocate(8); // 2 * (2 + 2) bytes

        // Platform 0: "1" at Lausanne (stationId 0)
        buffer.putShort(0, (short) 0);  // NAME_ID: "1"
        buffer.putShort(2, (short) 0);  // STATION_ID: 0 (Lausanne)

        // Platform 1: "70" at Zürich (stationId 1)
        buffer.putShort(4, (short) 1);  // NAME_ID: "70"
        buffer.putShort(6, (short) 1);  // STATION_ID: 1 (Zürich)

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals(2, platforms.size());
        assertEquals("1", platforms.name(0));
        assertEquals(0, platforms.stationId(0));

        assertEquals("70", platforms.name(1));
        assertEquals(1, platforms.stationId(1));
    }

    // Additional Structure tests

    @Test
    void structureWithEmptyFieldsWorks() {
        Structure structure = new Structure();
        assertEquals(0, structure.totalSize());
    }

    @Test
    void structureWithSingleFieldWorks() {
        Structure structure = new Structure(field(0, S32));
        assertEquals(4, structure.totalSize());
    }

    @Test
    void structureWithSameTypeFieldsWorks() {
        Structure structure = new Structure(
            field(0, U8),
            field(1, U8),
            field(2, U8)
        );
        assertEquals(3, structure.totalSize());
        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
        assertEquals(2, structure.offset(2, 0));
    }

    @Test
    void structureOffsetWithMultipleElementsIsCorrect() {
        Structure structure = new Structure(
            field(0, U8),
            field(1, U16)
        );
        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));

        assertEquals(3, structure.offset(0, 1));
        assertEquals(4, structure.offset(1, 1));

        assertEquals(6, structure.offset(0, 2));
        assertEquals(7, structure.offset(1, 2));
    }

    @Test
    void structureWithInvalidFieldIndexThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            Structure structure = new Structure(field(0, U8));
            structure.offset(1, 0); // Trying to access field index 1 which doesn't exist
        });
    }

    // Additional StructuredBuffer tests

    @Test
    void structuredBufferWithZeroElementsWorks() {
        Structure structure = new Structure(field(0, U8), field(1, U16));
        ByteBuffer buffer = ByteBuffer.allocate(0);
        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(0, sBuffer.size());
    }

    @Test
    void structuredBufferWithManyElementsWorks() {
        Structure structure = new Structure(field(0, U8));
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(1000, sBuffer.size());
    }

    @Test
    void structuredBufferThrowsOnOutOfBoundsAccess() {
        Structure structure = new Structure(field(0, U8));
        ByteBuffer buffer = ByteBuffer.allocate(3);
        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);

        // This should work
        sBuffer.getU8(0, 2);

        // This should throw
        assertThrows(IndexOutOfBoundsException.class, () -> sBuffer.getU8(0, 3));
    }

    @Test
    void structuredBufferAccessesLastElementCorrectly() {
        Structure structure = new Structure(field(0, U8));
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(4, (byte) 123);  // Last element

        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(123, sBuffer.getU8(0, 4));
    }

    @Test
    void structuredBufferHandlesDifferentFieldTypeCombinations() {
        Structure structure = new Structure(
            field(0, U16),
            field(1, S32),
            field(2, U8)
        );

        ByteBuffer buffer = ByteBuffer.allocate(14); // For 2 elements (2+4+1)*2

        buffer.putShort(0, (short) 1000);
        buffer.putInt(2, 2000000);
        buffer.put(6, (byte) 200);

        buffer.putShort(7, (short) 2000);
        buffer.putInt(9, -2000000);
        buffer.put(13, (byte) 100);

        StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);

        assertEquals(1000, sBuffer.getU16(0, 0));
        assertEquals(2000000, sBuffer.getS32(1, 0));
        assertEquals(200, sBuffer.getU8(2, 0));

        assertEquals(2000, sBuffer.getU16(0, 1));
        assertEquals(-2000000, sBuffer.getS32(1, 1));
        assertEquals(100, sBuffer.getU8(2, 1));
    }

    // Additional BufferedStations tests

    @Test
    void bufferedStationsWorksWithEmptyBuffer() {
        List<String> stringTable = Collections.emptyList();
        ByteBuffer buffer = ByteBuffer.allocate(0);

        BufferedStations stations = new BufferedStations(stringTable, buffer);
        assertEquals(0, stations.size());
    }

    @Test
    void bufferedStationsWorksWithSingleStation() {
        List<String> stringTable = Arrays.asList("Bern");
        ByteBuffer buffer = ByteBuffer.allocate(10); // 1 * (2 + 4 + 4)

        // Correct conversion factor: multiply by (2^32/360) to convert to integer representation
        double toIntegerRepresentation = (1L << 32) / 360.0;
        
        buffer.putShort(0, (short) 0);
        buffer.putInt(2, (int)(7.45 * toIntegerRepresentation));
        buffer.putInt(6, (int)(46.95 * toIntegerRepresentation));

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals(1, stations.size());
        assertEquals("Bern", stations.name(0));
        assertEquals(7.45, stations.longitude(0), 0.0001);
        assertEquals(46.95, stations.latitude(0), 0.0001);
    }

    @Test
    void bufferedStationsWorksWithManyStations() {
        List<String> stringTable = Arrays.asList("Lausanne", "Genève", "Zürich", "Bern", "Basel");
        ByteBuffer buffer = ByteBuffer.allocate(50); // 5 * (2 + 4 + 4)

        // Correct conversion factor: multiply by (2^32/360) to convert to integer representation
        double toIntegerRepresentation = (1L << 32) / 360.0;
        
        // Just populate first and last for brevity
        buffer.putShort(0, (short) 0);
        buffer.putInt(2, (int)(6.63 * toIntegerRepresentation));
        buffer.putInt(6, (int)(46.52 * toIntegerRepresentation));

        buffer.putShort(40, (short) 4);
        buffer.putInt(42, (int)(7.58 * toIntegerRepresentation));
        buffer.putInt(46, (int)(47.56 * toIntegerRepresentation));

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals(5, stations.size());
        assertEquals("Lausanne", stations.name(0));
        assertEquals("Basel", stations.name(4));
        assertEquals(7.58, stations.longitude(4), 0.0001);
    }

    @Test
    void bufferedStationsHandlesExtremeCoordinates() {
        List<String> stringTable = Arrays.asList("North Pole", "Equator");
        ByteBuffer buffer = ByteBuffer.allocate(20); // 2 * (2 + 4 + 4)

        // Correct conversion factor: multiply by (2^32/360) to convert to integer representation
        double toIntegerRepresentation = (1L << 32) / 360.0;
        
        buffer.putShort(0, (short) 0);
        buffer.putInt(2, (int)(0.0 * toIntegerRepresentation));
        buffer.putInt(6, (int)(90.0 * toIntegerRepresentation));

        buffer.putShort(10, (short) 1);
        buffer.putInt(12, (int)(0.0 * toIntegerRepresentation));
        buffer.putInt(16, (int)(0.0 * toIntegerRepresentation));

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals(90.0, stations.latitude(0), 0.0001);
        assertEquals(0.0, stations.latitude(1), 0.0001);
    }

    @Test
    void bufferedStationsThrowsOnOutOfBoundsAccess() {
        List<String> stringTable = Arrays.asList("Lausanne");
        ByteBuffer buffer = ByteBuffer.allocate(10); // 1 * (2 + 4 + 4)

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(1));
    }

    // Additional BufferedStationAliases tests

    @Test
    void bufferedStationAliasesWorksWithEmptyBuffer() {
        List<String> stringTable = Collections.emptyList();
        ByteBuffer buffer = ByteBuffer.allocate(0);

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);
        assertEquals(0, aliases.size());
    }

    @Test
    void bufferedStationAliasesWorksWithSingleAlias() {
        List<String> stringTable = Arrays.asList("Lausanne", "Losanna");
        ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)

        buffer.putShort(0, (short) 1);  // ALIAS_ID: "Losanna"
        buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Lausanne"

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals(1, aliases.size());
        assertEquals("Losanna", aliases.alias(0));
        assertEquals("Lausanne", aliases.stationName(0));
    }

    @Test
    void bufferedStationAliasesHandlesMultipleAliasesForSameStation() {
        List<String> stringTable = Arrays.asList("Zürich", "Zurich", "Zurigo", "Zurich HB");
        ByteBuffer buffer = ByteBuffer.allocate(12); // 3 * (2 + 2)

        // Three aliases for Zürich
        buffer.putShort(0, (short) 1);  // ALIAS_ID: "Zurich"
        buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Zürich"

        buffer.putShort(4, (short) 2);  // ALIAS_ID: "Zurigo"
        buffer.putShort(6, (short) 0);  // STATION_NAME_ID: "Zürich"

        buffer.putShort(8, (short) 3);  // ALIAS_ID: "Zurich HB"
        buffer.putShort(10, (short) 0); // STATION_NAME_ID: "Zürich"

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals(3, aliases.size());
        assertEquals("Zurich", aliases.alias(0));
        assertEquals("Zurigo", aliases.alias(1));
        assertEquals("Zurich HB", aliases.alias(2));

        // All point to "Zürich"
        assertEquals("Zürich", aliases.stationName(0));
        assertEquals("Zürich", aliases.stationName(1));
        assertEquals("Zürich", aliases.stationName(2));
    }

    @Test
    void bufferedStationAliasesThrowsOnOutOfBoundsAccess() {
        List<String> stringTable = Arrays.asList("Lausanne", "Losanna");
        ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> aliases.alias(1));
    }

    @Test
    void bufferedStationAliasesWorksWithComplexMapping() {
        List<String> stringTable = Arrays.asList(
            "Genève", "Geneva", "Genf", "Ginevra",
            "Zürich", "Zurich", "Zurigo"
        );

        ByteBuffer buffer = ByteBuffer.allocate(20); // 5 * (2 + 2)

        // Aliases for Genève
        buffer.putShort(0, (short) 1);  // ALIAS_ID: "Geneva"
        buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Genève"

        buffer.putShort(4, (short) 2);  // ALIAS_ID: "Genf"
        buffer.putShort(6, (short) 0);  // STATION_NAME_ID: "Genève"

        buffer.putShort(8, (short) 3);  // ALIAS_ID: "Ginevra"
        buffer.putShort(10, (short) 0); // STATION_NAME_ID: "Genève"

        // Aliases for Zürich
        buffer.putShort(12, (short) 5); // ALIAS_ID: "Zurich"
        buffer.putShort(14, (short) 4); // STATION_NAME_ID: "Zürich"

        buffer.putShort(16, (short) 6); // ALIAS_ID: "Zurigo"
        buffer.putShort(18, (short) 4); // STATION_NAME_ID: "Zürich"

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals(5, aliases.size());
        assertEquals("Geneva", aliases.alias(0));
        assertEquals("Genève", aliases.stationName(0));

        assertEquals("Zurigo", aliases.alias(4));
        assertEquals("Zürich", aliases.stationName(4));
    }

    // Additional BufferedPlatforms tests

    @Test
    void bufferedPlatformsWorksWithEmptyBuffer() {
        List<String> stringTable = Collections.emptyList();
        ByteBuffer buffer = ByteBuffer.allocate(0);

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);
        assertEquals(0, platforms.size());
    }

    @Test
    void bufferedPlatformsWorksWithSinglePlatform() {
        List<String> stringTable = Arrays.asList("12A", "Lausanne");
        ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)

        buffer.putShort(0, (short) 0);  // NAME_ID: "12A"
        buffer.putShort(2, (short) 0);  // STATION_ID: 0

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals(1, platforms.size());
        assertEquals("12A", platforms.name(0));
        assertEquals(0, platforms.stationId(0));
    }

    @Test
    void bufferedPlatformsHandlesMultiplePlatformsAtSameStation() {
        List<String> stringTable = Arrays.asList("1", "2", "3", "Bern");
        ByteBuffer buffer = ByteBuffer.allocate(12); // 3 * (2 + 2)

        // Three platforms at the same station
        buffer.putShort(0, (short) 0);  // NAME_ID: "1"
        buffer.putShort(2, (short) 0);  // STATION_ID: 0

        buffer.putShort(4, (short) 1);  // NAME_ID: "2"
        buffer.putShort(6, (short) 0);  // STATION_ID: 0

        buffer.putShort(8, (short) 2);  // NAME_ID: "3"
        buffer.putShort(10, (short) 0); // STATION_ID: 0

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals(3, platforms.size());
        assertEquals("1", platforms.name(0));
        assertEquals("2", platforms.name(1));
        assertEquals("3", platforms.name(2));

        // All at the same station
        assertEquals(0, platforms.stationId(0));
        assertEquals(0, platforms.stationId(1));
        assertEquals(0, platforms.stationId(2));
    }

    @Test
    void bufferedPlatformsThrowsOnOutOfBoundsAccess() {
        List<String> stringTable = Arrays.asList("1", "Lausanne");
        ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(1));
    }

    @Test
    void bufferedPlatformsWorksWithDifferentPlatformNameTypes() {
        List<String> stringTable = Arrays.asList("1", "3A", "4B/C", "12S", "Quai 7", "Lausanne", "Genève");
        ByteBuffer buffer = ByteBuffer.allocate(20); // 5 * (2 + 2)

        // Different platform name formats
        buffer.putShort(0, (short) 0);  // NAME_ID: "1" (numeric)
        buffer.putShort(2, (short) 0);  // STATION_ID: 0 (Lausanne)

        buffer.putShort(4, (short) 1);  // NAME_ID: "3A" (alphanumeric)
        buffer.putShort(6, (short) 0);  // STATION_ID: 0 (Lausanne)

        buffer.putShort(8, (short) 2);  // NAME_ID: "4B/C" (complex)
        buffer.putShort(10, (short) 0); // STATION_ID: 0 (Lausanne)

        buffer.putShort(12, (short) 3); // NAME_ID: "12S"
        buffer.putShort(14, (short) 1); // STATION_ID: 1 (Genève)

        buffer.putShort(16, (short) 4); // NAME_ID: "Quai 7" (descriptive)
        buffer.putShort(18, (short) 1); // STATION_ID: 1 (Genève)

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals(5, platforms.size());
        assertEquals("1", platforms.name(0));
        assertEquals("3A", platforms.name(1));
        assertEquals("4B/C", platforms.name(2));
        assertEquals("12S", platforms.name(3));
        assertEquals("Quai 7", platforms.name(4));

        assertEquals(0, platforms.stationId(0));
        assertEquals(0, platforms.stationId(1));
        assertEquals(0, platforms.stationId(2));
        assertEquals(1, platforms.stationId(3));
        assertEquals(1, platforms.stationId(4));
    }
}