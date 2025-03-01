package ch.epfl.rechor.journey;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PackedCriteriaTest {
    private static final int MAX_ARRIVAL = 2879;
    private static final int MIN_ARRIVAL = -240;
    private static final int MAX_CHANGES = 127;
    private static final int MAX_PAYLOAD = 0xFFFFFFFF;

    @Nested
    class PackTests {
        @Test
        void packValidValues() {
            long packed = PackedCriteria.pack(120, 3, 0xCAFEBABE);
            assertEquals(120, PackedCriteria.arrMins(packed));
            assertEquals(3, PackedCriteria.changes(packed));
            assertEquals(0xCAFEBABE, PackedCriteria.payload(packed));
        }

        @ParameterizedTest
        @ValueSource(ints = { -241, 2880 })
        void packInvalidArrivalThrows(int invalidArrival) {
            assertThrows(IllegalArgumentException.class,
                    () -> PackedCriteria.pack(invalidArrival, 0, 0));
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 128 })
        void packInvalidChangesThrows(int invalidChanges) {
            assertThrows(IllegalArgumentException.class,
                    () -> PackedCriteria.pack(0, invalidChanges, 0));
        }
    }

    @Nested
    class DepartureTests {
        @Test
        void hasDepMinsDetectsPresence() {
            long withDep = PackedCriteria.withDepMins(0, 120);
            long withoutDep = PackedCriteria.pack(120, 0, 0);

            assertTrue(PackedCriteria.hasDepMins(withDep));
            assertFalse(PackedCriteria.hasDepMins(withoutDep));
        }

        @Test
        void depMinsRoundTrip() {
            long packed = PackedCriteria.withDepMins(0, 360); // 6:00 AM
            assertEquals(360, PackedCriteria.depMins(packed));
        }

        @Test
        void depMinsThrowsWhenMissing() {
            long noDep = PackedCriteria.pack(0, 0, 0);
            assertThrows(IllegalArgumentException.class,
                    () -> PackedCriteria.depMins(noDep));
        }
    }

    @Nested
    class FieldExtractionTests {
        @Test
        void extractAllFields() {
            long packed = PackedCriteria.withDepMins(
                    PackedCriteria.pack(120, 5, 0x12345678),
                    360
            );

            assertEquals(360, PackedCriteria.depMins(packed));
            assertEquals(120, PackedCriteria.arrMins(packed));
            assertEquals(5, PackedCriteria.changes(packed));
            assertEquals(0x12345678, PackedCriteria.payload(packed));
        }

        @Test
        void extractBoundaryValues() {
            long packed = PackedCriteria.pack(MIN_ARRIVAL, MAX_CHANGES, MAX_PAYLOAD);
            assertEquals(MIN_ARRIVAL, PackedCriteria.arrMins(packed));
            assertEquals(MAX_CHANGES, PackedCriteria.changes(packed));
            assertEquals(MAX_PAYLOAD, PackedCriteria.payload(packed));
        }
    }

    @Nested
    class DominationTests {
        @ParameterizedTest
        @CsvSource({
                "360, 120, 2, 360, 120, 2, true",    // Equal
                "360, 120, 2, 300, 120, 2, true",   // Better departure
                "360, 120, 2, 360, 180, 2, true",    // Better arrival
                "360, 120, 2, 360, 120, 3, true",    // Better changes
                "360, 120, 2, 300, 180, 3, true",    // Better all
                "300, 120, 2, 360, 120, 2, false",  // Worse departure
                "300, 180, 3, 360, 120, 2, false"    // Worse all
        })
        void dominationScenarios(int dep1, int arr1, int chg1,
                                 int dep2, int arr2, int chg2,
                                 boolean expected) {
            long c1 = PackedCriteria.withDepMins(PackedCriteria.pack(arr1, chg1, 0), dep1);
            long c2 = PackedCriteria.withDepMins(PackedCriteria.pack(arr2, chg2, 0), dep2);

            assertEquals(expected, PackedCriteria.dominatesOrIsEqual(c1, c2));
        }

        @Test
        void mixedDeparturePresenceThrows() {
            long withDep = PackedCriteria.withDepMins(0, 120);
            long withoutDep = PackedCriteria.pack(120, 0, 0);

            assertThrows(IllegalArgumentException.class,
                    () -> PackedCriteria.dominatesOrIsEqual(withDep, withoutDep));
        }
    }

    @Nested
    class ModificationTests {
        @Test
        void withAdditionalChangeIncrements() {
            long original = PackedCriteria.pack(120, 5, 0);
            long modified = PackedCriteria.withAdditionalChange(original);
            assertEquals(6, PackedCriteria.changes(modified));
        }

        @Test
        void withAdditionalChangeThrowsAtMax() {
            long maxChanges = PackedCriteria.pack(0, MAX_CHANGES, 0);
            assertThrows(IllegalArgumentException.class,
                    () -> PackedCriteria.withAdditionalChange(maxChanges));
        }

        @Test
        void withDepMinsPreservesOtherFields() {
            long original = PackedCriteria.pack(120, 5, 0x12345678);
            long modified = PackedCriteria.withDepMins(original, 360);

            assertEquals(360, PackedCriteria.depMins(modified));
            assertEquals(120, PackedCriteria.arrMins(modified));
            assertEquals(5, PackedCriteria.changes(modified));
            assertEquals(0x12345678, PackedCriteria.payload(modified));
        }

        @Test
        void withoutDepMinsClearsOnlyDeparture() {
            long withDep = PackedCriteria.withDepMins(
                    PackedCriteria.pack(120, 5, 0x12345678),
                    360
            );
            long withoutDep = PackedCriteria.withoutDepMins(withDep);

            assertFalse(PackedCriteria.hasDepMins(withoutDep));
            assertEquals(120, PackedCriteria.arrMins(withoutDep));
            assertEquals(5, PackedCriteria.changes(withoutDep));
            assertEquals(0x12345678, PackedCriteria.payload(withoutDep));
        }
    }

    @Nested
    class EdgeCaseTests {
        @Test
        void minDepartureTime() {
            long packed = PackedCriteria.withDepMins(0, MIN_ARRIVAL);
            assertEquals(MIN_ARRIVAL, PackedCriteria.depMins(packed));
        }

        @Test
        void maxDepartureTime() {
            long packed = PackedCriteria.withDepMins(0, MAX_ARRIVAL);
            assertEquals(MAX_ARRIVAL, PackedCriteria.depMins(packed));
        }

        @Test
        void fullPayloadRange() {
            long packed = PackedCriteria.withPayload(0, MAX_PAYLOAD);
            assertEquals(MAX_PAYLOAD, PackedCriteria.payload(packed));
        }

        @Test
        void complementStorageVerification() {
            int depTime = 120;
            long packed = PackedCriteria.withDepMins(0, depTime);
            int storedValue = (int) ((packed >> 51) & 0xFFF);
            assertEquals(4095 - (depTime + 240), storedValue);
        }
    }
}
