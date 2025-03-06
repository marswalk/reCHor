package ch.epfl.rechor.journey;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.LongConsumer;

/**
 * Represents an immutable Pareto frontier of optimization criteria tuples.
 * Tuples are stored in lexicographical order (ascending arrival minutes, ascending changes,
 * ascending payload). Each tuple is packed into a {@code long} using criteria encoding conventions.
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

            // Copy all elements from the original front array (we are just copying the not null elements (efficiency?)
            // but the physical size is the same as the original)
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
         * Adds a tuple of packed criteria to the Pareto front.
         * <p>
         * This method adds the given packed tuple to the Pareto front only if it is not
         * dominated by any existing tuple in the front. If added, all tuples in the array tuples
         * that are dominated by this new tuple are removed.
         * <p>
         * The tuples in the Pareto front are always maintained in lexicographical order.
         *
         * @param packedTuple the packed criteria tuple to add to the Pareto front
         * @return this builder, to enable method chaining
         */
        public Builder add(long packedTuple) {
            // If the tuples is empty, simply add the tuple
            if (size == 0) {
                ensureCapacity(1);
                tuples[0] = packedTuple;
                size = 1;
                return this;
            }

            // Find the position where the new tuple should be inserted
            // by searching for the first element greater than the new tuple
            int insertPos = 0;
            boolean isDominated = false;

            // First, check if the new tuple is dominated by any existing tuple
            // We only need to check tuples that come before it in lexicographical order
            while (insertPos < size && tuples[insertPos] <= packedTuple) {
                // If an existing tuple dominates or equals the new one, no need to add it
                if (PackedCriteria.dominatesOrIsEqual(tuples[insertPos], packedTuple)) {
                    isDominated = true;
                    break;
                }
                insertPos++;
            }

            // If the new tuple is dominated, don't add it
            if (isDominated) {
                return this;
            }

            // Now we need to remove any tuples that are dominated by the new one
            // These will be tuples that come after the insertion position
            int writePos = insertPos;
            for (int readPos = insertPos; readPos < size; readPos++) {
                // Keep only tuples that are not dominated by the new one
                if (!PackedCriteria.dominatesOrIsEqual(packedTuple, tuples[readPos])) {
                    if (writePos != readPos) {
                        tuples[writePos] = tuples[readPos];
                    }
                    writePos++;
                }
            }

            // Update the size after removing dominated tuples
            int newSize = writePos + 1; // +1 for the new tuple

            // Ensure we have enough capacity for the new tuple
            ensureCapacity(newSize);

            // Shift elements to make room for the new tuple
            for (int i = size - 1; i >= insertPos; i--) {
                tuples[i + 1] = tuples[i];
            }

            // Insert the new tuple
            tuples[insertPos] = packedTuple;
            size = newSize;

            return this;
        }

        // Own helper methods for add method
        /**
         * Ensures that the internal array has at least the specified capacity.
         * If not, the array is resized to 1.5 times the required capacity.
         *
         * @param minCapacity the minimum capacity needed
         */
        private void ensureCapacity(int minCapacity) {
            if (tuples.length < minCapacity) {
                int newCapacity = Math.max(minCapacity, tuples.length * 3 / 2);
                tuples = Arrays.copyOf(tuples, newCapacity);
            }
        }

        /**
         * Adds a tuple with the given arrival time, number of changes, and payload to the Pareto front.
         * <p>
         * The tuple is only added if it is not dominated by any existing tuple in the front.
         * All tuples in the front that are dominated by this new tuple are removed.
         *
         * @param arrMins the arrival time in minutes after midnight
         * @param changes the number of changes
         * @param payload the payload associated with this tuple
         * @return this builder, to enable method chaining
         * @throws IllegalArgumentException if the arrival time or changes are invalid
         */
        public Builder add(int arrMins, int changes, int payload) {
            long packedTuple = PackedCriteria.pack(arrMins, changes, payload);
            return add(packedTuple);
        }

        /**
         * Adds all tuples from the given builder to this builder.
         * <p>
         * Each tuple is only added if it is not dominated by any existing tuple in this front.
         * Any tuples in this front that are dominated by a newly added tuple are removed.
         *
         * @param that the builder whose tuples should be added to this one
         * @return this builder, to enable method chaining
         */
        public Builder addAll(Builder that) {
            // If the other builder is empty, nothing to add
            if (that.isEmpty()) {
                return this;
            }
            // If this builder is empty, just copy all tuples from the other builder
            if (isEmpty()) {
                ensureCapacity(that.size);
                System.arraycopy(that.tuples, 0, tuples, 0, that.size);
                size = that.size;
                return this;
            }

            // Otherwise, add each tuple individually
            for (int i = 0; i < that.size; i++) {
                add(that.tuples[i]);
            }

            return this;
        }

        /**
         * Returns true if all tuples in the given builder, when assigned the specified
         * departure time, are dominated by at least one tuple in this builder.
         *
         * @param that the builder whose tuples should be checked for domination
         * @param depMins the departure time in minutes after midnight to assign to all tuples
         * @return true if all tuples in the given builder are dominated
         */
        public boolean fullyDominates(Builder that, int depMins) {
            // If the other builder is empty, it is trivially dominated
            if (that.isEmpty()) {
                return true;
            }

            // If this builder is empty, it cannot dominate anything
            if (isEmpty()) {
                return false;
            }

            // Check each tuple in the other builder
            for (int i = 0; i < that.size; i++) {
                long thatTuple = that.tuples[i];

                // Add departure time to the tuple if it doesn't have one
                if (!PackedCriteria.hasDepMins(thatTuple)) {
                    thatTuple = PackedCriteria.withDepMins(thatTuple, depMins);
                }

                // Check if this tuple is dominated by any tuple in this builder
                boolean isDominated = false;
                for (int j = 0; j < size; j++) {
                    if (PackedCriteria.dominatesOrIsEqual(tuples[j], thatTuple)) {
                        isDominated = true;
                        break;
                    }
                }

                // If any tuple is not dominated, return false
                if (!isDominated) {
                    return false;
                }
            }

            // All tuples are dominated
            return true;
        }

        /**
         * Applies the given action to each tuple in this builder.
         *
         * @param action the action to apply to each tuple
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        /**
         * Builds and returns an immutable Pareto front containing all the tuples
         * currently in this builder.
         *
         * @return immutable Pareto front
         */
        public ParetoFront build() {
            return new ParetoFront(java.util.Arrays.copyOf(tuples, size));
        }
    }
}
