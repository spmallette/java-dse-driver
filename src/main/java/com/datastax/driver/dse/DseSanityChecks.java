/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.VersionNumber;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DseSanityChecks {

    private static final Logger LOGGER = LoggerFactory.getLogger(DseSanityChecks.class);

    private static final Pattern VERSION_RANGE_PATTERN = Pattern.compile("\\[(.+?),(.+?)\\)");

    /**
     * Performs a series of runtime checks to ensure the environment does not have any
     * incompatible libraries or configurations.
     * <p/>
     * Depending on the severity of an
     * incompatibility, an {@link IllegalStateException} may be thrown or an ERROR or
     * WARNING is logged.
     *
     * @throws IllegalStateException If an environment incompatibility is detected.
     */
    static void check() {
        checkRuntimeCoreVersionCompatibility(
                Cluster.getDriverVersion(),
                DseCluster.getCompatibleCoreDriverVersionRange());
    }

    /**
     * Checks if the runtime version of the core java driver is within the
     * compatible version range. Logs a warning if the version is not within
     * the allowed range.
     */
    @VisibleForTesting
    static void checkRuntimeCoreVersionCompatibility(String driverVersion, String driverVersionRange) {
        if (!isWithinRange(driverVersion, driverVersionRange)) {
            LOGGER.warn("Detected incompatible core driver version: {}. Compatible core driver versions should be in the range {}.",
                    driverVersion,
                    driverVersionRange);
        }
    }

    @VisibleForTesting
    static boolean isWithinRange(String version, String range) {
        VersionNumber coreDriverVersion = VersionNumber.parse(version);
        // for the upper bound, we should not consider any pre-release label or any other qualifier, i.e.
        // 3.1.0-rc1 or 3.1.0-SNAPSHOT shouldn't be allowed in the range [3.0.0,3.1.0),
        // but VersionNumber would have accepted it because 3.1.0-rc1 < 3.1.0,
        // so we consider the "stable" version instead
        VersionNumber coreDriverStableVersion = coreDriverVersion.nextStable();
        Matcher matcher = VERSION_RANGE_PATTERN.matcher(range);
        if (matcher.matches()) {
            VersionNumber lowerBoundInclusive = VersionNumber.parse(matcher.group(1));
            VersionNumber upperBoundExclusive = VersionNumber.parse(matcher.group(2));
            return coreDriverVersion.compareTo(lowerBoundInclusive) >= 0
                    && coreDriverStableVersion.compareTo(upperBoundExclusive) < 0;
        } else {
            throw new IllegalStateException("Cannot parse version range: " + range);
        }
    }

}
