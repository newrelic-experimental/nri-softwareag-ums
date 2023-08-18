package com.newrelic.nri.softwareag.ums;

import java.util.HashMap;
import java.util.Map;

public class UMSMetrics {

	Map<String, Object> attributes = new HashMap<>();
	Map<String, Number> metrics = new HashMap<>();

	protected void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	protected void addMetric(String name, Number value) {
		metrics.put(name, value);
	}


	protected Map<String, Object> getAttributes() {
		return attributes;
	}

	protected Map<String,Number> getMetrics() {
		return metrics;
	}


}
