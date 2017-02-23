/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi;

import com.jcabi.manifests.Manifests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attempts to resolve the project version from the Bundle manifest.  If not present, will throw RuntimeException
 * on initialization.   If this happens, try building with 'mvn compile' to generate the Bundle manifest.
 * <p/>
 * In IntelliJ you can have compile run after make by right clicking on 'compile' in the 'Maven Projects' tool window.
 */
public class VersionProvider {

    private static final Pattern versionPattern = Pattern.compile(("(\\d+.\\d+\\.\\d+)(.*)"));

    private static final String PROJECT_VERSION;

    static {
        String bundleName = Manifests.read("Bundle-SymbolicName");
        if (bundleName.equals("com.datastax.driver.osgi")) {
            String bundleVersion = Manifests.read("Bundle-Version");
            Matcher matcher = versionPattern.matcher(bundleVersion);
            if (matcher.matches()) {
                String majorVersion = matcher.group(1);
                // Replace all instances of '.' after the main version with '-' to properly
                // resolve the correct version.
                String rest = matcher.group(2).replaceAll("\\.", "-");
                PROJECT_VERSION = majorVersion + rest;
            } else {
                // This should never happen, but if we are using a non X.Y.Z version number
                // we'll just back off to the bundle version.
                PROJECT_VERSION = bundleVersion;
            }
        } else {
            throw new RuntimeException("Couldn't resolve bundle manifest (try building with mvn compile)");
        }
    }

    public static String projectVersion() {
        return PROJECT_VERSION;
    }


    public static String getVersion(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            throw new IllegalArgumentException(propertyName + " system property is not set.");
        }
        return value;
    }

}
