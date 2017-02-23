/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.Test;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DseVersion("5.0.0")
public class DefaultTimestampTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    /**
     * Ensures that if {@link GraphStatement#setDefaultTimestamp} is set on a statement that creates a vertex that
     * the timestamp is used for the cells that are actually stored in cassandra.   Accesses the internal C* tables to
     * do this, so this test may break in the future if the internal storage format for graph data changes.
     * <p/>
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "short")
    public void should_use_default_timestamp_if_set() {
        long ts = (System.currentTimeMillis() - 1) * 1000 + 555;
        // DSE Graph uses the timestamp provided + 1 for updates.
        long expectedTs = ts + 1;
        GraphStatement addVtxStmt = new SimpleGraphStatement("graph.addVertex(label, 'person', 'name', 'tom', 'age', 23);")
                .setDefaultTimestamp(ts);
        GraphResultSet result = session().executeGraph(addVtxStmt);
        Vertex v = result.one().asVertex();
        GraphNode id = v.getId();

        // Access the data as it is stored in C* tables.
        // Note: This could be interpreted as somewhat fragile as it is possible the internal storage format for graph
        // data may change.
        Statement readCqlStatement = select()
                .column("name").writeTime("name").as("wname")
                .column("age").writeTime("age").as("wage")
                .column(quote("~~vertex_exists")).writeTime(quote("~~vertex_exists")).as("wv")
                .from(cluster().getConfiguration().getGraphOptions().getGraphName(), "person_p")
                .where(eq("community_id", id.get("community_id").asLong()));

        ResultSet resultSet = session().execute(readCqlStatement);
        // Each row only contains the value for 1 property, check which property is non-null and ensure its timestamp
        // matches what was set as the default timestamp.
        for (Row row : resultSet) {
            String name = row.getString("name");
            int age = row.getInt("age");
            // The ~~vertex_exists column merely indicates whether or not the vertex exists.
            boolean vertexExists = row.getBool(quote("~~vertex_exists"));

            if (name != null) {
                assertThat(name).isEqualTo("tom");
                assertThat(row.getLong("wname")).isEqualTo(expectedTs);
            } else if (!row.isNull("age")) {
                assertThat(age).isEqualTo(23);
                assertThat(row.getLong("wage")).isEqualTo(expectedTs);
            } else if (!row.isNull(quote("~~vertex_exists"))) {
                assertThat(vertexExists).isTrue();
                assertThat(row.getLong("wv")).isEqualTo(expectedTs);
            } else {
                fail("Got a row where all columns were null.");
            }
        }
    }
}
