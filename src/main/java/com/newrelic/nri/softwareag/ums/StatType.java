package com.newrelic.nri.softwareag.ums;

public enum StatType {
	Channel {
        @Override
        public String getEventType() {
            return "UMSChannel";
        }
	},
	Bridge {
        @Override
        public String getEventType() {
            return "EMSBridge";
        }
	},
	Queue {
        @Override
        public String getEventType() {
            return "UMSQueue";
        }
	},
	Route {
        @Override
        public String getEventType() {
            return "EMSRoute";
        }
	},
	Topic {
        @Override
        public String getEventType() {
            return "UMSTopic";
        }
	},
    Server {
        @Override
        public String getEventType() {
            return "EMSServer";
        }
	},
    ChannelDetails {
        @Override
        public String getEventType() {
            return "EMSChannelDetails";
        }
	},
	QueueTotals {
        @Override
        public String getEventType() {
            return "EMSQueueTotals";
        }
	},
	TopicTotals {
        @Override
        public String getEventType() {
            return "EMSTopicTotals";
        }
	},
	RouteTotals {
        @Override
        public String getEventType() {
            return "EMSRouteTotals";
        }
	};

	public abstract String getEventType();
}
