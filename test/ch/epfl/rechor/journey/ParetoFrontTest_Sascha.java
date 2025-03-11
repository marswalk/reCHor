package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static ch.epfl.rechor.journey.PackedCriteria.withDepMins;
import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

public class ParetoFrontTest_Sascha {
    private ParetoFront emptyFront;
    private ParetoFront singleElementFront;
    private ParetoFront multipleElementFront;

    @BeforeEach
    void setup() {
        emptyFront = ParetoFront.EMPTY;

        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(PackedCriteria.pack(480, 2, 0));
        singleElementFront = builder.build();

        ParetoFront.Builder multipleBuilder = new ParetoFront.Builder();
        multipleBuilder.add(PackedCriteria.pack(480, 2, 0));
        multipleBuilder.add(PackedCriteria.pack(481, 1, 0));
        multipleBuilder.add(PackedCriteria.pack(482, 0, 0));
        multipleElementFront = multipleBuilder.build();
    }

    @Test
    void testEmptyParetoFront() {
        assertEquals(0, emptyFront.size());
        assertThrows(NoSuchElementException.class, () -> emptyFront.get(480, 2));
    }

    @Test
    void testSingleElementFront() {
        assertEquals(1, singleElementFront.size());
        assertDoesNotThrow(() -> singleElementFront.get(480, 2));
        assertThrows(NoSuchElementException.class, () -> singleElementFront.get(500, 2));
    }

    @Test
    void testMultipleElementFront() {
        assertEquals(3, multipleElementFront.size());
        assertDoesNotThrow(() -> multipleElementFront.get(480, 2));
        assertDoesNotThrow(() -> multipleElementFront.get(481, 1));
        assertDoesNotThrow(() -> multipleElementFront.get(482, 0));
        assertThrows(NoSuchElementException.class, () -> multipleElementFront.get(500, 2));
    }

    @Test
    void testForEach() {
        AtomicLong count = new AtomicLong(0);
        multipleElementFront.forEach(value -> count.incrementAndGet());
        assertEquals(3, count.get());
    }

