package ch.epfl.rechor;

/**
 * Utility class for checking method preconditions.
 *
 * @author Ben Fall (373176)
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * Checks whether a condition is true.
     * If the condition is false, throws an IllegalArgumentException.
     *
     * @param shouldBeTrue the condition to check
     * @throws IllegalArgumentException if shouldBeTrue is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
