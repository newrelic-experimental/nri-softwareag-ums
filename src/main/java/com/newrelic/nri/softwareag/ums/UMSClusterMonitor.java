package com.newrelic.nri.softwareag.ums;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.newrelic.nri.softwareag.ums.metrics.AttributeMetric;
import com.newrelic.nri.softwareag.ums.metrics.GaugeMetric;
import com.newrelic.nri.softwareag.ums.metrics.Metric;
import com.pcbsys.nirvana.client.nIllegalArgumentException;
import com.pcbsys.nirvana.client.nSessionAttributes;
import com.pcbsys.nirvana.nAdminAPI.nBaseAdminException;
import com.pcbsys.nirvana.nAdminAPI.nClusterNode;
import com.pcbsys.nirvana.nAdminAPI.nClusterStatus;
import com.pcbsys.nirvana.nAdminAPI.nClusterStatusEntry;
import com.pcbsys.nirvana.nAdminAPI.nLeafNode;
import com.pcbsys.nirvana.nAdminAPI.nRealmNode;

public class UMSClusterMonitor {
    private UMServer umeServer;
    private nRealmNode realmNode;
    private nClusterNode clusterNode = null;
    private List<nLeafNode> channels = new ArrayList<>();
    private List<nLeafNode> queues = new ArrayList<>();
    private List<nRealmNode> realms = new ArrayList<>();

    public UMSClusterMonitor(UMServer ems) {
        umeServer = ems;
    }

    private void connect(UMServer server) {
        if (server == null) {
            Utils.reportError("UMServer instance is null");
            return;
        }
        
        String host = server.getUMSURL();
        String username = server.getUsername();
        String password = server.getPassword();

        try {
            realmNode = new nRealmNode(new nSessionAttributes(host), username, password);
        } catch (nBaseAdminException | nIllegalArgumentException e) {
            Utils.reportError("Failed to connect", e);
        }
    }

    private void waitForRealmNodeNameSpace(nRealmNode realmNode) {
        if (realmNode == null) {
            Utils.reportError("RealmNode is null");
            return;
        }
        try {
            Thread.sleep(realmNode.getUpdateInterval());
        } catch (InterruptedException e) {
            Utils.reportError("Error during sleep", e);
        }
    }

 
    public void populateMetrics(JSONMetricReporter metricReporter) {
        if (umeServer == null) {
            Utils.reportError("UMEServer instance is null");
            return;
        }

        connect(umeServer);

        if (realms != null) {
            this.getClusterStats(metricReporter);
            for (nRealmNode realm : realms) {
                UMSMonitor realmMonitor = new UMSMonitor(umeServer);
                realmMonitor.connectRealm(realm);
                realmMonitor.getChannelStats(metricReporter);
                realmMonitor.getQueueStats(metricReporter);

            }
        }
    }

    private void addGaugeMetric(List<Metric> metricList, String name, Number value) {
        if (metricList == null || name == null || value == null) {
            Utils.reportError("Input arguments are null");
            return;
        }

        Number convertedValue = (value instanceof Long) ? new Float(value.floatValue()) : value;
        metricList.add(new GaugeMetric(name, convertedValue));
    }

