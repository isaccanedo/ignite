/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.database;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.DynamicCacheDescriptor;
import org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager;
import org.apache.ignite.internal.processors.cache.persistence.checkpoint.CheckpointListener;
import org.apache.ignite.internal.processors.query.QuerySchema;
import org.apache.ignite.internal.processors.query.QueryUtils;
import org.apache.ignite.testframework.junits.WithSystemProperty;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_SKIP_CONFIGURATION_CONSISTENCY_CHECK;

/**
 *
 */
@WithSystemProperty(key = IGNITE_SKIP_CONFIGURATION_CONSISTENCY_CHECK, value = "true")
public class IgnitePersistentStoreSchemaLoadTest extends GridCommonAbstractTest {
    /** Cache name. */
    private static final String TMPL_NAME = "test_cache*";

    /** Table name. */
    private static final String TBL_NAME = Person.class.getSimpleName();

    /** Name of the cache created with {@code CREATE TABLE}. */
    private static final String SQL_CACHE_NAME = QueryUtils.createTableCacheName(QueryUtils.DFLT_SCHEMA, TBL_NAME);

    /** Name of the cache created upon cluster start. */
    private static final String STATIC_CACHE_NAME = TBL_NAME;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        cfg.setCacheConfiguration(cacheCfg(TMPL_NAME));

        DataStorageConfiguration pCfg = new DataStorageConfiguration();

        pCfg.setDefaultDataRegionConfiguration(new DataRegionConfiguration()
            .setPersistenceEnabled(true)
            .setMaxSize(100L * 1024 * 1024));

        pCfg.setCheckpointFrequency(1000);

        cfg.setDataStorageConfiguration(pCfg);

