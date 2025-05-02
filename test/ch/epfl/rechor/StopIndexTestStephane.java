package ch.epfl.rechor;

import ch.epfl.rechor.StopIndex;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StopIndexTestStephane {

    private static final List<String> STOPS = List.of(
            "Mézières FR, village",
            "Mézières VD, village",
            "Mézery-près-Donneloye, village",
            "Charleville-Mézières",
            "Lausanne"
    );

    private static final Map<String, String> ALTERNATIVE_NAMES = Map.of(
            "Losanna", "Lausanne"
    );

    @Test
    void testStrictCaseSensitivityBlocksMatch() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        // 'meZ' has uppercase Z, so the match is case-sensitive and fails
        List<String> results = index.stopsMatching("meZ vil", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testAccentsMatterInStrictMatch() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        // 'mèz' uses a grave accent, but stop name uses 'é'
        List<String> results = index.stopsMatching("mèz vil", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testAlternativeNameReturnsMainNameOnly() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        List<String> results = index.stopsMatching("losanna", 10);
        assertEquals(List.of("Lausanne"), results);
    }

    @Test
    void testMaxStopsLimitIsRespected() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        List<String> results = index.stopsMatching("mez vil", 2);
        assertEquals(2, results.size());
    }

    @Test
    void testInvalidMaxStopsThrows() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        assertThrows(IllegalArgumentException.class, () -> index.stopsMatching("mez", 0));
    }

    @Test
    void testEmptyQueryReturnsEmptyList() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        List<String> results = index.stopsMatching("", 10);
        assertTrue(results.isEmpty());
    }
    @Test
    void testQueryMatchesAtStartOfWordGetsMultiplier4() {
        List<String> stops = List.of("Village de Mézières");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> results = index.stopsMatching("vill", 10);
        assertEquals(List.of("Village de Mézières"), results); // start of word: score x4
    }

    @Test
    void testQueryMatchesAtEndOfWordGetsMultiplier2() {
        List<String> stops = List.of("Chemin de Donneloye");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> results = index.stopsMatching("loye", 10);
        assertEquals(List.of("Chemin de Donneloye"), results); // end of word: score x2
    }

    @Test
    void testQueryMatchesFullWordGetsMultiplier8() {
        List<String> stops = List.of("Mézières village");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> results = index.stopsMatching("village", 10);
        assertEquals(List.of("Mézières village"), results); // start + end of word: x8
    }

    @Test
    void testSubqueryAppearsMultipleTimesOnlyFirstIsUsed() {
        List<String> stops = List.of("mez mez mez");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> results = index.stopsMatching("mez", 10);
        assertEquals(List.of("mez mez mez"), results); // should only count the first "mez"
    }

    @Test
    void testDifferentAccentVariantsMatchSameLetter() {
        List<String> stops = List.of("Mézières", "Mezieres", "Mëzières");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> results = index.stopsMatching("mez", 10);
        assertTrue(results.containsAll(List.of("Mézières", "Mezieres", "Mëzières")));
    }

    @Test
    void testQueryWithMultipleWordsInAnyOrderMatches() {
        List<String> stops = List.of("Village Mézières");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> results = index.stopsMatching("mez vill", 10);
        assertEquals(List.of("Village Mézières"), results);

        results = index.stopsMatching("vill mez", 10);
        assertEquals(List.of("Village Mézières"), results); // order should not matter
    }

    @Test
    void testNoMatchesReturnsEmptyList() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        List<String> results = index.stopsMatching("inexistant", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testLimitIsSmallerThanMatchingResults() {
        StopIndex index = new StopIndex(STOPS, ALTERNATIVE_NAMES);

        List<String> results = index.stopsMatching("mez", 2); // more than 2 matches exist
        assertEquals(2, results.size());
    }
}

