//package ch.epfl.rechor.journey;
//
//import ch.epfl.rechor.journey.PackedCriteria;
//import ch.epfl.rechor.journey.ParetoFront;
//import org.junit.jupiter.api.Test;
//
//import java.util.NoSuchElementException;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.function.LongConsumer;
//
//import static ch.epfl.rechor.PackedRange.pack;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class ParetoFrontTest_Adelie {
//    //Test Builder
//    // banques de critères
//    // (8h00, 3)
//    long tuple0 = PackedCriteria.pack(480, 3, 0);
//    // (8h00, 4)
//    long tuple1 = PackedCriteria.pack(480, 4, 0);
//    //(8h01, 2)
//    long tuple2 = PackedCriteria.pack(481, 2, 0);
//    //(8h02, 1)
//    long tuple3 = PackedCriteria.pack(482, 1, 0);
//    //(8h03, 0)
//    long tuple4 = PackedCriteria.pack(483, 0, 0);
//    //(8h04, 1)
//    long tuple5 = PackedCriteria.pack(484, 1, 0);
//
//    @Test
//    void AddWorksOnExamples() {
//        ParetoFront.Builder builder0 = new ParetoFront.Builder();
//
//        String expectedDisplay0 = "Builder is empty.";
//        String expectedDisplay1 = "Arrival : 480\n" +
//                "Changes : 3\n" + "---------------------------------------\n";
//        String expectedDisplay2 = expectedDisplay1 + "Arrival : 484\n" +
//                "Changes : 1\n" + "---------------------------------------\n";
//        String expectedDisplay3 = expectedDisplay1 + "Arrival : 481\n" +
//                "Changes : 2\n" + "---------------------------------------\n"
//                + "Arrival : 484\n" + "Changes : 1\n" + "---------------------------------------\n";
//        String expectedDisplay4 = expectedDisplay1 + "Arrival : 481\n" +
//                "Changes : 2\n" + "---------------------------------------\n" +
//                "Arrival : 482\n" + "Changes : 1\n" + "---------------------------------------\n";
//        String expectedDisplay5 = expectedDisplay4 + "Arrival : 483\n" +
//                "Changes : 0\n" + "---------------------------------------\n";
//
//        assertEquals(expectedDisplay0, builder0.toString());
//        assertEquals(expectedDisplay1, builder0.add(tuple0).toString());
//        assertEquals(expectedDisplay1, builder0.add(tuple1).toString());
//        assertEquals(expectedDisplay2, builder0.add(tuple5).toString());
//        assertEquals(expectedDisplay3, builder0.add(tuple2).toString());
//        assertEquals(expectedDisplay4, builder0.add(tuple3).toString());
//        assertEquals(expectedDisplay5, builder0.add(tuple4).toString());
//    }
//
//    @Test
//    void testBuilderDefaultConstructor() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        assertEquals(0, builder.numberOfTuples);
//        // Capacité initiale
//        assertEquals(2, builder.capacity);
//    }
//
//    @Test
//    void testIsNotEmpty() {
//        // il est réellement vide
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        assertTrue(builder.isEmpty());
//
//        builder.paretoBuilding[0] = tuple0;
//        builder.numberOfTuples = 1;
//        // Pas vide après ajout
//        assertFalse(builder.isEmpty());
//    }
//
//
//    @Test
//    void testClear() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.paretoBuilding[0] = PackedCriteria.pack(480, 3, 0);
//        builder.numberOfTuples = 1;
//
//        builder.clear();
//        assertTrue(builder.isEmpty());
//        assertEquals(0, builder.numberOfTuples);
//    }
//
//
//    @Test
//    void addWorksCorrectly() {
//        ParetoFront.Builder builder0 = new ParetoFront.Builder();
//        String expected = "Arrival : 480\n" +
//                "Changes : 3\n" + "---------------------------------------\n";
//        assertEquals(expected, builder0.add(480, 3, 0).toString());
//    }
//
//    @Test
//    void addWorksCorrectlyWithMoreTuples() {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        String expected = "Arrival : 480\n" +
//                "Changes : 3\n" + "---------------------------------------\n" +
//                "Arrival : 481\n" +
//                "Changes : 2\n" + "---------------------------------------\n" +
//                "Arrival : 482\n" +
//                "Changes : 1\n" + "---------------------------------------\n";
//        builder1.add(480, 3, 5);
//        builder1.add(481, 2, 70);
//        builder1.add(482, 1, 50);
//        assertEquals(expected, builder1.toString());
//    }
//
//    @Test
//    void addWorksCorrectlyWithMoreTuplesMessy() {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        String expected = "Arrival : 480\n" +
//                "Changes : 3\n" + "---------------------------------------\n" +
//                "Arrival : 481\n" +
//                "Changes : 2\n" + "---------------------------------------\n" +
//                "Arrival : 482\n" +
//                "Changes : 1\n" + "---------------------------------------\n";
//        builder1.add(481, 2, 70);
//        builder1.add(480, 3, 5);
//        builder1.add(482, 1, 50);
//        assertEquals(expected, builder1.toString());
//    }
//
//    @Test
//    void addWorksCorrectlyWithMoreTuplesMessy2() {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        String expected = "Arrival : 480\n" +
//                "Changes : 1\n" + "---------------------------------------\n";
//
//        builder1.add(481, 2, 70);
//        builder1.add(480, 3, 5);
//        builder1.add(482, 1, 50);
//        builder1.add(480, 1, 50);
//        assertEquals(expected, builder1.toString());
//    }
//
//    @Test
//    void addWorksCorrectlyWithNothing() {
//        ParetoFront.Builder builder0 = new ParetoFront.Builder();
//        String expected = "Builder is empty.";
//        assertEquals(expected, builder0.toString());
//    }
//
//    @Test
//    void addAllWorksCorrectly() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        builderThis.add(481, 3, 50);
//        builderThis.add(480, 5, 30);
//
//        builderThat.add(483, 2, 60);
//        builderThat.add(484, 1, 40);
//        builderThat.addAll(builderThis);
//
//        String expected = "Arrival : 480\n" +
//                "Changes : 5\n" + "---------------------------------------\n" +
//                "Arrival : 481\n" +
//                "Changes : 3\n" + "---------------------------------------\n" +
//                "Arrival : 483\n" +
//                "Changes : 2\n" + "---------------------------------------\n" +
//                "Arrival : 484\n" +
//                "Changes : 1\n" + "---------------------------------------\n";
//        assertEquals(expected, builderThat.toString());
//    }
//
//    @Test
//    void addAllWorksCorrectly2() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        builderThis.add(481, 2, 50);
//        builderThis.add(480, 1, 30);
//
//        builderThat.add(483, 3, 60);
//        builderThat.add(484, 4, 40);
//        builderThat.addAll(builderThis);
//
//        // tous sont supprimés sauf le premier car il domine les autres
//        String expected = "Arrival : 480\n" +
//                "Changes : 1\n" + "---------------------------------------\n";
//        assertEquals(expected, builderThat.toString());
//    }
//
//    @Test
//    void addAllWorksCorrectlyWithNothing() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        builderThat.add(483, 3, 60);
//        builderThat.add(484, 2, 40);
//        builderThat.addAll(builderThis);
//
//        // tous sont supprimés sauf le premier car il domine les autres
//        String expected = "Arrival : 483\n" +
//                "Changes : 3\n" + "---------------------------------------\n" +
//                "Arrival : 484\n" +
//                "Changes : 2\n" + "---------------------------------------\n";
//        assertEquals(expected, builderThat.toString());
//    }
//
//    @Test
//    void fullyDominatesWorksCorrectly() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.paretoBuilding[0] = PackedCriteria.pack(0, 3, 0);
//        builder.paretoBuilding[1] = PackedCriteria.pack(0, 1, 0);
//
//        assertTrue(builder.fullyDominates(builder, 480));
//    }
//
//    @Test
//    void testFullyDominatesAllDominated() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        long tuple0 = PackedCriteria.pack(481, 2, 50);
//        tuple0 = PackedCriteria.withDepMins(tuple0, 400);
//        builderThis.add(tuple0);
//        long tuple1 = PackedCriteria.pack(480, 1, 30);
//        tuple1 = PackedCriteria.withDepMins(tuple1, 12);
//        builderThis.add(tuple1);
//
//        builderThat.add(483, 3, 60);
//        builderThat.add(484, 2, 40);
//
//        // Tous les tuples de 'that' sont dominés par ceux de 'this'
//        assertTrue(builderThis.fullyDominates(builderThat, 50));
//    }
//
//    @Test
//    void testFullyDominatesNoneDominated() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        long tuple1 = PackedCriteria.pack(484, 2, 50);
//        tuple1 = PackedCriteria.withDepMins(tuple1, 12);
//        builderThis.add(tuple1);
//
//        builderThat.add(482, 1, 40);
//        builderThat.add(480, 0, 30);
//
//        // Aucun tuple de 'that' n'est dominé par ceux de 'this'
//        assertFalse(builderThis.fullyDominates(builderThat, 50));
//    }
//
//    @Test
//    void testFullyDominatesPartiallyDominated() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        long tuple0 = PackedCriteria.pack(482, 2, 50);
//        tuple0 = PackedCriteria.withDepMins(tuple0, 50);
//        long tuple1 = PackedCriteria.pack(483, 1, 30);
//        tuple1 = PackedCriteria.withDepMins(tuple1, 39);
//        builderThis.add(tuple0);
//        builderThis.add(tuple1);
//
//        builderThat.add(485, 3, 60);
//        builderThat.add(480, 1, 40);
//
//        // le tuple avec 485 est dominé
//        assertFalse(builderThis.fullyDominates(builderThat, 50));
//    }
//
//    @Test
//    void testFullyDominatesThatIsEmpty() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        builderThis.add(481, 2, 50);
//
//        assertTrue(builderThis.fullyDominates(builderThat, 50));
//    }
//
//    @Test
//    void testFullyDominatesThisIsEmpty() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//
//        builderThat.add(484, 3, 60);
//
//        // 'this' est vide, donc aucun tuple ne peut dominer ceux de 'that'
//        assertFalse(builderThis.fullyDominates(builderThat, 50));
//    }
//
//    @Test
//    void testFullyDominatesBothIsEmpty() {
//        ParetoFront.Builder builderThis = new ParetoFront.Builder();
//        ParetoFront.Builder builderThat = new ParetoFront.Builder();
//        assertTrue(builderThis.fullyDominates(builderThat, 50));
//    }
//
//
//    @Test
//    void testBuildWithEmpty() {
//        ParetoFront.Builder builder0 = new ParetoFront.Builder();
//        String expected = "";
//        assertEquals(expected, builder0.build().toString());
//    }
//
//    @Test
//    void testBuildWorksCorrectly() {
//        ParetoFront.Builder builder0 = new ParetoFront.Builder();
//        builder0.add(483, 3, 0);
//        builder0.add(484, 2, 0);
//
//        String expected = "Arrival : 483\n" +
//                "Changes : 3\n" + "---------------------------------------\n" +
//                "Arrival : 484\n" +
//                "Changes : 2\n" + "---------------------------------------\n";
//
//        assertEquals(expected, builder0.build().toString());
//    }
//
//    @Test
//    void testBuildWorksCorrectly1() {
//        ParetoFront.Builder builder0 = new ParetoFront.Builder();
//        builder0.add(483, 3, 0);
//        builder0.add(482, 2, 0);
//
//        String expected = "Arrival : 482\n" +
//                "Changes : 2\n" + "---------------------------------------\n";
//
//        assertEquals(expected, builder0.build().toString());
//    }
//
//
//
//    //Test ParetoFront
//    @Test
//    void testEmptyParetoFront() {
//        ParetoFront emptyPareto = ParetoFront.EMPTY;
//        assertEquals(0, emptyPareto.size());
//    }
//
//    @Test
//    void testConstructorWithEmptyArray() {
//        ParetoFront emptyPareto = new ParetoFront(new long[0]);
//        assertEquals(0, emptyPareto.size());
//    }
//
//    @Test
//    void testConstructor() {
//        long[] packedCriteria = {0, 0};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//        assertEquals(2, paretoFront.size());
//    }
//
//    @Test
//    void testNonEmptyParetoFrontSize() {
//        long[] packedCriteria = {(tuple0), (tuple1)};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//        assertEquals(2, paretoFront.size());
//    }
//
//    @Test
//    void testGetCriteriaExists() {
//        long[] packedCriteria = {(tuple0), (tuple1)};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//        assertEquals(tuple0, paretoFront.get(480, 3));
//
//        long[] packedCriteria1 = {(tuple0), (tuple1), (tuple2), (tuple3), (tuple4), (tuple5)};
//        ParetoFront paretoFront1 = new ParetoFront(packedCriteria1);
//        assertEquals(tuple4, paretoFront1.get(483, 0));
//    }
//
//    @Test
//    void testGetWithBoundaryValuesForChanges() {
//        long packed = PackedCriteria.pack(280, (int) Math.pow(2, 7) - 1, 0);
//        long[] packedCriteria = {packed};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//
//        assertEquals(packed, paretoFront.get(280, (int) Math.pow(2, 7) - 1));
//    }
//
//    @Test
//    void testGetWithBoundaryValuesForArrMins() {
//        long packed = PackedCriteria.pack(2879, 2, 0);
//        long[] packedCriteria = {packed};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//
//        assertEquals(packed, paretoFront.get(2879, 2));
//    }
//
//    @Test
//    void testSizeAfterArrayModification() {
//        long[] packedCriteria = {PackedCriteria.pack(480, 3, 0)};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//
//        // Modifier le tableau d'origine
//        packedCriteria[0] = PackedCriteria.pack(500, 2, 1);
//        // La taille reste inchangée
//        assertEquals(1, paretoFront.size());
//    }
//
//    @Test
//    void testGetCriteriaNotFound() {
//        long[] packedCriteria = {(tuple0), (tuple1)};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//
//        assertThrows(NoSuchElementException.class, () -> {
//            paretoFront.get(20, 1);
//        });
//
//        long[] packedCriteria1 = {(tuple0), (tuple1), (tuple2), (tuple3), (tuple4), (tuple5)};
//        ParetoFront paretoFront1 = new ParetoFront(packedCriteria1);
//        assertThrows(NoSuchElementException.class, () -> {
//            paretoFront1.get(1000000000, 1);
//        });
//    }
//
//    // test de chatGPT
//    @Test
//    void testForEach() {
//        // Create a ParetoFront with sample packed criteria
//        long[] packedCriteria = {(tuple0), (tuple1)};
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//
//        // Use an AtomicLong to accumulate results
//        AtomicLong accumulator = new AtomicLong(0);
//        LongConsumer consumer = accumulator::addAndGet;
//
//        // Apply the consumer to each element in the Pareto front
//        paretoFront.forEach(consumer);
//
//        // Verify that all packed criteria were consumed
//        long expectedSum = tuple0 + tuple1;
//        assertEquals(expectedSum, accumulator.get());
//    }
//
//}
