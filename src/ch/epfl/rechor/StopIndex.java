package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A searchable registry of transit locations that provides functionality for finding stations by name.
 * The search functionality is designed to be both case-insensitive and accent-insensitive.
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
     * Initializes a new station registry.
     *
     * @param stopNames a collection of official station names to be indexed
     * @param stopAliases a mapping from synonym names to their corresponding official names
     */
    public StopIndex(List<String> stopNames, Map<String, String> stopAliases) {
        this.stopNames = new ArrayList<>(stopNames);
        this.aliasToStopNameMap = new HashMap<>(stopAliases);
    }

    /**
     * Retrieves stations that correspond to the provided search term.
     *
     * @param searchTerm the input string to search for, which will be divided by spaces into search tokens
     * @param resultLimit the upper bound on number of results to return
     * @return a list of station names that match the search criteria, ordered by relevance score in descending order
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
        List<String> allStopNames = new ArrayList(stopNames);
        allStopNames.addAll(aliasToStopNameMap.keySet());

        for (String stopName : allStopNames) {
            int relevance = computeRelevance(stopName, searchTokens);
            if (relevance > 0) {
                // If this is an alias name, store the relevance against the official name
                String officialStopName = aliasToStopNameMap.getOrDefault(stopName, stopName);
                rankedResults.add(new RankedStop(officialStopName, relevance));
            }
        }

        // For single-character searches, require at least a high match relevance
        // This helps prevent irrelevant results when typing just one letter
        if (searchTerm.trim().length() == 1) {
            int minimumThreshold = SINGLE_CHAR_MINIMUM_THRESHOLD;
            // rankedResults.removeIf(entry -> entry < minimumThreshold);
        }

        // Custom comparator that implements the specific ordering rules
        Comparator<String> customComparator = (a, b) -> {
            // First compare by relevance (descending order)
            // int relevanceComparison = Integer.compare(scores.get(b), scores.get(a));
            //if (relevanceComparison != 0) {
            //     return relevanceComparison;
            // }

            // Get the base names (before any comma or space)
            String baseA = a.split("[, ]")[0];
            String baseB = b.split("[, ]")[0];

            // If base names are different, compare them
            int baseComparison = baseA.compareToIgnoreCase(baseB);
            if (baseComparison != 0) {
                return baseComparison;
            }

            // If base names are the same:
            // 1. Names with comma come before names without comma
            boolean aHasComma = a.contains(",");
            boolean bHasComma = b.contains(",");

            if (aHasComma && !bHasComma) {
                return -1;
            } else if (!aHasComma && bHasComma) {
                return 1;
            }

            // 2. If both have or don't have commas, sort alphabetically
            return a.compareToIgnoreCase(b);
        };

        // Return sorted results
        return rankedResults.stream()
                .sorted(Comparator.comparingInt(RankedStop::relevance).reversed())
                .map(RankedStop::name)
                .distinct() // <-- removes duplicates, keeps first occurrence
                .limit(resultLimit)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the overall relevance value of a station name based on the provided search tokens.
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
     * Evaluates the match quality for a single search token.
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
     * Generates a search pattern for a token with special handling for accented characters.
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
