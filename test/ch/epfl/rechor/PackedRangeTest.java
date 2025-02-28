package ch.epfl.rechor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PackedRangeTest {

    @Test
    public void testPackValidIntervalIsJustRight() {
        int start = 100;
        int end = 150;
        int packed = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(packed));
        assertEquals(50, PackedRange.length(packed));
        assertEquals(end, PackedRange.endExclusive(packed));
    }

    @Test
    public void testPackInvalidIntervalThrowsExceptionBecauseLogicMatters() {
        assertThrows(IllegalArgumentException.class, () -> {
            // end is not greater than start
            PackedRange.pack(50, 50);
        });
    }

    @Test
    public void testTheWackyRangeFailsWhenLengthIsTooLong() {
        // Length of 256 is too big for 8 bits (max is 255)
        int start = 0;
        int end = 256;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedRange.pack(start, end);
        });
    }

    @Test
    public void testStartInclusiveMethodIsAMasterMind() {
        int start = 12345;
        int end = start + 1;
        int packed = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(packed));
    }

    @Test
    public void testLengthShouldBeExactLikeAMathematician() {
        int start = 200;
        int end = 250;
        int packed = PackedRange.pack(start, end);
        assertEquals(end - start, PackedRange.length(packed));
    }

    @Test
    public void testEndExclusiveIsNotASecretAnymore() {
        int start = 300;
        int end = 350;
        int packed = PackedRange.pack(start, end);
        assertEquals(end, PackedRange.endExclusive(packed));
    }

    @Test
    public void testPackedRangeUsingExtremeValuesIsAlmostCrazy() {
        // Using high start values to test the upper limits.
        int start = 0xFFFFFF - 100;
        int end = 0xFFFFFF + 1;
        int packed = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(packed));
        assertEquals(101, PackedRange.length(packed));
        assertEquals(end, PackedRange.endExclusive(packed));
    }

    @Test
    public void testPackMethodWithMinimumValuesForItToBeCool() {
        int start = 0;
        int end = 1;
        int packed = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(packed));
        assertEquals(1, PackedRange.length(packed));
        assertEquals(end, PackedRange.endExclusive(packed));
    }

    @Test
    public void testPackShouldFailWhenNegativeStartIsTotallyBizarre() {
        int start = -1;
        int end = 10;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedRange.pack(start, end);
        });
    }

    @Test
    public void testPackShouldFailWhenLengthIsTooBigForItsOwnGood() {
        // Here length = 300, which is too high to fit in 8 bits.
        int start = 0;
        int end = 300;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedRange.pack(start, end);
        });
    }

    @Test
    public void testMultiplePackedRangesNoMonkeyBusinessAllowed() {
        // Loop through a series of intervals and verify each one.
        for (int i = 0; i < 100; i++) {
            int start = i;
            int length = (i % 255) + 1; // keep length in the range [1, 255]
            int end = start + length;
            int packed = PackedRange.pack(start, end);
            assertEquals(start, PackedRange.startInclusive(packed));
            assertEquals(length, PackedRange.length(packed));
            assertEquals(end, PackedRange.endExclusive(packed));
        }
    }
}