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

    // Time constants
    private static final int MIN_MINUTES = -240;
    private static final int MAX_MINUTES = 2880;
    private static final int TIME_OFFSET = 240;
    private static final int TIME_MASK = 0xFFF;
    private static final int TIME_MAX_VALUE = 4095;

    // Changes constants
    private static final int MIN_CHANGES = 0;
    private static final int MAX_CHANGES = 127;
    private static final int CHANGES_MASK = 0x7F;

    // Bit positions
    private static final int DEP_TIME_SHIFT = 51;
    private static final int ARR_TIME_SHIFT = 39;
    private static final int CHANGES_SHIFT = 32;

    // Masks
    private static final long PAYLOAD_MASK = 0xFFFFFFFFFL;
    private static final long DEP_TIME_MASK = 0xFFFL << DEP_TIME_SHIFT;

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
        Preconditions.checkArgument(arrMins >= MIN_MINUTES && arrMins < MAX_MINUTES);
        Preconditions.checkArgument(changes >= MIN_CHANGES && changes < MAX_CHANGES + 1);

        int elapsedTime = arrMins + TIME_OFFSET; // Minutes since 8pm day before
        long packedArrival = (long) elapsedTime << ARR_TIME_SHIFT;
        long packedChanges = (long) changes << CHANGES_SHIFT;
        long packedPayload = Integer.toUnsignedLong(payload);

        return packedArrival | packedChanges | packedPayload;
    }

    /**
     * Checks if the criteria has a departure time.
     * @param criteria the packed criteria
     * @return true if the criteria has a departure time, false otherwise
     */
    public static boolean hasDepMins(long criteria) {
        return ((criteria >> DEP_TIME_SHIFT) & TIME_MASK) != 0;
    }

    /**
     * Extracts the departure time in minutes after midnight.
     * @param criteria the packed criteria
     * @return the departure time in minutes after midnight
     * @throws IllegalArgumentException if the criteria does not have a departure time
     */
    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int storedDep = (int) ((criteria >> DEP_TIME_SHIFT) & TIME_MASK);
        return (TIME_MAX_VALUE - storedDep) - TIME_OFFSET; // Takes (1's(!)) complement and converts to minutes since midnight
    }

    /**
     * Extracts the arrival time in minutes after midnight.
     * @param criteria the packed criteria
     * @return the arrival time in minutes after midnight
     */
    public static int arrMins(long criteria) {
        int storedArr = (int) ((criteria >> ARR_TIME_SHIFT) & TIME_MASK);
        return storedArr - TIME_OFFSET; // Converts to minutes since midnight
    }

    /**
     * Extracts the number of changes.
     * @param criteria the packed criteria
     * @return the number of changes
     */
    public static int changes(long criteria) {
        return (int) ((criteria >> CHANGES_SHIFT) & CHANGES_MASK);
    }

    /**
     * Extracts the payload.
     * @param criteria the packed criteria
     * @return the payload
     */
    public static int payload(long criteria) {
        return (int) (criteria & PAYLOAD_MASK);
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
        return criteria & ~DEP_TIME_MASK;
    }

    /**
     * Packs criteria with a (different) departure time.
     * @param criteria the packed criteria
     * @param depMins the departure time in minutes after midnight (must be between -240 and 2880)
     * @return packed criteria as a long
     * @throws IllegalArgumentException if depMins is out of bounds
     */
    public static long withDepMins(long criteria, int depMins) {
        Preconditions.checkArgument(depMins >= MIN_MINUTES && depMins < MAX_MINUTES);
        int storedDep = TIME_MAX_VALUE - (depMins + TIME_OFFSET);
        return (withoutDepMins(criteria)) | ((long) storedDep << DEP_TIME_SHIFT); // for safety lets clear the depMins first
    }

    /**
     * Packs criteria with an additional change.
     * @param criteria the packed criteria
     * @return packed criteria as a long
     * @throws IllegalArgumentException if the criteria already has 127 changes
     */
    public static long withAdditionalChange(long criteria) {
        int changes = changes(criteria);
        if (changes == MAX_CHANGES) throw new IllegalArgumentException("Cannot add more changes to criteria!");
        return (criteria + (1L << CHANGES_SHIFT)); // Risky if changes = 127 → overflows to 8th bit, hence the check
    }

    /**
     * Packs criteria with a (different) payload.
     * @param criteria the packed criteria
     * @param payload the payload
     * @return packed criteria as a long
     */
    public static long withPayload(long criteria, int payload) {
        return (criteria & ~PAYLOAD_MASK) | Integer.toUnsignedLong(payload);
    }
}