//package ch.epfl.rechor.journey;
//
//
//import org.junit.jupiter.api.Test;
//
//import java.util.*;
//
//
//import static ch.epfl.rechor.journey.PackedCriteria.pack;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class ParetoFrontTest_Carla {
//
//    @Test
//    void testClear2() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(789L);
//        assertFalse(builder.isEmpty(), "Le Builder ne doit pas être vide après un ajout.");
//
//        builder.clear();
//        assertTrue(builder.isEmpty(), "clear() doit vider le Builder.");
//    }
//    @Test
//    void testGetWithDominance() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//
//        builder.add(pack(112, 100, 678));
//        builder.add(pack(114, 98, 678));
//        builder.add(pack(113, 99, 678));
//
//        ParetoFront front = builder.build();
//        assertEquals(pack(112, 100, 678), front.get(112, 100), "get(0) doit retourner le premier élément trié.");
//        assertEquals(pack(113, 99, 678), front.get(113, 99), "get(1) doit retourner le deuxième élément.");
//        assertThrows(NoSuchElementException.class, () ->{
//            front.get(78, 66);});
//    }
//
//    @Test
//    void testSizeWithSimpleFront() {
//        ParetoFront front = new ParetoFront.Builder()
//                .add(pack(112, 100, 678))
//                .add(pack(114, 98, 678))
//                .add(pack(113, 99, 678))
//                .build();
//        assertEquals(3, front.size());
//    }
//    @Test
//    void testSizeWithDominatedFront() {
//        ParetoFront front = new ParetoFront.Builder()
//                .add(pack(112, 100, 678))
//                .add(pack(114, 100, 678))
//                .add(pack(113, 99, 678))
//                .build();
//        assertEquals(2, front.size());
//        assertEquals(pack(112, 100, 678), front.get(112, 100));
//    }
//
//    @Test
//    void testSizeWithDepMinsFront() {
//        ParetoFront front = new ParetoFront.Builder()
//                .add(PackedCriteria.withDepMins(pack(112, 100, 678), 128))
//                .add(PackedCriteria.withDepMins(pack(114, 98, 678), 128))
//                .add(PackedCriteria.withDepMins(pack(113, 99, 678), 128))
//                .build();
//        assertEquals(3, front.size());
//        assertEquals(PackedCriteria.withDepMins(pack(112, 100, 678), 128), front.get(112, 100));
//    }
//
//    @Test
//    void testForEachOnEmptyFront() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        List<Long> collected = new ArrayList<>();
//
//        builder.build().forEach(collected::add);
//        assertTrue(collected.isEmpty(), "forEach() ne doit rien exécuter sur une frontière vide.");
//    }
//    @Test
//    void testForEachOnMultipleElements() {
//        ParetoFront front = new ParetoFront.Builder()
//                .add(PackedCriteria.withDepMins(pack(112, 100, 678), 128))
//                .add(PackedCriteria.withDepMins(pack(114, 98, 678), 128))
//                .add(PackedCriteria.withDepMins(pack(113, 99, 678), 128))
//                .build();
//
//
//        List<Long> collected = new ArrayList<>();
//        front.forEach(collected::add);
//
//        List<Long> expectedOrder = List.of(PackedCriteria.withDepMins(pack(112, 100, 678), 128), PackedCriteria.withDepMins(pack(113, 99, 678),128), PackedCriteria.withDepMins(pack(114, 98, 678),128));
//        assertEquals(expectedOrder, collected, "forEach() doit parcourir les éléments triés.");
//    }
//
//    private ParetoFront.Builder builder;
//
//
//    void setUp() {
//        builder = new ParetoFront.Builder();
//    }
//
//    @Test
//    void testAddNonDominatedTuple() {
//        setUp();
//        long tuple1 = pack(2001, 5, 678);
//        long tuple2 = pack(2000, 6, 678);
//        long tuple3 = pack(2003, 4, 678);
//        builder.add(tuple1);
//        builder.add(tuple2);
//        builder.add(tuple3);
//        assertEquals("[2000min/6ch, 2001min/5ch, 2003min/4ch]", builder.toString() );
//    }
//
//    @Test
//    void testAddWorksOn1Tuple() {
//        setUp();
//        long tuple1 = pack(2001, 5, 678);
//        builder.add(tuple1);
//        assertEquals("[2001min/5ch]", builder.toString() );
//    }
//
//    @Test
//    void testAddOrders() {
//        setUp();
//        long tuple1 = pack(2001, 5, 678);
//        long tuple2 = pack(2000, 6, 678);
//        long tuple3 = pack(2003, 4, 678);
//        long tuple4 = pack(2004, 7, 678);
//        builder.add(tuple1);
//        builder.add(tuple2);
//        builder.add(tuple3);
//        builder.add(tuple4);
//
//        assertEquals("[2000min/6ch, 2001min/5ch, 2003min/4ch]", builder.toString() );
//    }
//
//    @Test
//    void testClearBuilder() {
//        setUp();
//        builder.add(pack(2001, 5, 678));
//        builder.clear();
//        assertTrue(builder.isEmpty());
//    }
//
//    @Test
//    void testBuildCreatesParetoFront() {
//        setUp();
//        builder.add(pack(2001, 5, 678));
//        builder.add(pack(2000, 6, 678));
//        ParetoFront front = builder.build();
//        assertEquals(2, front.size());
//        assertEquals(pack(2001, 5, 678), front.get(2001, 5));
//    }
//
//    @Test
//    void testAddAllMergesBuilders() {
//        setUp();
//        ParetoFront.Builder otherBuilder = new ParetoFront.Builder();
//        builder.add(pack(2001, 5, 678));
//        otherBuilder.add(pack(2000, 6, 678));
//        builder.addAll(otherBuilder);
//        assertEquals("[2000min/6ch, 2001min/5ch]", builder.toString());
//    }
//
//    @Test
//    void testFullyDominates() {
//        setUp();
//        ParetoFront.Builder otherBuilder = new ParetoFront.Builder();
//        builder.add(pack(2001, 5, 678));
//        builder.add(pack(2000, 6, 678));
//        otherBuilder.add(pack(2003, 7, 678));
//        assertThrows(IllegalArgumentException.class, () ->{
//            builder.fullyDominates(otherBuilder, 678);});
////        assertFalse(builder.fullyDominates(otherBuilder, 678));
//    }
//
//    @Test
//    void printingEmptyPareto(){
//        assertEquals("[]", new ParetoFront.Builder().toString());
//    }
//
//    @Test
//    void printingOneElementPareto(){
//        assertEquals("[2879min/127ch]", new ParetoFront.Builder().add(2879, 127, Integer.MAX_VALUE).toString());
//    }
//
//    @Test
//    void printingTwoElementsPareto(){
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        builder1.add(500, 2, 3);
//        builder1.add(499,3,4);
//        assertEquals("[499min/3ch, 500min/2ch]", builder1.toString());
//    }
//
//    @Test
//    void printingLongBuilder (){
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        builder1.add(1000,100,485);
//        builder1.add(1001,99,485);
//        builder1.add(1002,98,485);
//        builder1.add(1003,97,485);
//        builder1.add(1004,96,485);
//        assertEquals("[1000min/100ch, 1001min/99ch, 1002min/98ch, 1003min/97ch, 1004min/96ch]", builder1.toString());
//    }
//
//    @Test
//    void addAlltest () {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        builder1.add(1000, 100, 485);
//        builder1.add(1001, 99, 485);
//        builder1.add(1002, 98, 485);
//        builder1.add(1003, 97, 485);
//        builder1.add(1004, 96, 485);
//        ParetoFront.Builder builder2 = new ParetoFront.Builder();
//        builder2.addAll(builder1);
//        assertEquals("[1000min/100ch, 1001min/99ch, 1002min/98ch, 1003min/97ch, 1004min/96ch]", builder2.toString());
//
//    }
//
//    @Test
//    void addAlltestVariation () {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        builder1.add(1000, 100, 485);
//        builder1.add(1001, 99, 485);
//        builder1.add(1002, 98, 485);
//        builder1.add(1003, 97, 485);
//        builder1.add(1004, 96, 485);
//        ParetoFront.Builder builder2 = new ParetoFront.Builder();
//        builder2.add(500,2,1080);
//        builder2.addAll(builder1);
//        assertEquals("[500min/2ch]", builder2.toString());
//
//    }
//
//    @Test
//    void addAlltestVariation2 () {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        builder1.add(1000, 100, 485);
//        builder1.add(1001, 99, 485);
//        builder1.add(1002, 98, 485);
//        builder1.add(1003, 97, 485);
//        builder1.add(1004, 96, 485);
//        ParetoFront.Builder builder2 = new ParetoFront.Builder();
//        builder2.add(500,2,1080);
//        builder2.add(499,3,1080);
//
//        builder1.addAll(builder2);
//        assertEquals("[499min/3ch, 500min/2ch]", builder1.toString());
//
//    }
//
//
//    @Test
//    void emptyParetoFrontHasSizeZero() {
//        assertEquals(0, ParetoFront.EMPTY.size());
//    }
//
//    @Test
//    void getOnEmptyParetoFrontThrows() {
//        assertThrows(NoSuchElementException.class, () ->
//                ParetoFront.EMPTY.get(500, 2));
//    }
//
//    @Test
//    void builderConstructorCreatesEmptyBuilder() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        assertTrue(builder.isEmpty());
//    }
//
//    @Test
//    void builderCopyConstructorCreatesCopy() {
//        ParetoFront.Builder original = new ParetoFront.Builder();
//        original.add(500, 2, 42);
//        ParetoFront.Builder copy = new ParetoFront.Builder(original);
//        assertEquals(original.toString(), copy.toString());
//    }
//
//    @Test
//    void addWithMaxValuesWorks() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(2879, 127, Integer.MAX_VALUE);
//        assertEquals("[2879min/127ch]", builder.toString());
//    }
//
//    @Test
//    void addMultipleDominatedTuplesKeepsNonDominated() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(500, 2, 1)  // sera gardé
//                .add(500, 3, 2)  // dominé par le premier
//                .add(501, 2, 3)  // dominé par le premier
//                .add(499, 3, 4); // sera gardé
//        assertEquals("[499min/3ch, 500min/2ch]", builder.toString());
//    }
//
//    @Test
//    void addIdenticalTuplesKeepsOne() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(500, 2, 1)
//                .add(500, 2, 2);
//        assertEquals("[500min/2ch]", builder.toString());
//    }
//
//    @Test
//    void addCausesArrayResizing() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        for (int i = 0; i < 10; i++) {
//            builder.add(500 - i, 2 + i, i);
//        }
//        // Vérifie que tous les éléments sont présents
//        assertEquals(10, builder.build().size());
//    }
//
//    @Test
//    void forEachWorksOnEmpty() {
//        ArrayList<Long> collected = new ArrayList<>();
//        ParetoFront.EMPTY.forEach(collected::add);
//        assertTrue(collected.isEmpty());
//    }
//
//    @Test
//    void forEachCollectsAllElements() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(500, 2, 1)
//                .add(501, 1, 2);
//        ArrayList<Long> collected = new ArrayList<>();
//        builder.build().forEach(collected::add);
//        assertEquals(2, collected.size());
//    }
//
//    @Test
//    void fullyDominatesWithEmptyBuilders() {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        ParetoFront.Builder builder2 = new ParetoFront.Builder();
//        assertTrue(builder1.fullyDominates(builder2, 678));
//    }
//
//    @Test
//    void addAllWithEmptySourceDoesNothing() {
//        ParetoFront.Builder target = new ParetoFront.Builder();
//        target.add(500, 2, 1);
//        ParetoFront.Builder source = new ParetoFront.Builder();
//        target.addAll(source);
//        assertEquals("[500min/2ch]", target.toString());
//    }
//    @Test
//    void clearEmptiesBuilder() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(500, 2, 1)
//                .add(501, 1, 2)
//                .clear();
//        assertTrue(builder.isEmpty());
//    }
//
//    @Test
//    void buildCreatesCorrectParetoFront() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        builder.add(500, 2, 1)
//                .add(501, 1, 2);
//        ParetoFront front = builder.build();
//        assertEquals(2, front.size());
//        assertEquals(builder.toString(), front.toString());
//    }
//    @Test
//    void testAddToEmptyFrontier() {
//        ParetoFront.Builder builder = new ParetoFront.Builder();
//        long tuple = pack(10, 2, 100);
//        builder.add(tuple);
//        assertEquals("[10min/2ch]", builder.toString());
//    }
//
//    @Test
//    void testAddDominatedTuple() {
//        ParetoFront.Builder frontier = new ParetoFront.Builder();
//        long tuple1 = pack(10, 2, 100);
//        long tuple2 = pack(12, 3, 90); // Dominé par tuple1
//        frontier.add(tuple1);
//        frontier.add(tuple2);
//        assertEquals("[10min/2ch]", frontier.toString()); // tuple2 doit être ignoré
//    }
//
//    @Test
//    void testAddNonDominatedTuple2() {
//        ParetoFront.Builder frontier = new ParetoFront.Builder();
//
//        long tuple1 = pack(12, 1, 100);
//        long tuple2 = pack(10, 2, 90); // Pas dominé
//
//        frontier.add(tuple1);
//        frontier.add(tuple2);
//
//        assertEquals("[10min/2ch, 12min/1ch]", frontier.toString());
//    }
//
//    @Test
//    void testAddTupleThatDominatesOthers() {
//        ParetoFront.Builder frontier = new ParetoFront.Builder();
//        long tuple1 = pack(15, 4, 50);
//        long tuple2 = pack(20, 3, 30);
//        frontier.add(tuple1);
//        frontier.add(tuple2);
//        assertEquals("[15min/4ch, 20min/3ch]", frontier.toString());
//        long tuple3 = pack(14, 2, 80); // Doit dominer tuple1 et tuple2
//        frontier.add(tuple3);
//        assertEquals("[14min/2ch]", frontier.toString()); // tuple1 et tuple2 supprimés
//    }
//
//    @Test
//    void testLexicographicOrder() {
//        ParetoFront.Builder frontier = new ParetoFront.Builder();
//        long tuple1 = pack(14, 1, 100);
//        long tuple2 = pack(10, 3, 200);
//        long tuple3 = pack(12, 2, 50);
//
//        frontier.add(tuple1);
//        frontier.add(tuple2);
//        frontier.add(tuple3);
//
//        assertEquals("[10min/3ch, 12min/2ch, 14min/1ch]", frontier.toString()); // Doit être trié
//    }
//    @Test
//    void testFullyDominatesWithEmptyThat() {
//        ParetoFront.Builder receiver = new ParetoFront.Builder();
//        ParetoFront.Builder that = new ParetoFront.Builder();
//        assertTrue(receiver.fullyDominates(that, 0)); // Rien à dominer → vrai
//    }
//
//    @Test
//    void testFullyDominatesWhenAllTuplesAreDominated() {
//        ParetoFront.Builder receiver = new ParetoFront.Builder();
//        ParetoFront.Builder that = new ParetoFront.Builder();
//
//        long dominantTuple = pack(10, 1, 50);
//        long dominatedTuple = pack(12, 2, 60);
//
//        receiver.add(PackedCriteria.withDepMins(dominantTuple,5));
//        that.add(dominatedTuple);
//
//        assertTrue(receiver.fullyDominates(that, 5)); // Tous les tuples de `that` sont dominés
//    }
//
//    @Test
//    void testFullyDominatesWhenAtLeastOneTupleIsNotDominated() {
//        ParetoFront.Builder receiver = new ParetoFront.Builder();
//        ParetoFront.Builder that = new ParetoFront.Builder();
//
//        long dominantTuple = pack(10, 1, 50);
//        long nonDominatedTuple = pack(8, 0, 40); // Ce tuple n'est pas dominé
//
//        receiver.add(PackedCriteria.withDepMins(dominantTuple,5));
//        that.add(nonDominatedTuple);
//
//        assertFalse(receiver.fullyDominates(that, 5)); // Au moins un tuple n'est pas dominé
//    }
//
//    @Test
//    void testFullyDominatesWithDepMinsAffectingDomination() {
//        ParetoFront.Builder receiver = new ParetoFront.Builder();
//        ParetoFront.Builder that = new ParetoFront.Builder();
//
//        long dominantTuple = pack(10, 1, 50);
//        long affectedTuple = pack(10, 1, 60); // Même valeurs sauf charge utile
//
//
//        receiver.add(PackedCriteria.withDepMins(dominantTuple, 10));
//        assertEquals("[10→10min/1ch]", receiver.toString());
//        that.add(affectedTuple);
//
//        assertTrue(receiver.fullyDominates(that, 10)); // Charge utile ne change pas la domination
//    }
//
//    @Test
//    void testFullyDominatesWithMultipleDominatedTuples() {
//        ParetoFront.Builder receiver = new ParetoFront.Builder();
//        ParetoFront.Builder that = new ParetoFront.Builder();
//
//        long tuple1 = pack(10, 1, 50);
//        long tuple2 = pack(12, 2, 60);
//        long tuple3 = pack(14, 3, 70);
//
//        receiver.add(PackedCriteria.withDepMins(tuple1, 6));
//        receiver.add(PackedCriteria.withDepMins(tuple2, 7));
//        receiver.add(PackedCriteria.withDepMins(tuple3, 9));
//
//        long dominated1 = pack(12, 2, 80);
//        long dominated2 = pack(15, 4, 90);
//
//        that.add(pack(12, 2, 80));
//        that.add(pack(15, 4, 90));
//
//        assertTrue(receiver.fullyDominates(that, 5)); // Tous les tuples de `that` sont dominés
//    }
//
//    @Test
//    void testConstructorCopyworks (){
//        ParetoFront.Builder builder1 = new ParetoFront.Builder()
//                .add(pack(112, 100, 678))
//                .add(pack(114, 98, 678))
//                .add(pack(113, 99, 678));
//        ParetoFront.Builder builder2 = new ParetoFront.Builder();
//
//        builder2.add(PackedCriteria.withDepMins(pack(100, 10, 678), 25));
//        assertTrue(builder2.fullyDominates(builder1, 10));
//    }
//
//    //// ATTENTIONNNNNNNNNN
//}