package com.newrelic.nri.softwareag.ums.metrics;

public class GaugeMetric extends NumericMetric {

	public GaugeMetric(String name, Number value) {
		super(name, value);
	}

	@Override
	public SourceType getSourceType() {
		return SourceType.GAUGE;
	}

	@Override
	public String toString() {
		return "GaugeMetric [name=" + getName() + ", value=" + getValue() + "]";
	}

}