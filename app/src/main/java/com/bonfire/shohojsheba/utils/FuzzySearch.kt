package com.bonfire.shohojsheba.utils

/**
 * Calculates the similarity between two strings using a simple fuzzy matching algorithm.
 * Returns a score between 0.0 (no match) and 1.0 (perfect match).
 */
/**
 * Calculates the similarity between two strings using a simple fuzzy matching algorithm.
 * Returns a score between 0.0 (no match) and 1.0 (perfect match).
 *
 * How it works:
 * 1. Checks for exact match (Score 1.0).
 * 2. Checks if one string contains the other (Score 0.8+).
 * 3. Checks word-by-word similarity using Levenshtein Distance (Score based on matching words).
 * 4. Adds a bonus if words are in the same order.
 */
fun calculateSimilarity(query: String, target: String): Double {
    val queryLower = query.lowercase().trim()
    val targetLower = target.lowercase().trim()
    
    // Exact match
    if (queryLower == targetLower) return 1.0
    
    // Contains match
    if (targetLower.contains(queryLower)) {
        return 0.8 + (0.2 * (queryLower.length.toDouble() / targetLower.length))
    }
    
    // Word-based matching
    val queryWords = queryLower.split("\\s+".toRegex())
    val targetWords = targetLower.split("\\s+".toRegex())
    
    val matchingWords = queryWords.count { qWord ->
        targetWords.any { tWord ->
            tWord.contains(qWord) || qWord.contains(tWord) || 
            levenshteinDistance(qWord, tWord) <= 2
        }
    }
    
    val wordMatchScore = matchingWords.toDouble() / queryWords.size
    
    // Bonus for word order preservation
    val orderBonus = if (preservesWordOrder(queryWords, targetWords)) 0.1 else 0.0
    
    return (wordMatchScore * 0.7) + orderBonus
}

/**
 * Checks if the query words appear in the same order in the target
 */
private fun preservesWordOrder(queryWords: List<String>, targetWords: List<String>): Boolean {
    var targetIndex = 0
    for (qWord in queryWords) {
        val found = targetWords.drop(targetIndex).indexOfFirst { it.contains(qWord) || qWord.contains(it) }
        if (found == -1) return false
        targetIndex += found + 1
    }
    return true
}

/**
 * Calculates the Levenshtein distance between two strings
 * (minimum number of single-character edits needed to change one word into the other)
 */
/**
 * Calculates the Levenshtein distance between two strings.
 * This is the minimum number of single-character edits (insertions, deletions, or substitutions)
 * required to change one word into the other.
 * 
 * Example: "kitten" -> "sitting" (distance 3)
 * 1. k -> s (substitution)
 * 2. e -> i (substitution)
 * 3. insert g at the end (insertion)
 */
private fun levenshteinDistance(s1: String, s2: String): Int {
    val len1 = s1.length
    val len2 = s2.length
    
    val dp = Array(len1 + 1) { IntArray(len2 + 1) }
    
    for (i in 0..len1) dp[i][0] = i
    for (j in 0..len2) dp[0][j] = j
    
    for (i in 1..len1) {
        for (j in 1..len2) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,      // deletion
                dp[i][j - 1] + 1,      // insertion
                dp[i - 1][j - 1] + cost // substitution
            )
        }
    }
    
    return dp[len1][len2]
}

/**
 * Filters and sorts services by relevance to the query
 */
/**
 * Filters and sorts a list of items by relevance to a query string.
 * 
 * @param query The user's search text.
 * @param minScore The minimum similarity score (0.0 - 1.0) required to include an item.
 * @param selector A function that extracts a list of searchable strings from an item (e.g., title, keywords).
 * @return A sorted list of items, with the most relevant ones first.
 */
fun <T> List<T>.fuzzyFilter(
    query: String,
    minScore: Double = 0.3,
    selector: (T) -> List<String>
): List<T> {
    return this.mapNotNull { item ->
        // 1. Get all text fields we want to search in (e.g., Title EN, Title BN, Keywords)
        val searchableFields = selector(item)
        
        // 2. Find the best match score among all those fields
        val maxScore = searchableFields.maxOfOrNull { field ->
            calculateSimilarity(query, field)
        } ?: 0.0
        
        // 3. Keep the item only if it meets the minimum score threshold
        if (maxScore >= minScore) {
            item to maxScore // Store item with its score
        } else {
            null // Discard item
        }
    }
    // 4. Sort the results so the best matches appear at the top
    .sortedByDescending { it.second }
    .map { it.first } // Return only the items (discard the scores)
}
