package ch.epfl.rechor;

/**
 * Utility class for packing and unpacking intervals of integers into a single int.
 * The 24 most significant bits represent the start (inclusive) of the interval,
 * and the 8 least significant bits represent its length.
 * The class is non-instantiable.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class PackedRange {

    private PackedRange() {}

    /**
     * Packs an interval of integers into a single int.
     * The 24 most significant bits represent the start (inclusive) of the interval,
     * and the 8 least significant bits represent its length.
     *
     * @param startInclusive the start value (inclusive)
     * @param endExclusive the end value (exclusive)
     * @return an int packing the interval
     * @throws IllegalArgumentException if start or length cannot be represented
     */
    public static int pack(int startInclusive, int endExclusive) {
        Preconditions.checkArgument(startInclusive <= 0xFFFFFF);
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument((length >= 0) && (length <= 255));
        // preconditions are verified in the pack Bits32_24_8
        return Bits32_24_8.pack(startInclusive, length);
    }

    /**
     * Returns the length of a packed interval.
     *
     * @param interval the packed interval
     * @return the length of the interval
     */
    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Returns the start of a packed interval.
     *
     * @param interval the packed interval
     * @return the start value (inclusive)
     */
    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Returns the end of a packed interval.
     *
     * @param interval the packed interval
     * @return the end value (exclusive)
     */
    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);
    }
}