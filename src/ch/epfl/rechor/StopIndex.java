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
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be positive");
        }

        // If query is blank, return all stop names in alphabetical order
        if (query == null || query.isBlank()) {
            return stopNames.stream()
                    .sorted()
                    .limit(maxResults)
                    .collect(Collectors.toList());
        }

        // Split the query into subqueries
        String[] subqueries = query.trim().split("\\s+");

        // Build a map of all stops (primary and alternative) to their relevance scores
//        Map<String, Integer> scores = new HashMap<>();
        record ScoredStop(String name, int score) {}

        List<ScoredStop> scoredStops = new ArrayList<>();
        // Process all stop names including alternatives
        List<String> allNames = new ArrayList(stopNames);
        allNames.addAll(alternativeToMain.keySet());

        for (String stopName : allNames) {
            int score = calculateScore(stopName, subqueries);
            if (score > 0) {
                // If this is an alternative name, store the score against the primary name
                String primaryName = alternativeToMain.getOrDefault(stopName, stopName);
                scoredStops.add(new ScoredStop(primaryName, score));
            }
        }

        // For single-character queries, require at least a high match score
        // This helps prevent irrelevant results when typing just one letter
        if (query.trim().length() == 1) {
            int threshold = 100; // The score needs to be substantial to include
            // scoredStops.removeIf(entry -> entry < threshold);
        }

        // Custom comparator that implements the specific ordering rules
        Comparator<String> customComparator = (a, b) -> {
            // First compare by scores (descending order)
           // int scoreComparison = Integer.compare(scores.get(b), scores.get(a));
            //if (scoreComparison != 0) {
           //     return scoreComparison;
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
        return scoredStops.stream()
                .sorted(Comparator.comparingInt(ScoredStop::score).reversed()).map(ScoredStop::name)
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

        // For single-character queries, boost exact word matches and penalize non-initial matches
        if (subquery.length() == 1) {
            // Exact matches (like just "L" as a station name) get boosted
            if (stopName.equals(subquery)) {
                multiplier *= 10;
            }
            // Stations that just start with the letter get a moderate boost
            else if (matcher.start() == 0 && stopName.length() <= 3) {
                multiplier *= 5;
            }
            // Non-initial matches get penalized heavily
            else if (matcher.start() > 0) {
                multiplier /= 10; // Dramatically reduce score for non-initial matches
            }
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
