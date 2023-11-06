package com.newrelic.nri.softwareag.ums;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.newrelic.nri.softwareag.ums.metrics.AttributeMetric;
import com.newrelic.nri.softwareag.ums.metrics.GaugeMetric;
import com.newrelic.nri.softwareag.ums.metrics.Metric;
import com.pcbsys.nirvana.client.nIllegalArgumentException;
import com.pcbsys.nirvana.client.nSessionAttributes;
import com.pcbsys.nirvana.nAdminAPI.nBaseAdminException;
import com.pcbsys.nirvana.nAdminAPI.nContainer;
import com.pcbsys.nirvana.nAdminAPI.nLeafNode;
import com.pcbsys.nirvana.nAdminAPI.nNode;
import com.pcbsys.nirvana.nAdminAPI.nRealmNode;


public class UMSMonitor {
    private UMServer umeServer;
    private nRealmNode realmNode;
    private List<nLeafNode> channels = new ArrayList<>();
    private List<nLeafNode> queues = new ArrayList<>();


    public UMSMonitor(UMServer ems) {
        umeServer = ems;
    }

    private void connect(UMServer m) {
        String host = m.getUMSURL();
        String username=m.getUsername();
        String password=m.getPassword();

        try {
            realmNode = new nRealmNode(new nSessionAttributes(host),username,password);
          //  System.out.println();
          //  System.out.println("Connected to Realm : " + realmNode.getName());
          //  System.out.println();
            waitForRealmNodeNameSpace (realmNode);
            scanRealmForChannelsAndQueues(realmNode.getNodes());
        } catch (nBaseAdminException | nIllegalArgumentException e) {
            Utils.reportError("Failed to connect", e);
        }
    }
    public void connectRealm(nRealmNode rNode) {

        realmNode = rNode;
        //  System.out.println();
        //  System.out.println("Connected to Realm : " + realmNode.getName());
        //  System.out.println();
		waitForRealmNodeNameSpace (realmNode);
		scanRealmForChannelsAndQueues(realmNode.getNodes());
    }
    private void waitForRealmNodeNameSpace (nRealmNode realmNode) {
       // realmNode.waitForEntireNameSpace();
        try {
          Thread.sleep(realmNode.getUpdateInterval());
        } catch (InterruptedException e) {
          System.out.println(""+e);
        }
      }
    private void scanRealmForChannelsAndQueues(@SuppressWarnings("rawtypes") final Enumeration realmNamespaceNodes) {
       // System.out.println("1");
    	while (realmNamespaceNodes.hasMoreElements()) {
            final  nNode child = (nNode) realmNamespaceNodes.nextElement();
            //System.out.println("2");
            if (child instanceof nLeafNode) {
                final nLeafNode leafNode = (nLeafNode) child;
                //System.out.println("3");
                if (leafNode.isChannel()) {
                    channels.add(leafNode);
                } else if (leafNode.isQueue()) {
                    queues.add(leafNode);
                }
            } else if (child instanceof nContainer) {
            	  // System.out.println("4");
                scanRealmForChannelsAndQueues(((nContainer) child).getNodes());
            }
        }
    }

    public void populateMetrics(JSONMetricReporter metricReporter) throws Exception {
        if (umeServer == null) {
            Utils.reportError("UMEServer instance is null");
            return;
        }

        connect(umeServer);

        if (realmNode != null) {
            getChannelStats(metricReporter);
            getQueueStats(metricReporter);

        } else {
            Utils.reportError("Not connected to " + umeServer.getHost());
        }
    }

    private void addGaugeMetric(List<Metric> metricList, String name, Number n) {
        if (metricList == null || name == null || n == null) {
            Utils.reportError("Input arguments are null");
            return;
        }

        Number m = (n instanceof Long) ? new Float(n.floatValue()) : n;
        metricList.add(new GaugeMetric(name, m));
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
                for (nLeafNode c : channelInfos) {
                    metricList.clear();
                    String channelName = c.getName().trim();
                    metricList.add(new AttributeMetric("UM Server", emsServerName));
                    metricList.add(new AttributeMetric("Channel Name", channelName));
                    addGenericMeritcs(c,metricList);
					/*
					 * try {
					 *
					 * Thread.sleep(5000); } catch (InterruptedException e) { // TODO Auto-generated
					 * catch block e.printStackTrace(); }
					 */
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
                for (nLeafNode c : queueInfos) {
                    metricList.clear();
                    String queueName = c.getName().trim();
                    metricList.add(new AttributeMetric("UM Server", emsServerName));
                    metricList.add(new AttributeMetric("Queue Name", queueName));
                    addGenericMeritcs(c,metricList);
					/*
					 * try {
					 *
					 * Thread.sleep(5000); } catch (InterruptedException e) { // TODO Auto-generated
					 * catch block e.printStackTrace(); }
					 */
                    metricReporter.report("Queue Metrics", StatType.Queue, metricList);
                }
            }
        } catch (Exception e) {
            Utils.reportError("Exception occurred", e);
        }
    }

    private static void printLeafNode(StringBuilder displayString, nLeafNode oneLeaf) {
            displayString.append(oneLeaf.getAbsolutePath())
                         .append(" | ")
                         .append(oneLeaf.getCurrentNumberOfEvents())
                         .append(" | ")
                         .append(oneLeaf.getTotalPublished())
                         .append(" | ")
                         .append(oneLeaf.getTotalConsumed())
                         .append(" | ")
                         .append(oneLeaf.getMemoryUsage())
                         .append(" | ")
                         .append(oneLeaf.getTotalNoOfConnections())
                         .append("\n");

        }

    protected void addGenericMeritcs(nLeafNode c, List<Metric> metricList) {

    //	 StringBuilder displayString = new StringBuilder();
    //	 printLeafNode(displayString,c);
    //	System.out.println(displayString);

    	metricList.add(new AttributeMetric("AbsolutePath", c.getAbsolutePath()));


       // Events

    	addGaugeMetric(metricList, "Current Number Of Events", c.getCurrentNumberOfEvents());
        addGaugeMetric(metricList, "Used Space", c.getUsedSpace());
        addGaugeMetric(metricList, "Memory Usage", c.getMemoryUsage());
        addGaugeMetric(metricList, "Cache Hit Ratio", c.getCacheHitRatio());
        addGaugeMetric(metricList, "Free Percentage", c.getPercentageFreeInStore());


        //Totals
        addGaugeMetric(metricList, "Published Total", c.getTotalPublished());
        addGaugeMetric(metricList, "Consumed Total", c.getTotalConsumed());
        addGaugeMetric(metricList, "Connection Total", c.getTotalNoOfConnections());
        addGaugeMetric(metricList, "Connection Current", c.getCurrentNoOfConnections());

        //Rates
        addGaugeMetric(metricList, "Published Rate", c.getPublishRate());
        addGaugeMetric(metricList, "Consumed Rate", c.getConsumedRate());
        addGaugeMetric(metricList, "Connection Rate", c.getConnectionRate());
        addGaugeMetric(metricList, "Fanout Time (ms)", c.getFanoutTime());






    }
}
