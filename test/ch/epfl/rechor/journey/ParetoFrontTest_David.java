package ch.epfl.rechor.journey;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

import static ch.epfl.rechor.journey.PackedCriteria.*;
import static org.junit.jupiter.api.Assertions.*;

class ParetoFrontTest_David {
    private final static int ITERATIONS = 1;
    private static final Random random = new Random();

    private static long randomDouble(){
        return pack(random.nextInt(-240,2880),random.nextInt(0,128),random.nextInt());
    }

    private static long randomTriple(){
        return withDepMins(randomDouble(),random.nextInt(-240,2880));
    }

    private static long[] getAllCriteria(ParetoFront paretoFront){
        ArrayList<Long> temp = new ArrayList<>();
        paretoFront.forEach(temp::add);
        long[] allCriteria = new long[paretoFront.size()];
        for (int i=0; i< paretoFront.size();++i){
            allCriteria[i]= temp.get(i);
        }
        return allCriteria;
    }

    @Test
    void sizeWorksOnEmptyParetoFront(){
        assertEquals(0,ParetoFront.EMPTY.size());
    }

    @Test
    void sizeWorksOnTrivialParetoFront(){
        ParetoFront test = new ParetoFront.Builder()
                .add(240,1,0)
                .build();
        assertEquals(1,test.size());
    }

    @Test
    void sizeWorksOnNonTrivialParetoFront(){
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(241,2,0)
                .add(242,1,0)
                .add(243,0,0)
                .build();
        assertEquals(4,test.size());
    }

    @Test
    void getWorksOnTrivialParetoFrontWhenContainingTheCriteria() {
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .build();
        assertEquals(pack(240,3,0),test.get(240,3));
    }

    @Test
    void getWorksOnTrivialParetoFrontWhenNotContainingTheCriteria() {
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .build();
        assertThrows(NoSuchElementException.class, ()-> test.get(240,4));
        assertThrows(NoSuchElementException.class, ()-> test.get(241,3));
    }

    @Test
    void getWorksOnNonTrivialParetoFrontWhenContainingTheCriteria() {
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(241,2,0)
                .add(242,1,0)
                .add(243,0,0)
                .build();
        assertEquals(pack(240,3,0),test.get(240,3));
        assertEquals(pack(241,2,0),test.get(241,2));
        assertEquals(pack(242,1,0),test.get(242,1));
        assertEquals(pack(243,0,0),test.get(243,0));
    }

    @Test
    void getWorksOnNonTrivialParetoFrontWhenNotContainingTheCriteria() {
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(241,2,0)
                .add(242,1,0)
                .add(243,0,0)
                .build();
        assertThrows(NoSuchElementException.class, ()-> test.get(244,4));
        assertThrows(NoSuchElementException.class, ()-> test.get(243,3));
        assertThrows(NoSuchElementException.class, ()-> test.get(241,1));
        assertThrows(NoSuchElementException.class, ()-> test.get(242,2));
        assertThrows(NoSuchElementException.class, ()-> test.get(240,0));
    }

    @Test
    void getWorksOnEmptyParetoFront() {
        assertThrows(NoSuchElementException.class, ()-> ParetoFront.EMPTY.get(240,1));
    }

    @Test
    void isEmptyWorksOnEmptyBuilder() {
        assertTrue(new ParetoFront.Builder().isEmpty());
    }

    @Test
    void isEmptyWorksOnClearedBuilder() {
        ParetoFront.Builder test = new ParetoFront.Builder()
                .add(randomDouble());
        test.clear();
        assertTrue(test.isEmpty());
    }

    @Test
    void isEmptyWorksOnNonEmptyBuilder() {
        ParetoFront.Builder test = new ParetoFront.Builder()
                .add(240,3,0);
        assertFalse(test.isEmpty());
    }

