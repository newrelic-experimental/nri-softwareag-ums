package com.newrelic.nri.softwareag.ums.metrics;

public interface Metric {

	public SourceType getSourceType();

	public String getName();

	public Object getValue();

}
