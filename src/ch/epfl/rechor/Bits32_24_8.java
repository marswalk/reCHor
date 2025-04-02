package ch.epfl.rechor;

/**
 * Utility class for packing and unpacking 24-bit and 8-bit values into a single 32-bit integer.
 * <ul>
 * <li>The 24 most significant bits are used for the 24-bit value, and the 8 least significant bits are used for the 8-bit value.</li>
 * <li>The 24-bit value is extracted by shifting the 32-bit integer to the right by 8 bits.</li>
 * <li>The 8-bit value is extracted by masking the 32-bit integer with 0xFF.</li>
 * <li>The 24-bit value is packed by shifting it to the left by 8 bits and combining it with the 8-bit value.</li>
 * <li>The 24-bit value must fit in 24 bits, and the 8-bit value must fit in 8 bits.</li>
 * </ul>
 * The class is non-instantiable.
 *
 *  @author Guanting Wen (392412)
 *  @author Ben Fall (373176)
 */
public class Bits32_24_8 {

    private Bits32_24_8() {}

    /**
     * Packs a 24-bit value and an 8-bit value into a single 32-bit integer.
     *
     * @param bits24 the 24 most significant bits (must fit in 24 bits)
     * @param bits8 the 8 least significant bits (must fit in 8 bits)
     * @return a 32-bit integer with bits24 as the 24 MSBs and bits8 as the 8 LSBs
     * @throws IllegalArgumentException if bits24 doesn't fit in 24 bits or bits8 doesn't fit in 8 bits
     */
    public static int pack(int bits24, int bits8) {
        // Check if bits24 fits in 24 bits (max value 0xFFFFFF)
        Preconditions.checkArgument(bits24 >= 0 && bits24 <= 0xFFFFFF);

        // Check if bits8 fits in 8 bits (max value 0xFF)
        Preconditions.checkArgument(bits8 >= 0 && bits8 <= 0xFF);

        // Shift bits24 to the left by 8 bits and combine with bits8
        return (bits24 << 8) | bits8;
    }

    /**
     * Extracts the 24 most significant bits from a 32-bit integer.
     *
     * @param bits32 the 32-bit integer
     * @return the 24 most significant bits
     */
    public static int unpack24(int bits32) {
        // Shift right by 8 bits to get the 24 MSBs
        return (bits32 >>> 8) & 0xFFFFFF;
    }

    /**
     * Extracts the 8 least significant bits from a 32-bit integer.
     *
     * @param bits32 the 32-bit integer
     * @return the 8 least significant bits
     */
    public static int unpack8(int bits32) {
        // Mask with 0xFF to get the 8 LSBs
        return bits32 & 0xFF;
    }
}