    @Test
    void builderWorksOnTrivialPairCriteria() {
        long[] expectedArray = new long[]{
                pack(240,3,0),
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void builderWorksOnTrivialTripleCriteria() {
        long[] expectedArray = new long[]{
                withDepMins(pack(240,3,0),120),
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(withDepMins(pack(240,3,0),120))
                .build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void builderWorksOnAscendingOrderNonTrivialPairCriteria() {
        long[] expectedArray = new long[]{
                pack(240,3,0),
                pack(241,2,0),
                pack(242,1,0),
                pack(243,0,0)
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(240,4,0)
                .add(244,1,0)
                .add(241,2,0)
                .add(242,1,0)
                //.add(244,1,0)
                .add(243,0,0)
                .build();
        long[] actualArray = getAllCriteria(test);
        System.out.println(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void builderWorksOnAscendingOrderNonTrivialTripleCriteria() {
        long[] expectedArray = new long[]{
                withDepMins(pack(240,3,0),121),
                withDepMins(pack(239,3,0),120),
                withDepMins(pack(240,2,0),120)
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(withDepMins(pack(240,3,0),121))
                .add(withDepMins(pack(239,3,0),120))
                .add(withDepMins(pack(240,2,0),120))
                .add(withDepMins(pack(241,3,0),120))
                .add(withDepMins(pack(240,4,0),120))
                .add(withDepMins(pack(240,3,0),119))
                .build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void builderWorksOnDescendingOrderNonTrivialPairCriteria() {
        long[] expectedArray = new long[]{
                pack(240,3,0),
                pack(241,2,0),
                pack(242,1,0),
                pack(243,0,0)
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(243,0,0)
                .add(242,1,0)
                .add(244,1,0)
                .add(241,2,0)
                .add(240,4,0)
                .add(240,3,0)
                .build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void builderWorksOnDescendingOrderNonTrivialTripleCriteria() {
        long[] expectedArray = new long[]{
                withDepMins(pack(240,3,0),121),
                withDepMins(pack(239,3,0),120),
                withDepMins(pack(240,2,0),120)
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(withDepMins(pack(240,2,0),120))
                .add(withDepMins(pack(239,3,0),120))
                .add(withDepMins(pack(240,3,0),121))
                .add(withDepMins(pack(241,3,0),120))
                .add(withDepMins(pack(240,4,0),120))
                .add(withDepMins(pack(240,3,0),119))
                .build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    /**@RepeatedTest(ITERATIONS)
    void builderWorksOnRepeatedPairCriteria1() {
    long a = randomDouble();
    long[] expectedArray = new long[]{
    a
    };
    ParetoFront.Builder test = new ParetoFront.Builder().add(a);
    for (int i=Integer.MIN_VALUE;i<Integer.MAX_VALUE;++i){
    test.add(withPayload(a,i));
    }
    long[] actualArray = getAllCriteria(test.build());
    assertArrayEquals(expectedArray,actualArray);
    }

     @RepeatedTest(ITERATIONS)
     void builderWorksOnRepeatedTripleCriteria1() {
     long a = randomTriple();
     long[] expectedArray = new long[]{
     a
     };
     ParetoFront.Builder test = new ParetoFront.Builder().add(a);
     for (int i=Integer.MIN_VALUE;i<Integer.MAX_VALUE;++i){
     test.add(withPayload(a,i));
     }
     long[] actualArray = getAllCriteria(test.build());
     assertArrayEquals(expectedArray,actualArray);
     }

     @RepeatedTest(ITERATIONS)
     void builderWorksOnRepeatedPairCriteria2() {
     long a,b;
     do {
     a = randomDouble();
     b = randomDouble();
     } while (a>=b || dominatesOrIsEqual(a,b));
     long[] expectedArray = new long[]{
     a,b
     };
     ParetoFront.Builder test = new ParetoFront.Builder().add(a).add(b);
     for (int i=Integer.MIN_VALUE;i<Integer.MAX_VALUE;++i){
     test.add(withPayload(a,i));
     test.add(withPayload(b,i));
     }
     long[] actualArray = getAllCriteria(test.build());
     assertArrayEquals(expectedArray,actualArray);
     }

     @RepeatedTest(ITERATIONS)
     void builderWorksOnRepeatedTripleCriteria2() {
     long a,b,c;
     do {
     a = randomTriple();
     b = randomTriple();
     c = randomTriple();
     } while (a>=b || dominatesOrIsEqual(a,b) || b>=c || dominatesOrIsEqual(b,c));
     long[] expectedArray = new long[]{
     a,b,c
     };
     ParetoFront.Builder test = new ParetoFront.Builder().add(a).add(b).add(c);
     for (int i=Integer.MIN_VALUE;i<Integer.MAX_VALUE;++i){
     test.add(withPayload(a,i));
     test.add(withPayload(b,i));
     test.add(withPayload(c,i));
     }
     long[] actualArray = getAllCriteria(test.build());
     assertArrayEquals(expectedArray,actualArray);
     }*/

    @Test
    void builderWorksWithZeroCriteria() {
        long[] expectedArray = new long[0];
        ParetoFront test = new ParetoFront.Builder().build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void builderTrimsZeroElements1() {
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .build();
        assertEquals(1,test.size());
    }

    @Test
    void builderTrimsZeroElements2() {
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(241,2,0)
                .build();
        assertEquals(2,test.size());
    }

    @Test
    void builderTrimsZeroElements3() {
        ParetoFront test = new ParetoFront.Builder()
                .add(239,4,0)
                .add(240,3,0)
                .add(241,2,0)
                .add(242,1,0)
                .add(243,0,0)
                .build();
        assertEquals(5,test.size());
    }

    @Test
    void builderTrimsZeroElements4() {
        ParetoFront test = new ParetoFront.Builder()
                .add(237,6,0)
                .add(238,5,0)
                .add(239,4,0)
                .add(240,3,0)
                .add(241,2,0)
                .add(242,1,0)
                .add(243,0,0)
                .build();
        assertEquals(7,test.size());
    }

    @RepeatedTest(ITERATIONS)
    void fullyDominatesThrowsWhenPairs() {
        ParetoFront.Builder test = new ParetoFront.Builder()
                .add(randomDouble());
        assertThrows(IllegalArgumentException.class,()-> test.fullyDominates(test, random.nextInt(-240,2880)));

    }

    @RepeatedTest(ITERATIONS)
    void fullyDominatesWorksForEmptyBuilders() {
        ParetoFront.Builder test = new ParetoFront.Builder()
                .add(withDepMins(randomDouble(),random.nextInt(-240,2880)));
        assertTrue(new ParetoFront.Builder().fullyDominates(new ParetoFront.Builder(),random.nextInt(-240,2880)));
        assertTrue(test.fullyDominates(new ParetoFront.Builder(),random.nextInt(-240,2880)));
        assertFalse(new ParetoFront.Builder().fullyDominates(test,random.nextInt(-240,2880)));
    }

    @RepeatedTest(ITERATIONS)
    void fullyDominatesWorksForSameBuilder() {
        int depMins = random.nextInt(-240,2880);
        ParetoFront.Builder test = new ParetoFront.Builder()
                .add(withDepMins(randomDouble(),depMins))
                .add(withDepMins(randomDouble(),depMins))
                .add(withDepMins(randomDouble(),depMins));
        assertTrue(test.fullyDominates(test, depMins));
    }

    @Test
    void fullyDominatesWorksWhenTrue() {
        ParetoFront.Builder test1 = new ParetoFront.Builder()
                .add(withDepMins(pack(240,0,0),120));
        ParetoFront.Builder test2 = new ParetoFront.Builder()
                .add(241,1,0)
                .add(240,2,0);
        assertTrue(test1.fullyDominates(test2,120));
    }

    @Test
    void fullyDominatesWorksWhenFalse() {
        ParetoFront.Builder test1 = new ParetoFront.Builder()
                .add(withDepMins(pack(240,3,0),120))
                .add(withDepMins(pack(241,1,0),120));
        ParetoFront.Builder test2 = new ParetoFront.Builder()
                .add(240,0,0);
        assertFalse(test1.fullyDominates(test2,120));
    }

    @Test
    void addAllWorksOnEmptyBuilder() {
        long[] expectedArray = new long[]{
                pack(240,3,0),
                pack(241,2,0)
        };
        ParetoFront test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(241,2,0)
                .addAll(new ParetoFront.Builder())
                .build();
        long[] actualArray = getAllCriteria(test);
        assertArrayEquals(expectedArray,actualArray);
    }

    @Test
    void addAllWorksFromEmptyBuilder() {
        long[] expectedArray = new long[]{
                pack(240,3,0),
                pack(241,2,0)
        };
        ParetoFront.Builder test = new ParetoFront.Builder()
                .add(240,3,0)
                .add(241,2,0);
        long[] actualArray = getAllCriteria(new ParetoFront.Builder().addAll(test).build());
        assertArrayEquals(expectedArray,actualArray);
    }

    @RepeatedTest(ITERATIONS)
    void addAllWorksOnSameBuilderWithDouble() {
        ParetoFront.Builder test = new ParetoFront.Builder();
        for (int i =0; i<100;++i){
            test.add(randomDouble());
        }
        assertArrayEquals(getAllCriteria(test.build()),getAllCriteria(test.addAll(test).build()));
    }

    @RepeatedTest(ITERATIONS)
    void addAllWorksOnSameBuilderWithTriple() {
        ParetoFront.Builder test = new ParetoFront.Builder();
        for (int i =0; i<100;++i){
            test.add(randomTriple());
        }
        assertArrayEquals(getAllCriteria(test.build()),getAllCriteria(test.addAll(test).build()));
    }

    @Test
    void addAllWorksOnNonTrivialBuilders() {
        long[] expectedArray = new long[]{
                pack(240, 3, 0),
                pack(241, 2, 0),
                pack(242, 1, 0),
                pack(243, 0, 0)
        };
        ParetoFront.Builder test1 = new ParetoFront.Builder()
                .add(240,3,0)
                .add(242,1,0);
        ParetoFront.Builder test2 = new ParetoFront.Builder()
                .add(241,2,0)
                .add(243,0,0);
        assertArrayEquals(expectedArray,getAllCriteria(test1.addAll(test2).build()));
        assertArrayEquals(expectedArray,getAllCriteria(test2.addAll(test1).build()));
    }

}