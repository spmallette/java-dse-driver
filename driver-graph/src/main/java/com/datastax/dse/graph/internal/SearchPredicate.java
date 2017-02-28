/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.internal;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import com.datastax.dse.graph.api.predicates.Search;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * List of predicates for geolocation usage with DseGraph and Search indexes.
 * Should not be accessed directly but through the {@link Search} static methods.
 */
public enum SearchPredicate implements DsePredicate {
    /**
     * Whether the text contains a given term as a token in the text (case insensitive).
     */
    token {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            return value != null && evaluate(value.toString(), (String) condition);
        }

        boolean evaluate(String value, String terms) {
            Set<String> tokens = Sets.newHashSet(tokenize(value.toLowerCase()));
            terms = terms.trim();
            List<String> tokenTerms = tokenize(terms.toLowerCase());
            if (!terms.isEmpty() && tokenTerms.isEmpty()) return false;
            for (String term : tokenTerms) {
                if (!tokens.contains(term)) return false;
            }
            return true;
        }

        @Override
        public boolean isValidCondition(Object condition) {
            if (condition == null) return false;
            else if (StringUtils.isNotBlank((String) condition))
                return true;
            else return false;
        }

        @Override
        public String toString() {
            return "token";
        }

    },

    /**
     * Whether the text contains a token that starts with a given term (case insensitive).
     */
    tokenPrefix {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            return value != null && evaluate(value.toString(), (String) condition);
        }

        boolean evaluate(String value, String prefix) {
            for (String token : tokenize(value.toLowerCase())) {
                if (token.startsWith(prefix.toLowerCase().trim())) return true;
            }
            return false;
        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null;
        }

        @Override
        public String toString() {
            return "tokenPrefix";
        }

    },

    /**
     * Whether the text contains a token that matches a regular expression (case insensitive).
     */
    tokenRegex {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            return value != null && evaluate(value.toString(), (String) condition);
        }

        boolean evaluate(String value, String regex) {
            Pattern compiled = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            for (String token : tokenize(value.toLowerCase())) {
                if (compiled.matcher(token).matches()) return true;
            }
            return false;
        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null && StringUtils.isNotBlank((String) condition);
        }


        @Override
        public String toString() {
            return "tokenRegex";
        }

    },

    /**
     * Whether some token in the text is within a given edit distance from the given term (case insensitive).
     */
    tokenFuzzy {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            if (value == null) return false;

            EditDistance fuzzyCondition = (EditDistance) condition;

            for (String token : tokenize(value.toString().toLowerCase())) {
                if (StringUtils.getLevenshteinDistance(token, fuzzyCondition.query.toLowerCase()) <= fuzzyCondition.distance) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null;
        }


        @Override
        public String toString() {
            return "tokenFuzzy";
        }

    },

    /**
     * Whether the text starts with a given prefix (case sensitive).
     */
    prefix {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            return value != null && value.toString().startsWith(((String) condition).trim());
        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null;
        }

        @Override
        public String toString() {
            return "prefix";
        }

    },

    /**
     * Whether the text matches a regular expression (case sensitive).
     */
    regex {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            if (value == null) {
                return false;
            }
            return Pattern.compile((String) condition, Pattern.DOTALL).matcher(value.toString()).matches();

        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null && StringUtils.isNotBlank((String) condition);
        }

        @Override
        public String toString() {
            return "regex";
        }

    },

    /**
     * Whether the text is within a given edit distance from the given term (case sensitive).
     */
    fuzzy {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            if (value == null) return false;
            EditDistance fuzzyCondition = (EditDistance) condition;
            return StringUtils.getLevenshteinDistance(value.toString(), fuzzyCondition.query) <= fuzzyCondition.distance;
        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null;
        }

        @Override
        public String toString() {
            return "fuzzy";
        }

    },

    /**
     * Whether tokenized text contains a given phrase, optionally within a given proximity (case insensitive).
     */
    phrase {
        @Override
        public boolean test(Object value, Object condition) {
            preEvaluate(condition);
            if (value == null) return false;

            EditDistance phraseCondition = (EditDistance) condition;

            List<String> valueTokens = tokenize(value.toString().toLowerCase());
            List<String> phraseTokens = tokenize(phraseCondition.query.toLowerCase());

            int valuePosition = 0;
            int phrasePosition = 0;
            int distance = 0;

            // Advance the value token position to the first match...
            while (!phraseTokens.get(phrasePosition).equals(valueTokens.get(valuePosition))) {
                valuePosition++;

                // ...but short-circuit if we run out of value tokens:
                if (valuePosition == valueTokens.size()) return false;
            }

            // Look for matches as long as there are unmatched phrase tokens:
            while (phrasePosition < phraseTokens.size()) {
                if (phraseTokens.get(phrasePosition).equals(valueTokens.get(valuePosition))) {
                    // If we match the last phrase token, we've matched the phrase (within the given edit distance):
                    if (phrasePosition == phraseTokens.size() - 1) return true;
                    valuePosition++;
                    phrasePosition++;
                } else {
                    // Look for the next token match...
                    while (!phraseTokens.get(phrasePosition).equals(valueTokens.get(valuePosition))) {
                        distance++;
                        valuePosition++;

                        // ...but short-circuit if we either surpass the edit distance or run out of value tokens:
                        if (distance > phraseCondition.distance || valuePosition == valueTokens.size())
                            return false;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isValidCondition(Object condition) {
            return condition != null;
        }

        @Override
        public String toString() {
            return "phrase";
        }

    };

    static final int MIN_TOKEN_LENGTH = 1;

    static List<String> tokenize(String str) {
        ArrayList<String> tokens = new ArrayList<>();
        int previous = 0;
        for (int p = 0; p < str.length(); p++) {
            if (!Character.isLetterOrDigit(str.charAt(p))) {
                if (p - previous >= MIN_TOKEN_LENGTH)
                    tokens.add(str.substring(previous, p));
                previous = p + 1;
            }
        }
        if (previous + MIN_TOKEN_LENGTH < str.length())
            tokens.add(str.substring(previous, str.length()));
        return tokens;
    }
}
