package ch.epfl.rechor.journey;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.LongConsumer;

/**
 * Represents an immutable Pareto frontier of optimization criteria tuples.
 * Tuples are stored in lexicographical order (ascending arrival minutes, ascending changes,
 * ascending payload). Each tuple is packed into a {@code long} using criteria encoding conventions.
 *
 * Packed criteria tuple format, packed in same format as PackedCriteria
 *
 * @see ParetoFront.Builder
 */
public final class ParetoFront {
    /**
     * Empty Pareto frontier instance.
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    private final long[] packedTuples;

    // Private constructor (immutable)
    private ParetoFront(long[] packedTuples) {
        this.packedTuples = packedTuples;
        // stores packedTuples without copying it
    }

    /**
     * Returns the number of tuples in the frontier.
     *
     * @return Size of the frontier (≥ 0)
     */
    public int size() {
        return packedTuples.length;
    }
    // each element of packedTuple represents a criteria tuple (type long)
    // either a tuple with 3 criteria (departure time, arrival time, changes) or with just two (arrival time and changes)

    /**
     * Retrieves the packed tuple matching the given arrival time and changes.
     *
     * @param arrMins Arrival time in minutes since midnight
     * @param changes Number of transfers
     * @return Packed criteria tuple
     * @throws NoSuchElementException If no matching tuple exists
     */
    public long get(int arrMins, int changes) {
        for (long tuple : packedTuples) {
            if (PackedCriteria.arrMins(tuple) == arrMins
                    && PackedCriteria.changes(tuple) == changes) {
                return tuple;
            }
        }
        throw new NoSuchElementException("No tuple with specified criteria");
    }

    /**
     * Performs an action for each tuple in lexicographical order (this being the order in which the tuples are stored in the array).
     *
     * @param action Consumer to process each packed tuple
     */
    public void forEach(LongConsumer action) {
        for (long tuple : packedTuples) {
            action.accept(tuple);
        }
    }


    // redefinition of toString to facilitate debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ParetoFront[");
        if (packedTuples.length == 0) {
            sb.append("empty");
        } else {
            sb.append("\n");
            for (long tuple : packedTuples) {
                sb.append("  • ")
                        .append("Arrival: ").append(formatTime(PackedCriteria.arrMins(tuple)))
                        .append(", Changes: ").append(PackedCriteria.changes(tuple))
                        .append(", Payload: ").append(PackedCriteria.payload(tuple))
                        .append("\n");
            }
        }
        return sb.append("]").toString();
    }

    private static String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }




    /**
     * Mutable builder for constructing {@code ParetoFront} instances.
     * Builder is a statistically nested class of ParetoFront.
     * Maintains tuples in lexicographical order and removes dominated entries during insertion.
     */
    public static final class Builder {
        private long[] tuples;
        // contains the criteria (tuples) in packed form and is "resized" as needed according to the methods, sorted lexicographically
        private int size;
        // to know the logical (effective) size of the boundary

        /** Constructs an empty builder. */
        public Builder() {
            tuples = new long[2];
            // For example, its initial capacity, when the boundary being built is empty, could be set to 2
            // makes sense that even so that in case the capacity is insufficiently great, we can for example
            // "increase" the array's size by a factor like 1.5
            // essentially then copying all tuples, adding the other one with lexicographical order preserved
            // if no capacity issue, then simpler and we can just use arraycopy
            size = 0;
        }

        /**
         * Creates a new builder with the same content as the given builder.
         * <p>
         * This constructor creates a deep copy of the given builder, ensuring that
         * modifications to either builder will not affect the other.
         *
         * @param that the builder to copy
         * @throws NullPointerException if the given builder is null
         */
        public Builder(Builder that) {
            Objects.requireNonNull(that, "Builder to copy cannot be null");

            // Copy the size
            this.size = that.size;

            // Create a new array with the same capacity as the original
            this.tuples = new long[that.tuples.length];

            // Copy all elements from the original front array
            if (this.size > 0) {
                System.arraycopy(that.tuples, 0, this.tuples, 0, that.size);
            }
        }


        /**
         * Checks if the frontier is empty.
         * @return {@code true} if no tuples exist
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Removes all tuples from the builder.
         * @return This builder (for chaining)
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Adds a packed tuple to the frontier if it is not dominated by existing entries.
         * Removes any entries dominated by the new tuple.
         *
         * @param packedTuple Packed criteria (arrival time, changes, payload)
         * @return This builder for method chaining
         */
        public Builder add(long packedTuple) {
            // Step 1: Find insertion index using binary search
            int idx = Arrays.binarySearch(tuples, 0, size, packedTuple);
            if (idx >= 0) return this; // Exact match exists (no action needed)
            idx = - (idx + 1); // Calculate insertion point

            // Step 2: Check dominance by existing elements before insertion point
            for (int i = 0; i < idx; i++) {
                if (dominates(tuples[i], packedTuple)) {
                    return this; // New tuple is dominated; exit
                }
            }

            // Step 3: Remove elements after idx that are dominated by the new tuple
            int newSize = idx;
            for (int i = idx; i < size; i++) {
                if (!dominates(packedTuple, tuples[i])) {
                    // Copy remaining non-dominated elements
                    int remaining = size - i;
                    System.arraycopy(tuples, i, tuples, newSize, remaining);
                    newSize += remaining;
                    break;
                }
            }

            // Step 4: Expand array if necessary
            ensureCapacity(newSize + 1);

            // Step 5: Insert new tuple and update size
            System.arraycopy(tuples, idx, tuples, idx + 1, newSize - idx);
            tuples[idx] = packedTuple;
            size = newSize + 1;

            return this;
        }




        // --- Helper Methods ---

        /**
         * Checks if tuple `a` dominates tuple `b` (a.arrival <= b.arrival and a.changes <= b.changes).
         */
        private boolean dominates(long a, long b) {
            int aArrival = (int) (a >> 40) & 0xFFFFFF; // Extract arrival from packed long
            int bArrival = (int) (b >> 40) & 0xFFFFFF;
            if (aArrival > bArrival) return false;

            int aChanges = (int) (a >> 24) & 0xFFFF; // Extract changes from packed long
            int bChanges = (int) (b >> 24) & 0xFFFF;
            return aChanges <= bChanges;
        }

        /**
         * Ensures the internal array has sufficient capacity.
         */
        private void ensureCapacity(int minCapacity) {
            if (minCapacity > tuples.length) {
                int newCapacity = Math.max(tuples.length * 2, minCapacity);
                tuples = Arrays.copyOf(tuples, newCapacity);
            }
        }






        /**
         * Builds an immutable {@code ParetoFront} from the current state.
         * @return Immutable Pareto frontier
         */
        public ParetoFront build() {
            return new ParetoFront(java.util.Arrays.copyOf(tuples, size));
        }
    }
}
