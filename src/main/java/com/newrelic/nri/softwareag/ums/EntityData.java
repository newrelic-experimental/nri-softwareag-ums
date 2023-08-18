package com.newrelic.nri.softwareag.ums;

public class EntityData {

	private String name;
	private StatType statType;

	private UMSMetrics metrics;

	public EntityData(String n, StatType st, UMSMetrics m) {
		name = n;
		statType = st;
		metrics = m;
	}

	public String getName() {
		return name;
	}

	public StatType getStatType() {
		return statType;
	}

	public UMSMetrics getMetrics() {
		return metrics;
	}


}
