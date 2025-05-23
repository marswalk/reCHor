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
    private static final int SINGLE_CHAR_MINIMUM_THRESHOLD = 100;
    private static final int WORD_START_BONUS = 4;
    private static final int WORD_END_BONUS = 2;
    private static final int EXACT_MATCH_BONUS = 10;
    private static final int SHORT_NAME_BONUS = 5;
    private static final int NON_INITIAL_PENALTY_DIVISOR = 10;
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
     * @param searchTerm the input string to search for. The term is split into tokens by spaces.
     * @param resultLimit the maximum number of results to return. Must be positive.
     * @return a list of stop names matching the search term, sorted by relevance in descending order.
     *         The list contains no duplicates and is limited to {@code resultLimit} entries.
     * @throws IllegalArgumentException if {@code resultLimit} is not positive.
     */
    public List<String> stopsMatching(String searchTerm, int resultLimit) {
        if (resultLimit <= 0) {
            throw new IllegalArgumentException("resultLimit must be positive");
        }

        // If searchTerm is blank, return all station names in alphabetical order
        if (searchTerm == null || searchTerm.isBlank()) {
            return stopNames.stream()
                    .sorted()
                    .limit(resultLimit)
                    .collect(Collectors.toList());
        }

        // Divide the searchTerm into individual tokens
        String[] searchTokens = searchTerm.trim().split("\\s+");

        // Store stops with their relevance scores
        record RankedStop(String name, int relevance) {}

        List<RankedStop> rankedResults = new ArrayList<>();
        // Process all stop names including aliases
        List<String> allStopNames = new ArrayList<>(stopNames);
        allStopNames.addAll(aliasToStopNameMap.keySet());

        for (String stopName : allStopNames) {
            int relevance = computeRelevance(stopName, searchTokens);
            if (relevance > 0) {
                // If this is an alias name, store the relevance against the official name
                String officialStopName = aliasToStopNameMap.getOrDefault(stopName, stopName);
                rankedResults.add(new RankedStop(officialStopName, relevance));
            }
        }

        // Return sorted results
        return rankedResults.stream()
                .sorted(Comparator.comparingInt(RankedStop::relevance).reversed())
                .map(RankedStop::name)
                .distinct() // <-- removes duplicates, keeps first occurrence  
                .limit(resultLimit)
                .collect(Collectors.toList());
    }

    /**
     * Computes the relevance score of a stop name based on the provided search tokens.
     *
     * @param stopName the name of the stop to evaluate.
     * @param searchTokens the tokens derived from the search term.
     * @return the relevance score of the stop name. A higher score indicates a better match.
     */
    private int computeRelevance(String stopName, String[] searchTokens) {
        int aggregateScore = 0;

        for (String token : searchTokens) {
            int tokenScore = evaluateTokenMatch(stopName, token);
            if (tokenScore == 0) {
                // If any token doesn't match, the entire search doesn't match
                return 0;
            }
            aggregateScore += tokenScore;
        }
        return aggregateScore;
    }

    /**
     * Evaluates the match quality of a single search token against a stop name.
     *
     * @param stopName the name of the stop to evaluate.
     * @param token the search token to match.
     * @return the relevance score for the token match. A score of 0 indicates no match.
     */
    private int evaluateTokenMatch(String stopName, String token) {
        Pattern matchPattern = createPatternForToken(token);
        Matcher textMatcher = matchPattern.matcher(stopName);

        if (!textMatcher.find()) {
            return 0;
        }

        // Calculate base relevance (percentage of characters matched)
        int matchLength = textMatcher.end() - textMatcher.start();
        int baseRelevance = matchLength * PERCENTAGE_MULTIPLIER / stopName.length();

        // Apply bonuses for word boundaries
        int weightMultiplier = 1;

        // Check if the match is at the beginning of a word
        boolean startsWord = textMatcher.start() == 0 || !Character.isLetter(stopName.charAt(textMatcher.start() - 1));
        if (startsWord) {
            weightMultiplier *= WORD_START_BONUS;
        }

        // Check if the match is at the end of a word
        boolean endsWord = textMatcher.end() == stopName.length() || !Character.isLetter(stopName.charAt(textMatcher.end()));
        if (endsWord) {
            weightMultiplier *= WORD_END_BONUS;
        }

        // For single-character searches, boost exact word matches and penalize non-initial matches
        if (token.length() == 1) {
            // Exact matches (like just "L" as a station name) get boosted
            if (stopName.equals(token)) {
                weightMultiplier *= EXACT_MATCH_BONUS;
            }
            // Stations that just start with the letter get a moderate boost
            else if (textMatcher.start() == 0 && stopName.length() <= 3) {
                weightMultiplier *= SHORT_NAME_BONUS;
            }
            // Non-initial matches get penalized heavily
            else if (textMatcher.start() > 0) {
                weightMultiplier /= NON_INITIAL_PENALTY_DIVISOR; // Dramatically reduce relevance for non-initial matches
            }
        }

        return baseRelevance * weightMultiplier;
    }

    /**
     * Creates a regular expression pattern for a search token, handling accented characters.
     *
     * @param token the search token to convert into a pattern.
     * @return a {@link Pattern} that matches the token, including its accented variants.
     */
    private Pattern createPatternForToken(String token) {
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

        for (char c : token.toCharArray()) {
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

        // If token has no uppercase letters, make the pattern case insensitive
        int flags = Pattern.UNICODE_CASE;
        if (token.equals(token.toLowerCase())) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        return Pattern.compile(patternBuilder.toString(), flags);
    }
}
