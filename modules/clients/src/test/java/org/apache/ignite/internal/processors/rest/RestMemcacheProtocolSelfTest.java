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

package org.apache.ignite.internal.processors.rest;

import java.util.Map;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ConnectorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

/**
 * TCP protocol test.
 */
public class RestMemcacheProtocolSelfTest extends GridCommonAbstractTest {
    /** */
    private static final String CACHE_NAME = "cache";

    /** */
    private static final String HOST = "127.0.0.1";

    /** */
    private static final int PORT = 11212;

    /** */
    private TestMemcacheClient client;

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        startGrid();
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        client = client();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        client.shutdown();

        grid().cache(DEFAULT_CACHE_NAME).clear();
        grid().cache(CACHE_NAME).clear();
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setLocalHost(HOST);

        assert cfg.getConnectorConfiguration() == null;

        ConnectorConfiguration clientCfg = new ConnectorConfiguration();

        clientCfg.setPort(PORT);

        cfg.setConnectorConfiguration(clientCfg);

        cfg.setCacheConfiguration(cacheConfiguration(DEFAULT_CACHE_NAME), cacheConfiguration(CACHE_NAME));

        return cfg;
    }

    /**
     * @param cacheName Cache name.
     * @return Cache configuration.
     */
    private static CacheConfiguration<?, ?> cacheConfiguration(@NotNull String cacheName) {
        return defaultCacheConfiguration()
            .setName(cacheName)
            .setStatisticsEnabled(true);
    }

    /**
     * @return Client.
     * @throws IgniteCheckedException In case of error.
     */
    private TestMemcacheClient client() throws IgniteCheckedException {
        return new TestMemcacheClient(HOST, PORT);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testPut() throws Exception {
        assertTrue(client.cachePut(null, "key1", "val1"));
        assertEquals("val1", grid().cache(DEFAULT_CACHE_NAME).get("key1"));

        assertTrue(client.cachePut(CACHE_NAME, "key1", "val1"));
        assertEquals("val1", grid().cache(CACHE_NAME).get("key1"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testGet() throws Exception {
        grid().cache(DEFAULT_CACHE_NAME).put("key", "val");

        Assert.assertEquals("val", client.cacheGet(null, "key"));

        grid().cache(CACHE_NAME).put("key", "val");

        Assert.assertEquals("val", client.cacheGet(CACHE_NAME, "key"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testRemove() throws Exception {
        grid().cache(DEFAULT_CACHE_NAME).put("key", "val");

        assertTrue(client.cacheRemove(null, "key"));
        assertFalse(client.cacheRemove(null, "wrongKey"));

        assertNull(grid().cache(DEFAULT_CACHE_NAME).get("key"));

        grid().cache(CACHE_NAME).put("key", "val");

        assertTrue(client.cacheRemove(CACHE_NAME, "key"));
        assertFalse(client.cacheRemove(CACHE_NAME, "wrongKey"));

        assertNull(grid().cache(CACHE_NAME).get("key"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testAdd() throws Exception {
        assertTrue(client.cacheAdd(null, "key", "val"));
        assertEquals("val", grid().cache(DEFAULT_CACHE_NAME).get("key"));
        assertFalse(client.cacheAdd(null, "key", "newVal"));
        assertEquals("val", grid().cache(DEFAULT_CACHE_NAME).get("key"));

        assertTrue(client.cacheAdd(CACHE_NAME, "key", "val"));
        assertEquals("val", grid().cache(CACHE_NAME).get("key"));
        assertFalse(client.cacheAdd(CACHE_NAME, "key", "newVal"));
        assertEquals("val", grid().cache(CACHE_NAME).get("key"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testReplace() throws Exception {
        assertFalse(client.cacheReplace(null, "key1", "val1"));
        grid().cache(DEFAULT_CACHE_NAME).put("key1", "val1");
        assertTrue(client.cacheReplace(null, "key1", "val2"));

        assertFalse(client.cacheReplace(null, "key2", "val1"));
        grid().cache(DEFAULT_CACHE_NAME).put("key2", "val1");
        assertTrue(client.cacheReplace(null, "key2", "val2"));

        grid().cache(DEFAULT_CACHE_NAME).clear();

        assertFalse(client.cacheReplace(CACHE_NAME, "key1", "val1"));
        grid().cache(CACHE_NAME).put("key1", "val1");
        assertTrue(client.cacheReplace(CACHE_NAME, "key1", "val2"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testMetrics() throws Exception {
        grid().cache(DEFAULT_CACHE_NAME).clearStatistics();
        grid().cache(CACHE_NAME).clearStatistics();

        grid().cache(DEFAULT_CACHE_NAME).put("key1", "val");
        grid().cache(DEFAULT_CACHE_NAME).put("key2", "val");
        grid().cache(DEFAULT_CACHE_NAME).put("key2", "val");

        grid().cache(DEFAULT_CACHE_NAME).get("key1");
        grid().cache(DEFAULT_CACHE_NAME).get("key2");
        grid().cache(DEFAULT_CACHE_NAME).get("key2");

        grid().cache(CACHE_NAME).put("key1", "val");
        grid().cache(CACHE_NAME).put("key2", "val");
        grid().cache(CACHE_NAME).put("key2", "val");

        grid().cache(CACHE_NAME).get("key1");
        grid().cache(CACHE_NAME).get("key2");
        grid().cache(CACHE_NAME).get("key2");

        Map<String, Long> m = client.cacheMetrics(null);

        assertNotNull(m);
        assertEquals(4, m.size());
        assertEquals(3, m.get("reads").longValue());
        assertEquals(3, m.get("writes").longValue());

        m = client.cacheMetrics(CACHE_NAME);

        assertNotNull(m);
        assertEquals(4, m.size());
        assertEquals(3, m.get("reads").longValue());
        assertEquals(3, m.get("writes").longValue());
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testIncrement() throws Exception {
        assertEquals(15L, client().increment("key", 10L, 5L));
        assertEquals(15L, grid().atomicLong("key", 0, true).get());

        assertEquals(18L, client().increment("key", 20L, 3L));
        assertEquals(18L, grid().atomicLong("key", 0, true).get());

        assertEquals(20L, client().increment("key", null, 2L));
        assertEquals(20L, grid().atomicLong("key", 0, true).get());

        assertEquals(15L, client().increment("key1", 10L, 5L));
        assertEquals(15L, grid().atomicLong("key1", 0, true).get());

        assertEquals(18L, client().increment("key1", 20L, 3L));
        assertEquals(18L, grid().atomicLong("key1", 0, true).get());

        assertEquals(20L, client().increment("key1", null, 2L));
        assertEquals(20L, grid().atomicLong("key1", 0, true).get());
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testDecrement() throws Exception {
        assertEquals(15L, client().decrement("key", 20L, 5L));
        assertEquals(15L, grid().atomicLong("key", 0, true).get());

        assertEquals(12L, client().decrement("key", 20L, 3L));
        assertEquals(12L, grid().atomicLong("key", 0, true).get());

        assertEquals(10L, client().decrement("key", null, 2L));
        assertEquals(10L, grid().atomicLong("key", 0, true).get());

        assertEquals(15L, client().decrement("key1", 20L, 5L));
        assertEquals(15L, grid().atomicLong("key1", 0, true).get());

        assertEquals(12L, client().decrement("key1", 20L, 3L));
        assertEquals(12L, grid().atomicLong("key1", 0, true).get());

        assertEquals(10L, client().decrement("key1", null, 2L));
        assertEquals(10L, grid().atomicLong("key1", 0, true).get());
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testAppend() throws Exception {
        assertFalse(client.cacheAppend(null, "wrongKey", "_suffix"));
        assertFalse(client.cacheAppend(CACHE_NAME, "wrongKey", "_suffix"));

        grid().cache(DEFAULT_CACHE_NAME).put("key", "val");
        assertTrue(client.cacheAppend(null, "key", "_suffix"));
        assertEquals("val_suffix", grid().cache(DEFAULT_CACHE_NAME).get("key"));

        grid().cache(CACHE_NAME).put("key", "val");
        assertTrue(client.cacheAppend(CACHE_NAME, "key", "_suffix"));
        assertEquals("val_suffix", grid().cache(CACHE_NAME).get("key"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testPrepend() throws Exception {
        assertFalse(client.cachePrepend(null, "wrongKey", "prefix_"));
        assertFalse(client.cachePrepend(CACHE_NAME, "wrongKey", "prefix_"));

        grid().cache(DEFAULT_CACHE_NAME).put("key", "val");
        assertTrue(client.cachePrepend(null, "key", "prefix_"));
        assertEquals("prefix_val", grid().cache(DEFAULT_CACHE_NAME).get("key"));

        grid().cache(CACHE_NAME).put("key", "val");
        assertTrue(client.cachePrepend(CACHE_NAME, "key", "prefix_"));
        assertEquals("prefix_val", grid().cache(CACHE_NAME).get("key"));
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testVersion() throws Exception {
        assertNotNull(client.version());
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testNoop() throws Exception {
        client.noop();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testQuit() throws Exception {
        client.quit();
    }
}