    protected void getClusterStats(JSONMetricReporter metricReporter) {
        try {
            if (realmNode == null) {
                Utils.reportError("RealmNode is null");
                return;
            }

            List<Metric> metricList = new ArrayList<>();
            waitForRealmNodeNameSpace(realmNode);
            this.clusterNode = this.realmNode.getCluster();
            metricList.clear();

            if (this.clusterNode != null) {
              String clusterName = this.clusterNode.getName().trim();
                metricList.add(new AttributeMetric("Cluster Name", clusterName));

                Enumeration<?> nodes = this.clusterNode.getNodes();
  

                Iterator<?> connectionStatusIterator = this.clusterNode.getClusterConnectionStatus().iterator();

                while (nodes.hasMoreElements()) {
                    nRealmNode realm = (nRealmNode) nodes.nextElement();
                    realms.add(realm);
                }

                while (connectionStatusIterator.hasNext()) {
                    Object connectionStatusObject = connectionStatusIterator.next();
                    nClusterStatus clusterStatus = (nClusterStatus) connectionStatusObject;
                   // System.out.println(clusterStatus.getName());

                    metricList.add(new AttributeMetric("Member", clusterStatus.getName()));
                  //  System.out.println(clusterStatus.getState());
                    metricList.add(new AttributeMetric("State", clusterStatus.getState()));
                    metricList.add(new AttributeMetric("Master", clusterStatus.getElectedMaster()));

                    nClusterStatusEntry statusEntry = clusterStatus.getStatus(clusterStatus.getName());
                    if (statusEntry != null) {
                      //  System.out.println();
                        if (statusEntry.isOnline()) {
                         //   System.out.println("online");
                            metricList.add(new AttributeMetric("Status", "online"));
                        } else {
                            metricList.add(new AttributeMetric("Status", "offline"));
                        }

                        metricList.add(new AttributeMetric("Broadcast Time", statusEntry.getBroadcastTime()));
                        metricList.add(new AttributeMetric("Comms Queue Size", statusEntry.getCommsQueueSize()));
                        metricList.add(new AttributeMetric("Queue Size", statusEntry.getQueueSize()));
                        metricList.add(new AttributeMetric("Response Time", statusEntry.getResponseTime()));
                        metricList.add(new AttributeMetric("Client Request Size", statusEntry.getClientRequestSize()));
                    }
                    metricReporter.report("Cluster Metrics", StatType.Cluster, metricList);
                    metricList.clear();
                    metricList.add(new AttributeMetric("Cluster Name", clusterName));
                   
                }
            }
        } catch (Exception e) {
            Utils.reportError("Exception occurred", e);
        }
    }

    protected void getChannelStats(JSONMetricReporter metricReporter) {
        try {
            if (realmNode == null) {
                Utils.reportError("RealmNode is null");
                return;
            }

            String emsServerName = realmNode.getName().trim();
            List<nLeafNode> channelInfos = channels;
            List<Metric> metricList = new ArrayList<>();

            if (channelInfos != null) {
                for (nLeafNode channel : channelInfos) {
                    metricList.clear();
                    String channelName = channel.getName().trim();
                    metricList.add(new AttributeMetric("UM Server", emsServerName));
                    metricList.add(new AttributeMetric("Channel Name", channelName));
                    addGenericMetrics(channel, metricList);
                    metricReporter.report("Channel Metrics", StatType.Channel, metricList);
                }
            }
        } catch (Exception e) {
            Utils.reportError("Exception occurred", e);
        }
    }

    protected void getQueueStats(JSONMetricReporter metricReporter) {
        try {
            if (realmNode == null) {
                Utils.reportError("RealmNode is null");
                return;
            }

            String emsServerName = realmNode.getName().trim();
            List<nLeafNode> queueInfos = queues;
            List<Metric> metricList = new ArrayList<>();

            if (queueInfos != null) {
                for (nLeafNode queue : queueInfos) {
                    metricList.clear();
                    String queueName = queue.getName().trim();
                    metricList.add(new AttributeMetric("UM Server", emsServerName));
                    metricList.add(new AttributeMetric("Queue Name", queueName));
                    addGenericMetrics(queue, metricList);
                    metricReporter.report("Queue Metrics", StatType.Queue, metricList);
                }
            }
        } catch (Exception e) {
            Utils.reportError("Exception occurred", e);
        }
    }



    protected void addGenericMetrics(nLeafNode node, List<Metric> metricList) {
        metricList.add(new AttributeMetric("AbsolutePath", node.getAbsolutePath()));

        // Events
        addGaugeMetric(metricList, "Current Number Of Events", node.getCurrentNumberOfEvents());
        addGaugeMetric(metricList, "Used Space", node.getUsedSpace());
        addGaugeMetric(metricList, "Memory Usage", node.getMemoryUsage());
        addGaugeMetric(metricList, "Cache Hit Ratio", node.getCacheHitRatio());
        addGaugeMetric(metricList, "Free Percentage", node.getPercentageFreeInStore());

        // Totals
        addGaugeMetric(metricList, "Published Total", node.getTotalPublished());
        addGaugeMetric(metricList, "Consumed Total", node.getTotalConsumed());
        addGaugeMetric(metricList, "Connection Total", node.getTotalNoOfConnections());
        addGaugeMetric(metricList, "Connection Current", node.getCurrentNoOfConnections());

        // Rates
        addGaugeMetric(metricList, "Published Rate", node.getPublishRate());
        addGaugeMetric(metricList, "Consumed Rate", node.getConsumedRate());
        addGaugeMetric(metricList, "Connection Rate", node.getConnectionRate());
        addGaugeMetric(metricList, "Fanout Time (ms)", node.getFanoutTime());
    }
}