    @Test
    void testBuilderIsEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty());
    }

    @Test
    void testBuilderAddAndClear() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(480, 2, 0);
        assertFalse(builder.isEmpty());
        builder.clear();
        assertTrue(builder.isEmpty());
    }

    @Test
    void testBuilderAddAll() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(480, 2, 0);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(481, 1, 0);

        builder1.addAll(builder2);
        assertFalse(builder1.isEmpty());
        ParetoFront builtFront = builder1.build();
        assertEquals(2, builtFront.size());
    }


    @Test
    void testFullyDominates() {

        ParetoFront.Builder builder0 = new ParetoFront.Builder();
        builder0.add(withDepMins(PackedCriteria.pack(481, 1, 0), 480));

        ParetoFront.Builder compare1 = new ParetoFront.Builder();
        compare1.add(PackedCriteria.pack(480, 2, 0));
        compare1.add(PackedCriteria.pack(482, 1, 0));

        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(withDepMins(PackedCriteria.pack(480, 2, 0), 480));
        builder1.add(withDepMins(PackedCriteria.pack(482, 1, 0), 480));

        ParetoFront.Builder compare2 = new ParetoFront.Builder();
        compare2.add(PackedCriteria.pack(485, 3, 0));
        compare2.add(PackedCriteria.pack(481, 1, 0));

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(withDepMins(PackedCriteria.pack(485, 3, 0), 480));
        builder2.add(withDepMins(PackedCriteria.pack(481, 1, 0), 480));

        ParetoFront.Builder compare3 = new ParetoFront.Builder();
        compare3.add(withDepMins(PackedCriteria.pack(480, 1, 0), 480));

        ParetoFront.Builder builder3 = new ParetoFront.Builder();
        builder3.add(withDepMins(PackedCriteria.pack(480, 1, 0), 480));

        ParetoFront.Builder compare4 = new ParetoFront.Builder();
        compare4.add(PackedCriteria.pack(485, 3, 0));
        compare4.add(PackedCriteria.pack(480, 2, 0));

        ParetoFront.Builder builder4 = new ParetoFront.Builder();
        builder4.add(withDepMins(PackedCriteria.pack(485, 3, 0), 470));
        builder4.add(withDepMins(PackedCriteria.pack(480, 2, 0), 470));

        ParetoFront.Builder compare5 = new ParetoFront.Builder();
        compare5.add(PackedCriteria.pack(485, 3, 0));
        compare5.add(PackedCriteria.pack(480, 2, 0));

        ParetoFront.Builder compare6 = new ParetoFront.Builder();

        ParetoFront.Builder builder5 = new ParetoFront.Builder();
        builder5.add(withDepMins(PackedCriteria.pack(485, 3, 0), 480));
        builder5.add(withDepMins(PackedCriteria.pack(480, 2, 0), 480));

        assertFalse(builder0.fullyDominates(compare1, 0));
        assertFalse(builder1.fullyDominates(compare2, 0));
        assertFalse(builder2.fullyDominates(compare3, 0));
        assertTrue(builder1.fullyDominates(compare4, 0));
        assertTrue(builder4.fullyDominates(compare5, 0));
        assertTrue(builder4.fullyDominates(compare6, 475));
    }

    // Sample tuples (packed criteria) used in tests:
    // T1: arrival 480 minutes, 3 changes, payload 101.
    // T2: arrival 480 minutes, 4 changes, payload 102 (should be dominated by T1).
    // T3: arrival 481 minutes, 2 changes, payload 103 (incomparable with T1).
    // T4: arrival 480 minutes, 1 change, payload 104 (dominates T1 and T3).
    // T5: arrival 483 minutes, 0 changes, payload 105.
    // T6: arrival 484 minutes, 1 change, payload 106 (dominated by T5).

    private final long T1 = PackedCriteria.pack(480, 3, 101);
    private final long T2 = PackedCriteria.pack(480, 4, 102);
    private final long T3 = PackedCriteria.pack(481, 2, 103);
    private final long T4 = PackedCriteria.pack(480, 1, 104);
    private final long T5 = PackedCriteria.pack(483, 0, 105);
    private final long T6 = PackedCriteria.pack(484, 1, 106);

    /**
     * Test that a single tuple added to an empty builder appears in the final Pareto frontier.
     */
    @Test
    public void testSingleAdd() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(T1);
        ParetoFront pf = builder.build();
        assertEquals(1, pf.size(), "Frontier should contain 1 tuple after adding one tuple");
        // Verify that T1 is present (using get(arrMins, changes)).
        assertEquals(T1, pf.get(480, 3), "The stored tuple should be T1");
    }

    /**
     * Test that adding a tuple dominated by an existing tuple does not change the frontier.
     * Here, T2 is dominated by T1 because they have the same arrival but T2 has more changes.
     */
    @Test
    public void testAddDominated() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(T1);
        int sizeAfterT1 = builder.build().size();
        builder.add(T2);
        ParetoFront pf = builder.build();
        assertEquals(sizeAfterT1, pf.size(), "Adding a dominated tuple (T2) should not change the frontier size");
        // T2 should not be retrievable.
        assertThrows(NoSuchElementException.class, () -> pf.get(480, 4), "T2 should not be present in the frontier");
    }

    @Test
    void sizeTest(){
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(0x00_00_00_02_00_00_00_00L);
        builder.add(0x00_00_02_01_00_00_00_00L);
        ParetoFront paretoFront = builder.build();
        assertEquals(2, paretoFront.size());
    }

    @Test
    void sizeTestMany(){
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(0x00_00_01_1F_00_00_00_00L);
        builder.add(0x00_00_02_1E_00_00_00_00L);
        builder.add(0x00_00_03_1D_00_00_00_00L);
        builder.add(0x00_00_04_1C_00_00_00_00L);
        builder.add(0x00_00_05_1B_00_00_00_00L);
        builder.add(0x00_00_06_1A_00_00_00_00L);
        builder.add(0x00_00_07_19_00_00_00_00L);
        builder.add(0x00_00_08_18_00_00_00_00L);
        builder.add(0x00_00_09_17_00_00_00_00L);
        builder.add(0x00_00_0A_16_00_00_00_00L);
        builder.add(0x00_00_0B_15_00_00_00_00L);
        builder.add(0x00_00_0C_14_00_00_00_00L);
        builder.add(0x00_00_0D_13_00_00_00_00L);
        builder.add(0x00_00_0E_12_00_00_00_00L);
        builder.add(0x00_00_0F_11_00_00_00_00L);
        builder.add(0x00_00_11_0F_00_00_00_00L);
        builder.add(0x00_00_12_0E_00_00_00_00L);
        builder.add(0x00_00_13_0D_00_00_00_00L);
        builder.add(0x00_00_14_0C_00_00_00_00L);
        builder.add(0x00_00_15_0B_00_00_00_00L);
        builder.add(0x00_00_16_0A_00_00_00_00L);
        builder.add(0x00_00_17_09_00_00_00_00L);
        builder.add(0x00_00_18_08_00_00_00_00L);
        builder.add(0x00_00_19_07_00_00_00_00L);
        builder.add(0x00_00_1A_06_00_00_00_00L);
        builder.add(0x00_00_1B_05_00_00_00_00L);
        builder.add(0x00_00_1C_04_00_00_00_00L);
        builder.add(0x00_00_1D_03_00_00_00_00L);
        builder.add(0x00_00_1E_02_00_00_00_00L);
        builder.add(0x00_00_1F_01_00_00_00_00L);
        ParetoFront paretoFront = builder.build();
        assertEquals(30, paretoFront.size());
    }

    @Test
    void sizeTestZero(){
        ParetoFront.Builder builder = new ParetoFront.Builder();
        ParetoFront paretoFront = builder.build();
        assertEquals(0, paretoFront.size());
    }
}