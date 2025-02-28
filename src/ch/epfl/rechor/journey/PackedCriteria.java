package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

public final class PackedCriteria {

    private PackedCriteria() {}

    // bits:
    // 63: 0
    // 62-51: departure time (or 0 if none) (12 bits)
    // 50-39: arrival time (12 bits)
    // 38-32: changes (7 bits)
    // 31-0: payload (32 bits)

    /**
     * Packs criteria without a departure time.
     * @param arrMins arrival time in minutes after midnight (must be between -240 and 2880)
     * @param changes number of changes (0 <= changes < 128)
     * @param payload payload (will be converted without sign extension)
     * @return packed criteria as a long.
     * @throws IllegalArgumentException if arrMins or changes are out of bounds.
     */
    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(arrMins >= -240 && arrMins < 2880);
        Preconditions.checkArgument(changes >= 0 && changes < 7);

        int elapsedTime = arrMins + 240; // Minutes since 8pm day before
        long packedArrival = (elapsedTime & 0xFFFL) << 39;
        long packedChanges = (changes & 0x7FL) << 32;
        long packedPayload = Integer.toUnsignedLong(payload);

        return packedArrival | packedChanges | packedPayload;
    }

    public static boolean hasDepMins(long criteria) {
        return ((criteria >> 51) & 0xFFF) != 0;
    }

    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int storedDep = (int) ((criteria >> 51) & 0xFFF);
        return (4095 - storedDep) - 240; // Takes (1's(!)) complement and converts to minutes since midnight
    }

    public static int arrMins(long criteria) {
        int storedArr = (int) ((criteria >> 39) & 0xFFF);
        return storedArr - 240; // Converts to minutes since midnight
    }

    public static int changes(long criteria) {
        return (int) ((criteria >> 32) & 0x7F);
    }

    public static int payload(long criteria) {
        return (int) (criteria & 0xFFFFFFFFL);
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));
        return (!hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2)) &&
                arrMins(criteria1) <= arrMins(criteria2) &&
                changes(criteria1) <= changes(criteria2);
    }

    public static long withoutDepMins(long criteria) {
        return criteria & ~(0xFFFL << 51);
    }

    public static long withDepMins(long criteria, int depMins) {
        Preconditions.checkArgument(depMins >= -240 && depMins < 2880);
        int storedDep = 4095 - (depMins + 240);
        return (withoutDepMins(criteria)) | ((long) storedDep << 51); // for safety lets clear the depMins first
    }

    public static long withAdditionalChange(long criteria) {
        int changes = changes(criteria);
        if (changes == 127) throw new IllegalArgumentException("Cannot add more changes to criteria!");
        return (criteria + (1L << 32)); // Risky if changes = 127 → overflows to 8th bit, hence the check in line 78
    }

    public static long withPayload(long criteria, int payload) {
        return (criteria & ~0xFFFFFFFFL) | Integer.toUnsignedLong(payload);
    }

}