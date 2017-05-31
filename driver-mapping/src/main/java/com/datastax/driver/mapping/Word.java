/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

/**
 * Represents a single-individual word in a property name. (e.g. "my", "xml" and "parser" in a
 * property named "myXmlParser").
 * Each word contains a String value and a boolean indicating whether or not
 * the value is an abbreviation.
 * In most cases there will be no trivial way to identify abbreviations
 * (i.e. my_xml_parser in snake case), but for some naming conventions this may be helpful.
 */
public class Word {

    private final String value;

    private final boolean isAbbreviation;

    public Word(String value, boolean isAbbreviation) {
        this.value = value;
        this.isAbbreviation = isAbbreviation;
    }

    public Word(String value) {
        this(value, false);
    }

    public String getValue() {
        return value;
    }

    public boolean isAbbreviation() {
        return isAbbreviation;
    }

}
