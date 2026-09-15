package com.interbank.common.repository;

public interface ProcessingMetricsProjection {
    Double getAvgDuration();
    Long getMinDuration();
    Long getMaxDuration();
    Long getCount();
}

