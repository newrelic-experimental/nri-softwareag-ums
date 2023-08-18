package com.newrelic.nri.softwareag.ums.metrics;


public abstract class NumericMetric implements Metric {
	String name;
	Number value;

	public NumericMetric(String name, Number value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Number getValue() {
		return value;
	}
}
