package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * Utility class for packing and unpacking journey criteria into a single long.
 * The 12 most significant bits represent the arrival time (in minutes after midnight),
 * the next 7 bits represent the number of changes, and the 32 least significant bits represent the payload.
 * The class is non-instantiable.
 *
 * <ul>
 *     Bits:
 *     <li>63: 0</li>
 *     <li>62-51: departure time (or 0 if none) (12 bits)</li>
 *     <li>50-39: arrival time (12 bits)</li>
 *     <li>38-32: changes (7 bits)</li>
 *     <li>31-0: payload (32 bits)</li>
 * </ul>
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class PackedCriteria {

    private PackedCriteria() {}

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
        Preconditions.checkArgument(changes >= 0 && changes < 128);

        int elapsedTime = arrMins + 240; // Minutes since 8pm day before
        long packedArrival = (elapsedTime & 0xFFFL) << 39;
        long packedChanges = (changes & 0x7FL) << 32;
        long packedPayload = Integer.toUnsignedLong(payload);

        return packedArrival | packedChanges | packedPayload;
    }

    /**
     * Checks if the criteria has a departure time.
     * @param criteria the packed criteria
     * @return true if the criteria has a departure time, false otherwise
     */
    public static boolean hasDepMins(long criteria) {
        return ((criteria >> 51) & 0xFFF) != 0;
    }

    /**
     * Extracts the departure time in minutes after midnight.
     * @param criteria the packed criteria
     * @return the departure time in minutes after midnight
     * @throws IllegalArgumentException if the criteria does not have a departure time
     */
    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int storedDep = (int) ((criteria >> 51) & 0xFFF);
        return (4095 - storedDep) - 240; // Takes (1's(!)) complement and converts to minutes since midnight
    }

    /**
     * Extracts the arrival time in minutes after midnight.
     * @param criteria the packed criteria
     * @return the arrival time in minutes after midnight
     */
    public static int arrMins(long criteria) {
        int storedArr = (int) ((criteria >> 39) & 0xFFF);
        return storedArr - 240; // Converts to minutes since midnight
    }

    /**
     * Extracts the number of changes.
     * @param criteria the packed criteria
     * @return the number of changes
     */
    public static int changes(long criteria) {
        return (int) ((criteria >> 32) & 0x7F);
    }

    /**
     * Extracts the payload.
     * @param criteria the packed criteria
     * @return the payload
     */
    public static int payload(long criteria) {
        return (int) (criteria & 0xFFFFFFFFL);
    }

    /**
     * Checks if the first criteria dominates the second criteria.
     * A criteria dominates another if it has a later arrival time, fewer changes, and a later departure time.
     * @param criteria1 the first packed criteria
     * @param criteria2 the second packed criteria
     * @return true if criteria1 dominates criteria2, false otherwise
     * @throws IllegalArgumentException if the two criteria have different departure time fields
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));
        return (!hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2)) &&
                arrMins(criteria1) <= arrMins(criteria2) &&
                changes(criteria1) <= changes(criteria2);
    }

    /**
     * Extracts the arrival time in minutes after midnight.
     * @param criteria the packed criteria
     * @return the arrival time in minutes after midnight
     */
    public static long withoutDepMins(long criteria) {
        return criteria & ~(0xFFFL << 51);
    }

    /**
     * Packs criteria with a (different) departure time.
     * @param criteria the packed criteria
     * @param depMins the departure time in minutes after midnight (must be between -240 and 2880)
     * @return packed criteria as a long
     * @throws IllegalArgumentException if depMins is out of bounds
     */
    public static long withDepMins(long criteria, int depMins) {
        Preconditions.checkArgument(depMins >= -240 && depMins < 2880);
        int storedDep = 4095 - (depMins + 240);
        return (withoutDepMins(criteria)) | ((long) storedDep << 51); // for safety lets clear the depMins first
    }

    /**
     * Packs criteria with an additional change.
     * @param criteria the packed criteria
     * @return packed criteria as a long
     * @throws IllegalArgumentException if the criteria already has 127 changes
     */
    public static long withAdditionalChange(long criteria) {
        int changes = changes(criteria);
        if (changes == 127) throw new IllegalArgumentException("Cannot add more changes to criteria!");
        return (criteria + (1L << 32)); // Risky if changes = 127 → overflows to 8th bit, hence the check in line 78
    }

    /**
     * Packs criteria with a (different) payload.
     * @param criteria the packed criteria
     * @param payload the payload
     * @return packed criteria as a long
     */
    public static long withPayload(long criteria, int payload) {
        return (criteria & ~0xFFFFFFFFL) | Integer.toUnsignedLong(payload);
    }

}