/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * The options for continuous paging.
 */
public class ContinuousPagingOptions {

    /**
     * The page unit, either bytes or rows.
     */
    public enum PageUnit {
        BYTES(1),
        ROWS(2);

        public final int id;

        PageUnit(int id) {
            this.id = id;
        }
    }

    /**
     * Returns a builder to create a new instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    private final int pageSize;
    private final PageUnit pageUnit;
    private final int maxPages;
    private final int maxPagesPerSecond;

    private ContinuousPagingOptions(int pageSize, PageUnit pageUnit, int maxPages, int maxPagesPerSecond) {
        this.pageSize = pageSize;
        this.pageUnit = pageUnit;
        this.maxPages = maxPages;
        this.maxPagesPerSecond = maxPagesPerSecond;
    }

    /**
     * Returns the page size.
     *
     * @see Builder#withPageSize(int, PageUnit)
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns the page unit.
     *
     * @see Builder#withPageSize(int, PageUnit)
     */
    public PageUnit getPageUnit() {
        return pageUnit;
    }

    /**
     * Returns the maximum number of pages.
     *
     * @see Builder#withMaxPages(int)
     */
    public int getMaxPages() {
        return maxPages;
    }

    /**
     * Returns the maximum pages per second.
     *
     * @see Builder#withMaxPagesPerSecond(int)
     */
    public int getMaxPagesPerSecond() {
        return maxPagesPerSecond;
    }

    @Override
    public String toString() {
        return String.format("continuous-paging-options=%d %s,%d,%d", pageSize, pageUnit.name(), maxPages, maxPagesPerSecond);
    }

    /**
     * A helper to create continuous paging options.
     */
    public static class Builder {
        private int pageSize = 5000;
        private PageUnit pageUnit = PageUnit.ROWS;
        private int maxPages;
        private int maxPagesPerSecond;

        /**
         * Sets the size of the page, in the given unit.
         * <p>
         * If this method is not called, the page size defaults to 5000 rows.
         */
        public Builder withPageSize(int pageSize, PageUnit pageUnit) {
            this.pageSize = pageSize;
            this.pageUnit = pageUnit;
            return this;
        }

        /**
         * Sets the maximum number of pages to retrieve.
         * <p>
         * If this method is not called, the maximum defaults to 0, which means retrieve all pages available.
         */
        public Builder withMaxPages(int maxPages) {
            this.maxPages = maxPages;
            return this;
        }

        /**
         * Sets the maximum number of pages per second.
         * <p>
         * If this method is not called, the rate is set to zero to indicate no limit.
         */
        public Builder withMaxPagesPerSecond(int maxPagesPerSecond) {
            this.maxPagesPerSecond = maxPagesPerSecond;
            return this;
        }

        /**
         * Returns the options specified by this builder.
         */
        public ContinuousPagingOptions build() {
            return new ContinuousPagingOptions(pageSize, pageUnit, maxPages, maxPagesPerSecond);
        }
    }


}
