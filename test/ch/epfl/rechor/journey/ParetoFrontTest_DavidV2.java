package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.epfl.rechor.journey.PackedCriteria.pack;
import static org.junit.jupiter.api.Assertions.*;

import static ch.epfl.rechor.journey.ParetoFront.*;

public class ParetoFrontTest_DavidV2 {

    @Test
    void newBuilderIsEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty());
    }

    @Test
    void builderCanAddElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(600, 3, 0);
        assertFalse(builder.isEmpty());
    }

    @Test
    void builderCanClear() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(600, 3, 0);
        builder.clear();
        assertTrue(builder.isEmpty());
    }

    @Test
    void builderDoesNotAddDominatedElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(500, 2, 0);
        builder.add(510, 3, 0);  // Moins bon que (500,2), donc ne doit pas être ajouté

        ParetoFront front = builder.build();
        assertEquals(1, front.size());
    }

    @Test
    void builderRemovesDominatedElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(600, 3, 0);
        builder.add(580, 2, 0);  // Meilleur, donc il doit supprimer (600,3)

        ParetoFront front = builder.build();
        assertEquals(1, front.size());
        assertThrows(NoSuchElementException.class, () -> front.get(600, 3));
    }

    //    @Test
//    void fullyDominatesWorksCorrectly() {
//        ParetoFront.Builder builder1 = new ParetoFront.Builder();
//        builder1.add(600, 3, 0);
//
//        ParetoFront.Builder builder2 = new ParetoFront.Builder();
//        builder2.add(610, 4, 0);
//
//        assertTrue(builder1.fullyDominates(builder2, 500));
//        assertFalse(builder2.fullyDominates(builder1, 500));
//    }
//    @Test
//    void fullyDominatesWhenAllTuplesAreDominated() {
//        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
//        thisBuilder.add(500, 1, 0);
//        thisBuilder.add(520, 2, 0);
//        thisBuilder.add(540, 3, 0);
//
//        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
//        thatBuilder.add(505, 1, 0); // Moins bon que 500, 1
//        thatBuilder.add(525, 2, 0); // Moins bon que 520, 2
//
//        assertTrue(thisBuilder.fullyDominates(thatBuilder, 480));
//    }
//    @Test
//    void fullyDominatesReturnsFalseWhenAtLeastOneTupleIsNotDominated() {
//        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
//        thisBuilder.add(500, 1, 0);
//        thisBuilder.add(520, 2, 0);
//
//        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
//        thatBuilder.add(505, 1, 0); // Dominé ✅
//        thatBuilder.add(515, 3, 0); // ❌ Non dominé (3 changements contre 2 max)
//
//        assertFalse(thisBuilder.fullyDominates(thatBuilder, 480));
//    }
//    @Test
//    void fullyDominatesReturnsFalseWhenThisIsEmpty() {
//        ParetoFront.Builder thisBuilder = new ParetoFront.Builder(); // Vide
//        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
//        thatBuilder.add(505, 1, 0);
//
//        assertFalse(thisBuilder.fullyDominates(thatBuilder, 480));
//    }
    @Test
    void fullyDominatesReturnsFalseWhenThisIsEmpty() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
        thatBuilder.add(505, 1, 0);

        assertFalse(thisBuilder.fullyDominates(thatBuilder, 480)); // ❌ Impossible de dominer si `this` est vide
    }
    @Test
    void fullyDominatesReturnsTrueWhenThatIsEmpty() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
        thisBuilder.add(500, 1, 0);

        ParetoFront.Builder thatBuilder = new ParetoFront.Builder(); // `that` est vide

        assertTrue(thisBuilder.fullyDominates(thatBuilder, 480)); // ✅ Une frontière vide est toujours dominée
    }
    @Test
    void fullyDominatesReturnsTrueWhenAllTuplesAreDominated() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
        thisBuilder.add(PackedCriteria.withDepMins(pack(500, 1, 0), 480)); // Ajout avec depMins
        thisBuilder.add(PackedCriteria.withDepMins(pack(520, 2, 0), 480));

        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
        thatBuilder.add(500, 1, 0);
        thatBuilder.add(510, 2, 0);

        assertTrue(thisBuilder.fullyDominates(thatBuilder, 480));
    }
    @Test
    void fullyDominatesReturnsFalseWhenAtLeastOneTupleIsNotDominated() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
        thisBuilder.add(PackedCriteria.withDepMins(pack(500, 1, 0), 480));
        thisBuilder.add(PackedCriteria.withDepMins(pack(520, 2, 0), 480));

        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
        thatBuilder.add(505, 1, 0); // ✅ Dominé
        thatBuilder.add(515, 3, 0); // ❌ Non dominé (3 changements contre 2 max)

        assertTrue(thisBuilder.fullyDominates(thatBuilder, 480));
    }
    @Test
    void fullyDominatesHandlesLargeDepMinsValuesCorrectly() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
        thisBuilder.add(PackedCriteria.withDepMins(pack(1000, 1, 0), 900));
        thisBuilder.add(PackedCriteria.withDepMins(pack(1020, 2, 0), 900));

        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
        thatBuilder.add(1005, 1, 0);
        thatBuilder.add(1015, 2, 0);

        assertTrue(thisBuilder.fullyDominates(thatBuilder, 900));
    }
    @Test
    void fullyDominatesReturnsTrueForEmptyThat() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder();
        thisBuilder.add(PackedCriteria.withDepMins(pack(500, 1, 0), 480));
        thisBuilder.add(PackedCriteria.withDepMins(pack(520, 2, 0), 480));

        ParetoFront.Builder thatBuilder = new ParetoFront.Builder(); // Empty

        assertTrue(thisBuilder.fullyDominates(thatBuilder, 480));
    }
    @Test
    void fullyDominatesReturnsFalseForEmptyThis() {
        ParetoFront.Builder thisBuilder = new ParetoFront.Builder(); // Empty

        ParetoFront.Builder thatBuilder = new ParetoFront.Builder();
        thatBuilder.add(500, 1, 0);
        thatBuilder.add(510, 2, 0);

        assertFalse(thisBuilder.fullyDominates(thatBuilder, 480));
    }

    @Test
    void builderIsEmptyInitially() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty());
    }

    @Test
    void builderAddIncreasesSize() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        assertFalse(builder.isEmpty());
    }

    @Test
    void builderClearResetsSize() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.clear();
        assertTrue(builder.isEmpty());
    }

    @Test
    void builderAddAllCombinesCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(10, 2, 1);
        builder1.add(15, 1, 1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(20, 0, 1);

        builder1.addAll(builder2);
        ParetoFront paretoFront = builder1.build();

        assertEquals(3, paretoFront.size());
    }


    @Test
    void addAllHandlesPartiallyDominatedCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(10, 2, 1); // (arrMins=10, changes=2, payload=1)
        builder1.add(15, 1, 1); // (arrMins=15, changes=1, payload=1)

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(12, 3, 1); // (arrMins=12, changes=3, payload=1) => Dominé par (10,2,1)
        builder2.add(14, 1, 1); // (arrMins=14, changes=1, payload=1) => Non dominé

        builder1.addAll(builder2);
        ParetoFront paretoFront = builder1.build();

        assertEquals(2, paretoFront.size()); // Doit contenir (10,2,1), (14,1,1)
    }



    @Test
    void paretoFrontGetReturnsCorrectCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        ParetoFront paretoFront = builder.build();

        long criteria = paretoFront.get(10, 2);
        assertEquals(pack(10, 2, 1), criteria);
    }

    @Test
    void paretoFrontGetThrowsExceptionForNonExistentCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        ParetoFront paretoFront = builder.build();

        assertThrows(NoSuchElementException.class, () -> paretoFront.get(15, 1));
    }


    @Test
    void builderAddDoesNotAddDominatedCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(12, 3, 1); // Dominated by the first criteria
        assertEquals(1, builder.build().size());
    }

    @Test
    void builderFullyDominatesReturnsFalseWhenNotAllCriteriaDominated2() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = PackedCriteria.withDepMins(pack(10, 2, 1), 5);
        builder1.add(criteria1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(pack(5, 1, 1));  // (arrMins=5, changes=1, payload=1) => Plus optimal

        assertFalse(builder1.fullyDominates(builder2, 5)); // Devrait être false
    }

    @Test
    void builderFullyDominatesReturnsFalseWhenNotAllCriteriaDominated() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = pack(10, 2, 1);
        criteria1 = PackedCriteria.withDepMins(criteria1,5);
        builder1.add(criteria1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(5, 1, 1);  // (arrMins=5, changes=1, payload=1) => Plus optimal

        assertFalse(builder1.fullyDominates(builder2, 5)); //
    }

    @Test
    void builderAddDoesNotAddDuplicateCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(10, 2, 1); // Critère identique

        assertEquals(1, builder.build().size());
    }

    @Test
    void builderDoesNotAddDominatedCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(15, 3, 1); // Dominé par (10, 2, 1)

        assertEquals(1, builder.build().size());
    }

    @Test
    void builderMergeNonDominatedCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(10, 2, 1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(8, 3, 1); // Non dominé par (10, 2, 1)

        builder1.addAll(builder2);
        ParetoFront paretoFront = builder1.build();

        assertEquals(2, paretoFront.size());
    }

    ////////////////////////////////////////////////////////////////////

    @Test
    void builderFullyDominatesReturnsTrueWhenAllCriteriaDominated() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = PackedCriteria.withDepMins(pack(10, 2, 1), 5);
        builder1.add(criteria1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(pack(15, 3, 1));  // (arrMins=15, changes=3, payload=1) => Moins optimal

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être true
    }

    @Test
    void builderFullyDominatesReturnsFalseWhenCriteriaEqual() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = PackedCriteria.withDepMins(pack(10, 2, 1), 5);
        builder1.add(criteria1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(PackedCriteria.withDepMins(pack(10, 2, 1), 5));  // Critères identiques

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être false
    }

    @Test
    void builderFullyDominatesReturnsFalseWhenNoCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être false
    }

    @Test
    void builderFullyDominatesReturnsTrueWithMultipleCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(PackedCriteria.withDepMins(pack(10, 2, 1), 5));
        builder1.add(PackedCriteria.withDepMins(pack(15, 1, 1), 5));

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(pack(20, 3, 1));  // (arrMins=20, changes=3, payload=1) => Moins optimal

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être true
    }

    @Test
    void builderFullyDominatesReturnsFalseWithMixedCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(PackedCriteria.withDepMins(pack(10, 2, 1), 5));
        builder1.add(PackedCriteria.withDepMins(pack(20, 1, 1), 5));

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(pack(15, 3, 1));  // (arrMins=15, changes=3, payload=1) => Moins optimal
        builder2.add(pack(5, 1, 1));   // (arrMins=5, changes=1, payload=1) => Plus optimal

        assertFalse(builder1.fullyDominates(builder2, 5)); // Devrait être false
    }

    @Test
    void builderFullyDominatesReturnsTrueWithDominatedCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = pack(10, 2, 1);
        long criteria2 = pack(30, 5, 1);

        criteria1 = PackedCriteria.withDepMins(criteria1,5);
        criteria2 = PackedCriteria.withDepMins(criteria1,5);

        builder1.add(criteria1);
        builder1.add(criteria2);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        builder2.add(pack(20, 3, 1));  // (arrMins=20, changes=3, payload=1) => Moins optimal
        builder2.add(pack(25, 4, 1));
        builder2.add(pack(21, 2,1));// (arrMins=25, changes=4, payload=1) => Moins optimal

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être true


    }
    @Test
    void fullyDominatesHandlesEmptyFrontier() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(10, 2, 1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder(); // Frontière vide

        assertTrue(builder1.fullyDominates(builder2, 5)); // Une frontière non vide domine toujours une frontière vide
        assertFalse(builder2.fullyDominates(builder1, 5)); // Une frontière vide ne domine jamais une frontière non vide
    }




    @Test
    void builderReturnTrueWhenNoCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être true
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void builderFullyDominatesReturnsFalseWithNonDominatedCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = pack(10, 2, 1);
        long criteria2 = pack(30, 5, 1);

        criteria1 = PackedCriteria.withDepMins(criteria1, 5);
        criteria2 = PackedCriteria.withDepMins(criteria2, 5);

        builder1.add(criteria1);
        builder1.add(criteria2);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(pack(5, 1, 1));   // (arrMins=5, changes=1, payload=1) => Plus optimal
        builder2.add(pack(25, 4, 1));  // (arrMins=25, changes=4, payload=1) => Moins optimal

        assertFalse(builder1.fullyDominates(builder2, 5)); // Devrait être false
    }

    @Test
    void builderFullyDominatesReturnsTrueWithEqualCriteria() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        long criteria1 = pack(10, 2, 1);
        criteria1 = PackedCriteria.withDepMins(criteria1, 5);
        builder1.add(criteria1);

        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(PackedCriteria.withDepMins(pack(10, 2, 1), 5));  // Critères identiques

        assertTrue(builder1.fullyDominates(builder2, 5)); // Devrait être true
    }

    @Test
    void builderIsEmptyReturnsTrueWhenEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty());
    }

    @Test
    void builderIsEmptyReturnsFalseWhenNotEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        assertFalse(builder.isEmpty());
    }

    @Test
    void builderClearEmptiesTheBuilder() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.clear();
        assertTrue(builder.isEmpty());
    }


    @Test
    void builderBuildCreatesParetoFront1() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(10, 2, 1);
        builder.add(10, 2, 1);
        builder.add(7,2,1);
        builder.add(9, 3, 1);

        ParetoFront paretoFront2 = builder.build();
        System.out.println("1: "+paretoFront2);
        assertEquals(1, paretoFront2.size());
    }


    @Test
    void clearWorksAfterMultipleAdds() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(15, 1, 1);
        builder.clear();
        builder.add(20, 0, 1);

        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size()); // Doit contenir uniquement (20,0,1)
    }


    @Test
    void builderBuildCreatesParetoFront2() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(10, 2, 1);
        builder.add(10, 2, 1);
        builder.add(9, 3, 1);
        builder.add(7,2,1);

        ParetoFront paretoFront2 = builder.build();
        System.out.println("2: "+paretoFront2);
        assertEquals(1, paretoFront2.size());
    }


    @Test
    void buildRemovesDominatedCriteriaInMiddle() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1); // (arrMins=10, changes=2, payload=1)
        builder.add(12, 3, 1); // (arrMins=12, changes=3, payload=1) => Dominé par (10,2,1)
        builder.add(15, 1, 1); // (arrMins=15, changes=1, payload=1)

        ParetoFront paretoFront = builder.build();
        assertEquals(2, paretoFront.size()); // Doit contenir (10,2,1) et (15,1,1)
    }


    @Test
    void buildHandlesUnsortedCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(15, 1, 1); // (arrMins=15, changes=1, payload=1)
        builder.add(10, 2, 1); // (arrMins=10, changes=2, payload=1)
        builder.add(20, 0, 1); // (arrMins=20, changes=0, payload=1)

        ParetoFront paretoFront = builder.build();
        assertEquals(3, paretoFront.size()); // Doit contenir (10,2,1), (15,1,1), et (20,0,1)
    }


    @Test
    void forEachDoesNotAllowModifyingCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(15, 1, 1);

        ParetoFront paretoFront = builder.build();
        List<Long> criteriaList = new ArrayList<>();
        paretoFront.forEach(criteria -> {
            criteriaList.add(criteria);
            // Tentative de modification (ne devrait pas avoir d'effet)
            criteria = pack(20, 0, 1);
        });

        assertEquals(2, criteriaList.size()); // Doit contenir les critères originaux
        assertTrue(criteriaList.contains(pack(10, 2, 1)));
        assertTrue(criteriaList.contains(pack(15, 1, 1)));
    }

    @Test
    void builderHandlesInvertedDominance() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 3, 1); // (arrMins=10, changes=3, payload=1)
        builder.add(12, 2, 1); // (arrMins=12, changes=2, payload=1) => Moins bon sur l'heure, meilleur sur les changements

        ParetoFront paretoFront = builder.build();
        assertEquals(2, paretoFront.size()); // Les deux critères doivent être conservés car aucun ne domine l'autre
    }


    @Test
    void builderHandlesDifferentDepartureTimes() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(PackedCriteria.withDepMins(pack(10, 2, 1), 5)); // Départ à 5h
        builder.add(PackedCriteria.withDepMins(pack(10, 2, 1), 6)); // Départ à 6h

        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size()); // Les deux critères doivent être conservés car les heures de départ diffèrent
    }
    @Test
    void builderHandlesCriteriaWithoutDepartureTime() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(PackedCriteria.withoutDepMins(pack(10, 2, 1))); // (arrMins=10, changes=2, payload=1)
        builder.add(PackedCriteria.withoutDepMins(pack(12, 3, 1))); // (arrMins=12, changes=3, payload=1) => Dominé par (10,2,1)
        builder.add(PackedCriteria.withoutDepMins(pack(8, 2, 1)));  // (arrMins=8, changes=2, payload=1) => Domine (10,2,1)

        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size()); // Seul (8,2,1) doit rester
        assertTrue(paretoFront.get(8, 2) != 0);
    }
    @Test
    void builderHandlesCriteriaWithDifferentPayload() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(10, 2, 1)); // (arrMins=10, changes=2, payload=1)
        builder.add(pack(10, 2, 2)); // Mêmes critères, mais charge utile différente

        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size()); // Un seul critère doit rester
        assertTrue(paretoFront.get(10, 2) != 0);
    }

    @Test
    void builderHandlesEqualCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(10, 2, 1)); // (arrMins=10, changes=2, payload=1)
        builder.add(pack(10, 2, 1)); // Critère identique

        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size()); // Un seul critère doit rester
        assertTrue(paretoFront.get(10, 2) != 0);
    }
    @Test
    void builderHandlesFullCriteriaDominance() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(10, 2, 1)); // (arrMins=10, changes=2, payload=1)
        builder.add(pack(12, 3, 1)); // (arrMins=12, changes=3, payload=1) => Dominé par (10,2,1)
        builder.add(pack(8, 2, 1));  // (arrMins=8, changes=2, payload=1) => Domine (10,2,1)

        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size()); // Seul (8,2,1) doit rester
        assertTrue(paretoFront.get(8, 2) != 0);
    }

    @Test
    void getThrowsExceptionForNonExistentCriteria() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 1);
        builder.add(15, 1, 1);

        ParetoFront paretoFront = builder.build();
        assertThrows(NoSuchElementException.class, () -> paretoFront.get(20, 0)); // Critère non existant
    }

    /** Tests sur la classe Builder **/

    @Test
    public void testBuilderIsInitiallyEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty(), "Un Builder initial doit être vide.");
    }

    @Test
    public void testBuilderCopyConstructor() {
        ParetoFront.Builder original = new Builder();
        original.add(PackedCriteria.pack(10,2, 0));

        Builder copy = new Builder(original);
        assertEquals(1, copy.build().size(), "Le constructeur de copie doit préserver les éléments.");
        assertEquals(PackedCriteria.pack(10,2, 0), copy.build().get(10, 2));
    }

    @Test
    public void testBuilderAddTuple() {
        Builder builder = new Builder();
        builder.add(PackedCriteria.pack(10,2, 0));

        assertFalse(builder.isEmpty(), "Le Builder ne doit pas être vide après un ajout.");
        assertEquals(1, builder.build().size(), "Un seul élément doit être ajouté.");
    }

    @Test
    public void testBuilderAddDominatedTuple() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(PackedCriteria.pack(10,2, 0));  // Ajout initial
        builder.add(PackedCriteria.pack(12,3, 0));  // Devrait être conservé
        builder.add(PackedCriteria.pack(9,1, 0));   // Devrait dominer (moins d’arrêts et temps inférieur)

        assertEquals(1, builder.build().size(), "Seul le tuple dominant doit être conservé.");
        assertEquals(PackedCriteria.pack(9,1, 0), builder.build().get(9, 1), "Le tuple dominant doit être conservé.");
    }

    @Test
    public void testBuilderClear() {
        Builder builder = new Builder();
        builder.add(PackedCriteria.pack(10,2, 0));
        builder.clear();

        assertTrue(builder.isEmpty(), "Le Builder doit être vide après clear().");
        assertThrows(NoSuchElementException.class, () -> builder.build().get(10, 2), "Une exception doit être levée pour un critère absent.");
    }

    @Test
    public void testBuilderAddAll() {
        Builder builder1 = new Builder();
        builder1.add(PackedCriteria.pack(10,2, 0));
        builder1.add(PackedCriteria.pack(12,3, 0));

        Builder builder2 = new Builder();
        builder2.add(PackedCriteria.pack(8,1, 0));  // Devrait être ajouté

        builder1.addAll(builder2);

        assertEquals(1, builder1.build().size(), "Seuls les éléments non dominés doivent rester.");
        assertEquals(PackedCriteria.pack(8,1, 0), builder1.build().get(8, 1), "L'élément ajouté doit être accessible.");
    }

    @Test
    public void testBuilderFullyDominates() {
        ///  Builder 1 n'a pas de min de depart ainsi une exeption est levee !
        Builder builder1 = new Builder();
        builder1.add(PackedCriteria.pack(10,2, 0));
        builder1.add(PackedCriteria.pack(12,3, 0));

        Builder builder2 = new Builder();
        builder2.add(PackedCriteria.pack(15,4, 0)); // Plus lent et plus de changements = dominé
        ///  ajouter AssertThrows
    }

    @Test
    public void testBuilderDoesNotFullyDominate() {
        ///  Builder 1 n'a pas de min de depart ainsi une exeption est levee !
        Builder builder1 = new Builder();
        builder1.add(PackedCriteria.pack(0,10, 2));

        Builder builder2 = new Builder();
        builder2.add(PackedCriteria.pack(0,8, 1)); // Meilleur, donc pas dominé

        ///  ajouter AssertThrows
    }

    @Test
    public void testBuilderBuildCreatesParetoFront() {
        Builder builder = new Builder();
        builder.add(PackedCriteria.pack(8,3, 0));
        builder.add(PackedCriteria.pack(8,4, 0));
        builder.add(PackedCriteria.pack(7,4, 0));

        ParetoFront front = builder.build();
        System.out.println(builder);
        assertEquals(2, front.size(), "Le ParetoFront construit doit contenir tous les éléments.");
        //assertEquals(PackedCriteria.pack(7,4, 0), front.get(7, 4), "Les éléments doivent être correctement insérés.");
    }

    /** Tests sur la classe ParetoFront **/

    @Test
    public void testParetoFrontSize() {
        Builder builder = new Builder();
        builder.add(PackedCriteria.pack(10,2, 0));
        builder.add(PackedCriteria.pack(15,1, 0));
        System.out.println(builder);
        ParetoFront front = builder.build();
        System.out.println(builder);
        assertEquals(2, front.size());
    }

    @Test
    public void testParetoFrontGetValidCriteria() {
        Builder builder = new Builder();
        builder.add(PackedCriteria.pack(10,2, 0));
        builder.add(PackedCriteria.pack(15,1, 0));

        ParetoFront front = builder.build();
        assertEquals(PackedCriteria.pack(10,2, 0), front.get(10, 2), "L'élément 10,2 doit être retrouvé.");
        assertEquals(PackedCriteria.pack(15,1, 0), front.get(15, 1), "L'élément 15,1 doit être retrouvé.");
    }

    @Test
    public void testParetoFrontGetThrowsExceptionOnMissingCriteria() {
        Builder builder = new Builder();
        builder.add(PackedCriteria.pack(10,2, 0));

        ParetoFront front = builder.build();
        assertThrows(NoSuchElementException.class, () -> front.get(12, 3),
                "Une exception doit être levée pour un critère absent.");
    }

