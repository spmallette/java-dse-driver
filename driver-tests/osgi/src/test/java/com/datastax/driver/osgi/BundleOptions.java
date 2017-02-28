/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.TestUtils;
import com.google.common.collect.Lists;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.util.PathUtils;

import java.util.List;

import static com.datastax.driver.osgi.VersionProvider.getVersion;
import static com.datastax.driver.osgi.VersionProvider.projectVersion;
import static org.ops4j.pax.exam.CoreOptions.*;

public class BundleOptions {

    public static UrlProvisionOption driverBundle() {
        return driverBundle(false);
    }

    public static UrlProvisionOption driverBundle(boolean useShaded) {
        String classifier = useShaded ? "-shaded" : "";
        return bundle("reference:file:" + PathUtils.getBaseDir() + "/../../driver-core/target/dse-java-driver-core-" + projectVersion() + classifier + ".jar");
    }

    public static MavenArtifactProvisionOption mappingBundle() {
        return mavenBundle("com.datastax.dse", "dse-java-driver-mapping", projectVersion());
    }

    public static MavenArtifactProvisionOption extrasBundle() {
        return mavenBundle("com.datastax.dse", "dse-java-driver-extras", projectVersion());
    }

    public static MavenArtifactProvisionOption guavaBundle() {
        return mavenBundle("com.google.guava", "guava", getVersion("guava.version"));
    }

    public static CompositeOption lz4Bundle() {
        return new CompositeOption() {

            @Override
            public Option[] getOptions() {
                return options(
                        systemProperty("cassandra.compression").value(ProtocolOptions.Compression.LZ4.name()),
                        mavenBundle("net.jpountz.lz4", "lz4", getVersion("lz4.version"))
                );
            }
        };
    }

    public static CompositeOption snappyBundle() {
        return new CompositeOption() {

            @Override
            public Option[] getOptions() {
                return options(
                        systemProperty("cassandra.compression").value(ProtocolOptions.Compression.SNAPPY.name()),
                        mavenBundle("org.xerial.snappy", "snappy-java", getVersion("snappy.version"))
                );
            }
        };
    }

    public static CompositeOption hdrHistogramBundle() {
        return new CompositeOption() {

            @Override
            public Option[] getOptions() {
                return options(
                        systemProperty("cassandra.usePercentileSpeculativeExecutionPolicy").value("true"),
                        mavenBundle("org.hdrhistogram", "HdrHistogram", getVersion("hdr.version"))
                );
            }
        };
    }

    public static CompositeOption nettyBundles() {
        final String nettyVersion = getVersion("netty.version");
        return new CompositeOption() {

            @Override
            public Option[] getOptions() {
                return options(
                        mavenBundle("io.netty", "netty-buffer", nettyVersion),
                        mavenBundle("io.netty", "netty-codec", nettyVersion),
                        mavenBundle("io.netty", "netty-common", nettyVersion),
                        mavenBundle("io.netty", "netty-handler", nettyVersion),
                        mavenBundle("io.netty", "netty-transport", nettyVersion),
                        mavenBundle("io.netty", "netty-resolver", nettyVersion)
                );
            }
        };
    }

    public static UrlProvisionOption mailboxBundle() {
        return bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes");
    }

    public static CompositeOption defaultOptions() {
        return new CompositeOption() {

            @Override
            public Option[] getOptions() {
                List<Option> options = Lists.newArrayList(
                        // Delegate javax.security.cert to the parent classloader.  javax.security.cert.X509Certificate is used in
                        // io.netty.util.internal.EmptyArrays, but not directly by the driver.
                        bootDelegationPackage("javax.security.cert"),
                        systemProperty("cassandra.version").value(CCMBridge.getGlobalCassandraVersion().toString()),
                        systemProperty("cassandra.contactpoints").value(TestUtils.IP_PREFIX + 1),
                        systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),
                        mavenBundle("org.slf4j", "slf4j-api", getVersion("slf4j.version")),
                        mavenBundle("ch.qos.logback", "logback-classic", getVersion("logback.version")),
                        mavenBundle("ch.qos.logback", "logback-core", getVersion("logback.version")),
                        mavenBundle("io.dropwizard.metrics", "metrics-core", getVersion("metrics.version")),
                        mavenBundle("org.testng", "testng", getVersion("testng.version")),
                        systemPackages("org.testng", "org.junit", "org.junit.runner", "org.junit.runner.manipulation",
                                "org.junit.runner.notification", "com.jcabi.manifests")
                );
                if (CCMBridge.isWindows()) {
                    // Workaround for Felix + Windows Server 2012.   Felix does not properly alias 'windowsserver2012'
                    // to 'win32', because of this some native libraries may fail to load.  To work around this, force
                    // the os.name property to win32 if on a windows platform.
                    // See: https://issues.apache.org/jira/browse/FELIX-5184
                    options.add(systemProperty("os.name").value("win32"));
                }

                return options.toArray(new Option[options.size()]);
            }
        };
    }
}