        return cfg;
    }

    /**
     * Create node configuration with a cache pre-configured.
     * @param gridName Node name.
     * @return Node configuration with a cache pre-configured.
     * @throws Exception if failed.
     */
    @SuppressWarnings("unchecked")
    private IgniteConfiguration getConfigurationWithStaticCache(String gridName) throws Exception {
        IgniteConfiguration cfg = getConfiguration(gridName);

        CacheConfiguration ccfg = cacheCfg(STATIC_CACHE_NAME);

        ccfg.setIndexedTypes(Integer.class, Person.class);
        ccfg.setSqlEscapeAll(true);

        cfg.setCacheConfiguration(ccfg);

        return optimize(cfg);
    }

    /** */
    private CacheConfiguration cacheCfg(String name) {
        CacheConfiguration<?, ?> cfg = new CacheConfiguration<>();

        cfg.setName(name);

        cfg.setRebalanceMode(CacheRebalanceMode.NONE);

        cfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        stopAllGrids();

        cleanPersistenceDir();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();

        cleanPersistenceDir();
    }

    /** */
    @Test
    public void testDynamicSchemaChangesPersistence() throws Exception {
        checkSchemaStateAfterNodeRestart(false);
    }

    /** */
    @Test
    public void testDynamicSchemaChangesPersistenceWithAliveCluster() throws Exception {
        checkSchemaStateAfterNodeRestart(true);
    }

    /** */
    @Test
    public void testDynamicSchemaChangesPersistenceWithStaticCache() throws Exception {
        IgniteEx node = startGrid(getConfigurationWithStaticCache(getTestIgniteInstanceName(0)));

        node.cluster().state(ClusterState.ACTIVE);

        IgniteCache cache = node.cache(STATIC_CACHE_NAME);

        assertNotNull(cache);

        CountDownLatch cnt = checkpointLatch(node);

        assertEquals(0, indexCnt(node, STATIC_CACHE_NAME));

        makeDynamicSchemaChanges(node, STATIC_CACHE_NAME);

        checkDynamicSchemaChanges(node, STATIC_CACHE_NAME);

        cnt.await();

        stopGrid(0);

        // Restarting with no-cache configuration - otherwise stored configurations
        // will be ignored due to cache names duplication.
        node = startGrid(0);

        node.cluster().state(ClusterState.ACTIVE);

        checkDynamicSchemaChanges(node, STATIC_CACHE_NAME);
    }

    /**
     * Perform test with cache created with {@code CREATE TABLE}.
     * @param aliveCluster Whether there should remain an alive node when tested node is restarted.
     * @throws Exception if failed.
     */
    private void checkSchemaStateAfterNodeRestart(boolean aliveCluster) throws Exception {
        IgniteEx node = startGrid(0);

        node.cluster().state(ClusterState.ACTIVE);

        if (aliveCluster)
            startGrid(1);

        CountDownLatch cnt = checkpointLatch(node);

        node.context().query().querySqlFields(
            new SqlFieldsQuery("create table \"Person\" (\"id\" int primary key, \"name\" varchar)"), false);

        assertEquals(0, indexCnt(node, SQL_CACHE_NAME));

        makeDynamicSchemaChanges(node, QueryUtils.DFLT_SCHEMA);

        checkDynamicSchemaChanges(node, SQL_CACHE_NAME);

        cnt.await();

        stopGrid(0);

        node = startGrid(0);

        node.cluster().state(ClusterState.ACTIVE);

        checkDynamicSchemaChanges(node, SQL_CACHE_NAME);

        node.context().query().querySqlFields(new SqlFieldsQuery("drop table \"Person\""), false).getAll();
    }

    /** */
    private int indexCnt(IgniteEx node, String cacheName) {
        DynamicCacheDescriptor desc = node.context().cache().cacheDescriptor(cacheName);

        int cnt = 0;

        if (desc != null) {
            QuerySchema schema = desc.schema();
            if (schema != null) {
                for (QueryEntity entity : schema.entities())
                    cnt += entity.getIndexes().size();
            }
        }
        return cnt;
    }

    /** */
    private int colsCnt(IgniteEx node, String cacheName) {
        DynamicCacheDescriptor desc = node.context().cache().cacheDescriptor(cacheName);

        int cnt = 0;

        if (desc != null) {
            QuerySchema schema = desc.schema();
            if (schema != null) {

                for (QueryEntity entity : schema.entities())
                    cnt += entity.getFields().size();
            }
        }

        return cnt;
    }

    /**
     * @param node Node whose checkpoint to wait for.
     * @return Latch released when checkpoint happens.
     */
    private CountDownLatch checkpointLatch(IgniteEx node) {
        final CountDownLatch cnt = new CountDownLatch(1);

        GridCacheDatabaseSharedManager db = (GridCacheDatabaseSharedManager)node.context().cache().context().database();

        db.addCheckpointListener(new CheckpointListener() {
            @Override public void onMarkCheckpointBegin(Context ctx) {
                cnt.countDown();
            }

            @Override public void beforeCheckpointBegin(Context ctx) throws IgniteCheckedException {

            }

            @Override public void onCheckpointBegin(Context ctx) {
                /* No-op. */
            }
        });

        return cnt;
    }

    /**
     * Create dynamic index and column.
     * @param node Node.
     * @param schema Schema name.
     */
    private void makeDynamicSchemaChanges(IgniteEx node, String schema) {
        node.context().query().querySqlFields(
            new SqlFieldsQuery("create index \"my_idx\" on \"Person\" (\"id\", \"name\")").setSchema(schema), false)
                .getAll();

        node.context().query().querySqlFields(
            new SqlFieldsQuery("alter table \"Person\" add column (\"age\" int, \"city\" char)")
            .setSchema(schema), false).getAll();

        node.context().query().querySqlFields(
            new SqlFieldsQuery("alter table \"Person\" drop column \"city\"").setSchema(schema), false)
            .getAll();
    }

    /**
     * Check that dynamically created schema objects are in place.
     * @param node Node.
     * @param cacheName Cache name.
     */
    private void checkDynamicSchemaChanges(IgniteEx node, String cacheName) {
        assertEquals(1, indexCnt(node, cacheName));

        assertEquals(3, colsCnt(node, cacheName));
    }

    /**
     *
     */
    protected static class Person implements Serializable {
        /** */
        private static final long serialVersionUID = 0L;

        /** */
        @SuppressWarnings("unused")
        private Person() {
            // No-op.
        }

        /** */
        public Person(int id) {
            this.id = id;
        }

        /** */
        @QuerySqlField
        protected int id;

        /** */
        @QuerySqlField
        protected String name;

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false;

            IgnitePersistentStoreSchemaLoadTest.Person person = (IgnitePersistentStoreSchemaLoadTest.Person)o;

            return id == person.id && (name != null ? name.equals(person.name) : person.name == null);

        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            int res = id;

            res = 31 * res + (name != null ? name.hashCode() : 0);

            return res;
        }
    }
}
