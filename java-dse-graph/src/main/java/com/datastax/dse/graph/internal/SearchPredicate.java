/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.internal;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import com.datastax.dse.graph.api.predicates.Search;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * List of predicates for geolocation usage with DseGraph and Search indexes.
 * Should not be accessed directly but through the {@link Search} static methods.
 */
public enum SearchPredicate implements BiPredicate<Object, Object> {
    token {
        boolean evaluateRaw(String value, String terms) {
            HashSet tokens = Sets.newHashSet(tokenize(value.toLowerCase()));
            terms = terms.trim();
            List tokenTerms = tokenize(terms.toLowerCase());
            if (!terms.isEmpty() && tokenTerms.isEmpty()) {
                return false;
            } else {
                Iterator var5 = tokenTerms.iterator();

                String term;
                do {
                    if (!var5.hasNext()) {
                        return true;
                    }

                    term = (String) var5.next();
                } while (tokens.contains(term));

                return false;
            }
        }
    },
    tokenPrefix {
        boolean evaluateRaw(String value, String prefix) {
            Iterator var3 = tokenize(value.toLowerCase()).iterator();

            String token;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                token = (String) var3.next();
            } while (!SearchPredicate.prefix.evaluateRaw(token, prefix.toLowerCase()));

            return true;
        }
    },
    tokenRegex {
        boolean evaluateRaw(String value, String regex) {
            Iterator var3 = tokenize(value.toLowerCase()).iterator();

            String token;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                token = (String) var3.next();
            } while (!SearchPredicate.regex.evaluateRaw(token, regex));

            return true;
        }
    },
    prefix {
        boolean evaluateRaw(String value, String prefix) {
            return value.startsWith(prefix.trim());
        }
    },
    regex {
        boolean evaluateRaw(String value, String regex) {
            return value.matches(regex);
        }
    };

    public boolean test(Object value, Object condition) {
        return value != null && this.evaluateRaw(value.toString(), (String) condition);
    }

    abstract boolean evaluateRaw(String var1, String var2);

    public static List<String> tokenize(String str) {
        List<String> tokens = new ArrayList<>();
        int previous = 0;

        for (int p = 0; p < str.length(); ++p) {
            if (!Character.isLetterOrDigit(str.charAt(p))) {
                if (p > previous + 1) {
                    tokens.add(str.substring(previous, p));
                }

                previous = p + 1;
            }
        }

        if (previous + 1 < str.length()) {
            tokens.add(str.substring(previous, str.length()));
        }

        return tokens;
    }
}
