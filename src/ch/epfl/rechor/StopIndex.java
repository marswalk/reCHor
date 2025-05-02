package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An index of stop names that allows searching for stops by name.
 * The search is case-insensitive and accent-insensitive.
 */
public final class StopIndex {
    private final List<String> stopNames;
    private final Map<String, String> alternativeToMain;

    /**
     * Creates a new stop index.
     *
     * @param stopNames a list of primary stop names to index
     * @param alternativeNames a map from alternative names to their corresponding primary names
     */
    public StopIndex(List<String> stopNames, Map<String, String> alternativeNames) {
        this.stopNames = new ArrayList<>(stopNames);
        this.alternativeToMain = new HashMap<>(alternativeNames);
    }

    /**
     * Searches for stops that match the given query.
     *
     * @param query the search query, which will be split by spaces into subqueries
     * @param maxResults the maximum number of results to return
     * @return a list of stop names matching the query, sorted by decreasing relevance score
     */
    public List<String> stopsMatching(String query, int maxResults) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be positive");
        }

        // Split the query into subqueries
        String[] subqueries = query.trim().split("\\s+");

        // Build a map of all stops (primary and alternative) to their relevance scores
        Map<String, Integer> scores = new HashMap<>();

        // Process all stop names including alternatives
        Set<String> allNames = new HashSet<>(stopNames);
        allNames.addAll(alternativeToMain.keySet());

        for (String stopName : allNames) {
            int score = calculateScore(stopName, subqueries);
            if (score > 0) {
                // If this is an alternative name, store the score against the primary name
                String primaryName = alternativeToMain.getOrDefault(stopName, stopName);
                scores.merge(primaryName, score, Integer::max);
            }
        }

        // Sort by score (descending) and return the top results
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .distinct()
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Calculates the relevance score of a stop name for the given subqueries.
     */
    private int calculateScore(String stopName, String[] subqueries) {
        int totalScore = 0;

        for (String subquery : subqueries) {
            int subqueryScore = calculateSubqueryScore(stopName, subquery);
            if (subqueryScore == 0) {
                // If any subquery doesn't match, the whole query doesn't match
                return 0;
            }
            totalScore += subqueryScore;
        }

        return totalScore;
    }

    /**
     * Calculates the score for a single subquery.
     */
    private int calculateSubqueryScore(String stopName, String subquery) {
        Pattern pattern = createPatternForSubquery(subquery);
        Matcher matcher = pattern.matcher(stopName);

        if (!matcher.find()) {
            return 0;
        }

        // Calculate base score (percentage of characters matched)
        int matchLength = matcher.end() - matcher.start();
        int baseScore = matchLength * 100 / stopName.length();

        // Apply multipliers for word boundaries
        int multiplier = 1;

        // Check if the match is at the beginning of a word
        boolean isWordStart = matcher.start() == 0 || !Character.isLetter(stopName.charAt(matcher.start() - 1));
        if (isWordStart) {
            multiplier *= 4;
        }

        // Check if the match is at the end of a word
        boolean isWordEnd = matcher.end() == stopName.length() || !Character.isLetter(stopName.charAt(matcher.end()));
        if (isWordEnd) {
            multiplier *= 2;
        }

        return baseScore * multiplier;
    }

    /**
     * Creates a pattern for a subquery with special handling of accented characters.
     */
    private Pattern createPatternForSubquery(String subquery) {
        // Build regex with character classes for accented letters
        StringBuilder regex = new StringBuilder();

        // Character classes for accented variants
        Map<Character, String> charClasses = Map.of(
            'c', "[cç]",
            'a', "[aáàâä]",
            'e', "[eéèêë]",
            'i', "[iíìîï]",
            'o', "[oóòôö]",
            'u', "[uúùûü]"
        );

        for (char c : subquery.toCharArray()) {
            char lowerC = Character.toLowerCase(c);
            String charClass = charClasses.get(lowerC);

            if (charClass != null) {
                // If the character is one that has accented variants
                if (Character.isUpperCase(c)) {
                    // For uppercase characters, preserve case sensitivity
                    regex.append(Pattern.quote(String.valueOf(c)));
                } else {
                    // For lowercase, use the character class
                    regex.append(charClass);
                }
            } else {
                // For other characters, use them directly with proper escaping
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }

        // If subquery has no uppercase letters, make the pattern case insensitive
        int flags = Pattern.UNICODE_CASE;
        if (subquery.equals(subquery.toLowerCase())) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        return Pattern.compile(regex.toString(), flags);
    }
}