/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api.predicates;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchTest {

    @Test(groups = "unit")
    public void testToken() {
        P<Object> p = Search.token("needle");
        assertThat(p.test("needle")).isTrue();
        assertThat(p.test("This is a needle in a haystack")).isTrue();
        assertThat(p.test("This is just the haystack")).isFalse();
    }

    @Test(groups = "unit")
    public void testPrefix() {
        P<Object> p = Search.prefix("abcd");
        assertThat(p.test("abcd")).isTrue();
        assertThat(p.test("abcdefg hijkl")).isTrue();
        assertThat(p.test("zabcd")).isFalse();
    }

    @Test(groups = "unit")
    public void testTokenPrefix() {
        P<Object> p = Search.tokenPrefix("abcd");
        assertThat(p.test("abcd")).isTrue();
        assertThat(p.test("abcdefg hijkl")).isTrue();
        assertThat(p.test("z abcd")).isTrue();
        assertThat(p.test("ab cd")).isFalse();
    }

    @Test(groups = "unit")
    public void testRegex() {
        P<Object> p = Search.regex("(foo|bar)");
        assertThat(p.test("foo")).isTrue();
        assertThat(p.test("bar")).isTrue();
        assertThat(p.test("foo bar")).isFalse();
    }

    @Test(groups = "unit")
    public void testTokenRegex() {
        P<Object> p = Search.tokenRegex("(foo|bar)");
        assertThat(p.test("foo")).isTrue();
        assertThat(p.test("bar")).isTrue();
        assertThat(p.test("foo bar")).isTrue();
        assertThat(p.test("foo bar qix")).isTrue();
        assertThat(p.test("qix")).isFalse();
    }

    @Test(groups = "unit")
    public void testPhrase() {
        P<Object> p = Search.phrase("Hello world", 2);
        assertThat(p.test("Hello World")).isTrue();
        assertThat(p.test("Hello Big World")).isTrue();
        assertThat(p.test("Hello Big Wild World")).isTrue();
        assertThat(p.test("Hello The Big Wild World")).isFalse();
        assertThat(p.test("Goodbye world")).isFalse();
    }

    @Test(groups = "unit")
    public void testFuzzy() {
        P<Object> p = Search.fuzzy("abc", 1);
        assertThat(p.test("abcd")).isTrue();
        assertThat(p.test("ab")).isTrue();
        assertThat(p.test("abce")).isTrue();
        assertThat(p.test("abdc")).isTrue();
        assertThat(p.test("badc")).isFalse();

        // Make sure we do NOT calculate the Damerau–Levenshtein distance (2), but the optimal string alignment distance (3):
        assertThat(Search.tokenFuzzy("ca", 2).test("abc")).isFalse();
    }

    @Test(groups = "unit")
    public void testTokenFuzzy() {
        P<Object> p = Search.tokenFuzzy("abc", 1);
        assertThat(p.test("foo abcd")).isTrue();
        assertThat(p.test("foo ab")).isTrue();
        assertThat(p.test("foo abce")).isTrue();
        assertThat(p.test("foo abdc")).isTrue();
        assertThat(p.test("foo badc")).isFalse();

        // Make sure we do NOT calculate the Damerau–Levenshtein distance (2), but the optimal string alignment distance (3):
        assertThat(Search.tokenFuzzy("ca", 2).test("abc 123")).isFalse();
    }

}
