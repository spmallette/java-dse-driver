/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.MaterializedViewMetadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.Test;

import static com.datastax.driver.core.Assertions.assertThat;

@CCMConfig(
        dse = true,
        dseConfig = {
                "authorization_options.enabled:true",
                "authorization_options.allow_row_level_security:true"
        })
@DseVersion("5.1.0")
public class RowLevelAccessTest extends CCMDseTestsSupport {

    /**
     * Validates that the row-level access definition is properly exposed when set on a table.
     *
     * @jira_ticket JAVA-1335
     * @test_category metadata
     */
    @Test(groups = "short")
    public void should_show_RLAC_for_table() throws Exception {
        session().execute(String.format("CREATE TABLE %s.reports (\n" +
                "  report_user text,\n" +
                "  report_number int,\n" +
                "  report_month int,\n" +
                "  report_year int,\n" +
                "  report_text text,\n" +
                "  PRIMARY KEY (report_user, report_number)\n" +
                ");", keyspace));
        String rlacCql = String.format("RESTRICT ROWS ON %s.reports USING report_user;", keyspace);
        session().execute(rlacCql);
        TableMetadata table = cluster().getMetadata().getKeyspace(keyspace).getTable("reports");
        assertThat(table.getOptions().getExtensions()).containsKey("DSE_RLACA");
        assertThat(table.exportAsString()).contains(rlacCql);
    }

    /**
     * Validates that the row-level access definition is properly exposed when set on a MV.
     * <p>
     * TODO after https://datastax.jira.com/browse/DSP-11795 enable test or delete it if MV + RLAC not supported
     *
     * @jira_ticket JAVA-1335, DSP-11123
     * @test_category metadata
     */
    @Test(groups = "short", enabled = false)
    public void should_show_RLAC_for_materialized_view() throws Exception {
        session().execute(String.format("CREATE TABLE %s.reports (\n" +
                "  report_user text,\n" +
                "  report_number int,\n" +
                "  report_month int,\n" +
                "  report_year int,\n" +
                "  report_text text,\n" +
                "  PRIMARY KEY (report_user, report_number)\n" +
                ");", keyspace));
        session().execute(String.format(
                "CREATE MATERIALIZED VIEW %s.reports_by_year AS "
                        + "SELECT report_year, report_user, report_number, report_text FROM %s.reports "
                        + "WHERE report_user IS NOT NULL AND report_number IS NOT NULL AND report_year IS NOT NULL "
                        + "PRIMARY KEY ((report_year, report_user), report_number)",
                keyspace, keyspace));
        String rlacCql = String.format("RESTRICT ROWS ON %s.reports_by_year USING report_user;", keyspace);
        session().execute(rlacCql);
        MaterializedViewMetadata mv = cluster().getMetadata().getKeyspace(keyspace).getMaterializedView("reports_by_year");
        assertThat(mv.getOptions().getExtensions()).containsKey("DSE_RLACA");
        assertThat(mv.exportAsString()).contains(rlacCql);
    }

}
