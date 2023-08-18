package com.newrelic.nri.softwareag.ums.metrics;


public class RateMetric extends NumericMetric {

	public RateMetric(String name, Number value) {
		super(name, value);
	}

	@Override
	public SourceType getSourceType() {
		return SourceType.RATE;
	}

	@Override
	public String toString() {
		return "RateMetric [name=" + getName() + ", value=" + getValue() + "]";
	}

}