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
package org.apache.ignite.internal.processors.query.h2.opt;

/**
 * Table statistics class. Used by query optimizer to estimate execution plan cost.
 */
public class TableStatistics {
    /** Total table row count (including primary and backup partitions). */
    private final long totalRowCnt;

    /** Local node row count. */
    private final long localRowCount;

    /**
     * @param totalRowCnt Total table row count (including primary and backup partitions).
     * @param localRowCount Local node row count.
     */
    public TableStatistics(long totalRowCnt, long localRowCount) {
        assert totalRowCnt >= 0 && localRowCount >= 0 : "totalRowCnt=" + totalRowCnt + ", localRowCount=" + localRowCount;

        this.totalRowCnt = totalRowCnt;
        this.localRowCount = localRowCount;
    }

    /**
     * @return Total table row count (including primary and backup partitions).
     */
    public long totalRowCount() {
        return totalRowCnt;
    }

    /**
     * @return Local node row count.
     */
    public long localRowCount() {
        return localRowCount;
    }
}
