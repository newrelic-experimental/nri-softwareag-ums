package com.newrelic.nri.softwareag.ums.metrics;


public class DeltaMetric extends NumericMetric {

	public DeltaMetric(String name, Number value) {
		super(name, value);
	}

	@Override
	public SourceType getSourceType() {
		return SourceType.DELTA;
	}

	@Override
	public String toString() {
		return "DeltaMetric [name=" + getName() + ", value=" + getValue() + "]";
	}
}
