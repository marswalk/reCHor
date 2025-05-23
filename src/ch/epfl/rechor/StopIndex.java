package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A searchable index of transit stops that provides functionality for finding stops by name.
 * The search is designed to be case-insensitive and accent-insensitive, allowing for flexible queries.
 * <p>
 * This class is immutable and thread-safe.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public final class StopIndex {
    // Constants for magic numbers
    private static final int WORD_START_BONUS = 4;
    private static final int WORD_END_BONUS = 2;
    private static final int PERCENTAGE_MULTIPLIER = 100;

    private final List<String> stopNames;
    private final Map<String, String> aliasToStopNameMap;

    /**
     * Initializes a new stop index.
     *
     * @param stopNames a list of official stop names to be indexed.
     * @param stopAliases a mapping of alias names to their corresponding official stop names.
     *                    Alias names are treated as equivalent to their official names during searches.
     */
    public StopIndex(List<String> stopNames, Map<String, String> stopAliases) {
        this.stopNames = new ArrayList<>(stopNames);
        this.aliasToStopNameMap = new HashMap<>(stopAliases);
    }

    /**
     * Searches for stops that match the given search term.
     *
     * @param query the input string to search for. The term is split into sub-queries by spaces.
     * @param resultLimit the maximum number of results to return. Must be positive.
     * @return a list of stop names matching the search term, sorted by relevance in descending order.
     *         The list contains no duplicates and is limited to {@code resultLimit} entries.
     * @throws IllegalArgumentException if {@code resultLimit} is not positive.
     */
    public List<String> stopsMatching(String query, int resultLimit) {
        if (resultLimit <= 0) {
            throw new IllegalArgumentException("resultLimit must be positive");
        }

        // If query is blank, return all station names in alphabetical order
        if (query == null || query.isBlank()) {
            return stopNames.stream()
                    .sorted()
                    .limit(resultLimit)
                    .collect(Collectors.toList());
        }

        // Divide the query into individual sub-queries
        String[] subQueries = query.trim().split("\\s+");

        // Store stops with their relevance scores
        record ScoredStop(String name, int relevance) {}

        List<ScoredStop> rankedResults = new ArrayList<>();
        // Process all stop names including aliases
        List<String> allStopNames = new ArrayList<>(stopNames);
        allStopNames.addAll(aliasToStopNameMap.keySet());

        for (String stopName : allStopNames) {
            int relevanceScore = computeRelevanceScore(stopName, subQueries);
            if (relevanceScore > 0) {
                // If this is an alias name, store the relevance against the official name
                String officialStopName = aliasToStopNameMap.getOrDefault(stopName, stopName);
                rankedResults.add(new ScoredStop(officialStopName, relevanceScore));
            }
        }

        // Return sorted results
        return rankedResults.stream()
                .sorted(Comparator.comparingInt(ScoredStop::relevance).reversed())
                .map(ScoredStop::name)
                .distinct() // <-- removes duplicates, keeps first occurrence
                .limit(resultLimit)
                .collect(Collectors.toList());
    }

    /**
     * Computes the relevance score of a stop name based on the provided sub-queries.
     *
     * @param stopName the name of the stop to evaluate.
     * @param subQueries the sub-queries derived from the search query.
     * @return the relevance score of the stop name. A higher score indicates a better match.
     */
    private int computeRelevanceScore(String stopName, String[] subQueries) {
        int totalScore = 0;

        for (String subQuery : subQueries) {
            int subQueryScore = evaluateSubQueryMatch(stopName, subQuery);
            if (subQueryScore == 0) {
                // If any sub-query doesn't match, the entire search doesn't match
                return 0;
            }
            totalScore += subQueryScore;
        }
        return totalScore;
    }

    /**
     * Evaluates the match quality of a single sub-query against a stop name.
     *
     * @param stopName the name of the stop to evaluate.
     * @param subQuery the sub-query to match.
     * @return the relevance score for the sub-query match. A score of 0 indicates no match.
     */
    private int evaluateSubQueryMatch(String stopName, String subQuery) {
        Pattern matchPattern = createPatternForSubQuery(subQuery);
        Matcher matcher = matchPattern.matcher(stopName);

        if (!matcher.find()) {
            return 0;
        }

        // Calculate base score (percentage of characters matched)
        int matchLength = matcher.end() - matcher.start();
        int baseScore = matchLength * PERCENTAGE_MULTIPLIER / stopName.length();

        // Apply bonuses for word boundaries
        int bonusMultiplier = 1;

        // Check if the match is at the beginning of a word
        boolean startsWord = matcher.start() == 0 || !Character.isLetter(stopName.charAt(matcher.start() - 1));
        if (startsWord) {
            bonusMultiplier *= WORD_START_BONUS;
        }

        // Check if the match is at the end of a word
        boolean endsWord = matcher.end() == stopName.length() || !Character.isLetter(stopName.charAt(matcher.end()));
        if (endsWord) {
            bonusMultiplier *= WORD_END_BONUS;
        }
        return baseScore * bonusMultiplier;
    }

    /**
     * Creates a regular expression pattern for a sub-query, handling accented characters.
     *
     * @param subQuery the sub-query to convert into a pattern.
     * @return a {@link Pattern} that matches the sub-query, including its accented variants.
     */
    private Pattern createPatternForSubQuery(String subQuery) {
        // Build regex with character classes for accented letters
        StringBuilder patternBuilder = new StringBuilder();

        // Character classes for accented variants
        Map<Character, String> accentVariants = Map.of(
                'c', "[cç]",
                'a', "[aáàâä]",
                'e', "[eéèêë]",
                'i', "[iíìîï]",
                'o', "[oóòôö]",
                'u', "[uúùûü]"
        );

        for (char c : subQuery.toCharArray()) {
            char lowerC = Character.toLowerCase(c);
            String charVariants = accentVariants.get(lowerC);

            if (charVariants != null) {
                // If the character is one that has accented variants
                if (Character.isUpperCase(c)) {
                    // For uppercase characters, preserve case sensitivity
                    patternBuilder.append(Pattern.quote(String.valueOf(c)));
                } else {
                    // For lowercase, use the character class
                    patternBuilder.append(charVariants);
                }
            } else {
                // For other characters, use them directly with proper escaping
                patternBuilder.append(Pattern.quote(String.valueOf(c)));
            }
        }

        // If sub-query has no uppercase letters, make the pattern case insensitive
        int flags = Pattern.UNICODE_CASE;
        if (subQuery.equals(subQuery.toLowerCase())) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        return Pattern.compile(patternBuilder.toString(), flags);
    }
}