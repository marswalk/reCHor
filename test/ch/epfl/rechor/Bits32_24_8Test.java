package ch.epfl.rechor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Bits32_24_8Test {

    @Test
    void testPackValidValues() {
        assertEquals(0x12345678, Bits32_24_8.pack(0x123456, 0x78));
        assertEquals(0x00000000, Bits32_24_8.pack(0x000000, 0x00));
        assertEquals(0xFFFFFFFF, Bits32_24_8.pack(0xFFFFFF, 0xFF));
    }

    @Test
    void testPackInvalidBits24() {
        assertThrows(IllegalArgumentException.class, () -> Bits32_24_8.pack(0x1000000, 0x00));
    }

    @Test
    void testPackInvalidBits8() {
        assertThrows(IllegalArgumentException.class, () -> Bits32_24_8.pack(0x000000, 0x100));
    }

    @Test
    void testUnpack24() {
        assertEquals(0x123456, Bits32_24_8.unpack24(0x12345678));
        assertEquals(0x000000, Bits32_24_8.unpack24(0x00000000));
        assertEquals(0xFFFFFF, Bits32_24_8.unpack24(0xFFFFFFFF));
    }

    @Test
    void testUnpack8() {
        assertEquals(0x78, Bits32_24_8.unpack8(0x12345678));
        assertEquals(0x00, Bits32_24_8.unpack8(0x00000000));
        assertEquals(0xFF, Bits32_24_8.unpack8(0xFFFFFFFF));
    }

    @Test
    void testPackUnpackRoundTrip() {
        int bits24 = 0x123456;
        int bits8 = 0x78;
        int packed = Bits32_24_8.pack(bits24, bits8);
        assertEquals(bits24, Bits32_24_8.unpack24(packed));
        assertEquals(bits8, Bits32_24_8.unpack8(packed));
    }
}
