package com.newrelic.nri.softwareag.ums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newrelic.nri.softwareag.ums.metrics.AttributeMetric;
import com.newrelic.nri.softwareag.ums.metrics.Metric;
import com.newrelic.nri.softwareag.ums.metrics.NumericMetric;
import com.newrelic.nri.softwareag.ums.metrics.SourceType;

public class JSONMetricReporter {

	private static final String NAME = "SoftwareagUME";
	private static final String PROTOCOL_VERSION = Integer.toString(2);
	private static final String INTEGRATION_VERSION = "1.0";

	private Map<String, List<Metric>> values = new HashMap<>();
	private List<EntityData> data = new ArrayList<>();

	public void report(String metricSetName, StatType statType, List<Metric> metricSet) {
		values.put(metricSetName, metricSet);
		UMSMetrics emsMetrics = new UMSMetrics();
		for (Metric metric : metricSet) {
			SourceType source = metric.getSourceType();
			switch (source) {
			case ATTRIBUTE:
				AttributeMetric aMetric = (AttributeMetric) metric;
				emsMetrics.addAttribute(aMetric.getName(), aMetric.getValue());
				break;
			case RATE:
			case DELTA:
			case GAUGE:
				NumericMetric nMetric = (NumericMetric) metric;
				emsMetrics.addMetric(nMetric.getName(), nMetric.getValue());
			}
		}
		EntityData entityData = new EntityData(metricSetName, statType, emsMetrics);
		data.add(entityData);
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("name", NAME);
		json.put("protocol_version", PROTOCOL_VERSION);
		json.put("integration_version", INTEGRATION_VERSION);

		JSONArray dataArray = new JSONArray();

		for (EntityData emsData : data) {
			JSONObject json2 = new JSONObject();
			JSONObject entity = new JSONObject();
			entity.put("name", emsData.getName());
			entity.put("type", emsData.getStatType().name());

			json2.put("entity", entity);
			JSONArray metrics = new JSONArray();
			UMSMetrics emsMetrics = emsData.getMetrics();
			JSONObject event = new JSONObject();
			event.put("event_type", emsData.getStatType().getEventType());
			Map<String, Object> attributes = emsMetrics.getAttributes();
			Set<String> keys = attributes.keySet();
			for (String key : keys) {
				Object value = attributes.get(key);
				event.put(key, value.toString());
			}
			Map<String, Number> theMetrics = emsMetrics.getMetrics();
			keys = theMetrics.keySet();
			for (String key : keys) {
				Number n = theMetrics.get(key);
				event.put(key, n);
			}
			metrics.add(event);
			json2.put("metrics", metrics);
			json2.put("events", new JSONArray());
			json2.put("inventory", new JSONObject());
			dataArray.add(json2);
		}
		json.put("data", dataArray);
		return json;
	}
}