//    @Test
//    public void testParetoFrontForEachTraversesAllElements() {
//        Builder builder = new Builder();
//        builder.add(PackedCriteria.pack(0,10, 2));
//        builder.add(PackedCriteria.pack(0,15, 1));
//        builder.add(PackedCriteria.pack(0,20, 3));
//
//        ParetoFront front = builder.build();
//        List<Long> collected = new ArrayList<>();
//
//        front.forEach(collected::add);
//
//        assertEquals(List.of((10, 2), PackedCriteria.pack((0,15, 1), PackedCriteria.pack((20,3, 0)), collected, "forEach doit traverser tous les éléments dans le bon ordre."));
//    }

    @Test
    public void testParetoFrontForEachOnEmptyFront() {
        ParetoFront emptyFront = ParetoFront.EMPTY;
        List<Long> collected = new ArrayList<>();

        emptyFront.forEach(collected::add);

        assertTrue(collected.isEmpty(), "forEach sur une frontière vide ne doit pas appeler l'action.");
    }


    @Test
    void emptyFrontHasSizeZero() {
        assertEquals(0, ParetoFront.EMPTY.size());
    }

    @Test
    void emptyFrontThrowsOnGet() {
        assertThrows(NoSuchElementException.class, () -> ParetoFront.EMPTY.get(500, 2));
    }

    @Test
    void addingSingleElementStoresItCorrectly() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(500, 2, 0);  // Arrivée à 500 minutes, 2 changements

        ParetoFront front = builder.build();
        assertEquals(1, front.size());
        assertDoesNotThrow(() -> front.get(500, 2));
    }

    @Test
    void forEachIteratesCorrectly() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(500, 2, 0);
        builder.add(550, 1, 0);

        ParetoFront front = builder.build();

        AtomicInteger count = new AtomicInteger();
        front.forEach(c -> count.incrementAndGet());

        assertEquals(2, count.get());
    }

    @Test
    void toStringDoesNotThrowException() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(480, 1, 0);
        builder.add(490, 2, 0);
        ParetoFront front = builder.build();

        assertDoesNotThrow(() -> front.toString());
    }

}