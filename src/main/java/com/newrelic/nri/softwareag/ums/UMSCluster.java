package com.newrelic.nri.softwareag.ums;


import com.pcbsys.nirvana.client.nChannel; 
import com.pcbsys.nirvana.client.nChannelAttributes;
import com.pcbsys.nirvana.client.nChannelNotFoundException;
import com.pcbsys.nirvana.client.nConsumeEvent;
import com.pcbsys.nirvana.client.nSessionAttributes;
import com.pcbsys.nirvana.client.nSessionNotConnectedException;
import com.pcbsys.nirvana.nAdminAPI.nClusterEventListener;
import com.pcbsys.nirvana.nAdminAPI.nClusterNode;
import com.pcbsys.nirvana.nAdminAPI.nClusterStatus;
import com.pcbsys.nirvana.nAdminAPI.nClusterStatusEntry;
import com.pcbsys.nirvana.nAdminAPI.nNode;
import com.pcbsys.nirvana.nAdminAPI.nRealmNode;
import java.util.Date;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class UMSCluster implements nClusterEventListener, Observer {

 // The realm node to connect to
 private nRealmNode myRealm;


 /**
  * Construct the cluster watch object using the RNAME of the realm to connect to
  *
  * @param args arg[0] should be the RNAME
  */
 public UMSCluster(String[] args) throws Exception {
   myRealm = new nRealmNode(new nSessionAttributes(args[0]));
   myRealm.waitForEntireNameSpace(40000);
   if (!myRealm.isClustered()) {
     System.out.println("Node is not part of a cluster");
     System.exit(1);
   }
   // add observer to the realm to get new cluster notifications
   nClusterNode cNode = myRealm.getCluster();
   Vector myRealms = new Vector();
   if (cNode != null) {
     report("Starting cluster monitor....");
     cNode.addListener(this);
     cNode.addObserver(this);

     nChannelAttributes ca = new nChannelAttributes("ClusterMonitor", 10, 0, nChannelAttributes.RELIABLE_TYPE);
     try {
       myRealm.getSession().findChannel(ca);
     } catch (nChannelNotFoundException e) {
       myRealm.getSession().createChannel(ca);
     }

     Enumeration enm = cNode.getNodes();
     while (enm.hasMoreElements()) {
       nNode node = (nNode) enm.nextElement();
       if (node instanceof nRealmNode) {
         myRealms.add(new RealmMonitor((nRealmNode) node));
       }
     }
     while (true) {
       Thread.sleep(31000);
       StringBuilder usage = new StringBuilder();
       for (int x = 0; x < myRealms.size(); x++) {
         RealmMonitor mon = (RealmMonitor) myRealms.elementAt(x);
         usage.append(mon.getRealmName()).append(" Mem ")
             .append(((int) ((mon.getNode().getFreeMemory() * 100) / (mon.getNode().getTotalMemory()))))
             .append("% free, ");
       }
       report("Monitoring...." + usage.toString());
       for (int x = 0; x < myRealms.size(); x++) {
         RealmMonitor mon = (RealmMonitor) myRealms.elementAt(x);
         for (int y = x; y < myRealms.size(); y++) {
           RealmMonitor mon2 = (RealmMonitor) myRealms.elementAt(y);
           if (mon.getLastEID() != mon2.getLastEID()) {
             StringBuilder buf = new StringBuilder();
             buf.append(mon.getRealmName()).append(" doesn't match ").append(mon2.getRealmName()).append(" ")
                 .append(mon.getLastEID()).append(" != ").append(mon2.getLastEID());
             report(buf.toString());
           }
         }
       }
       Vector tmp = myRealm.getCluster().getClusterConnectionStatus();
       for (int x = 0; x < tmp.size(); x++) {
         nClusterStatus cs = (nClusterStatus) tmp.elementAt(x);
         if (!(cs.getState().equalsIgnoreCase("Master") || cs.getState().equalsIgnoreCase("Slave"))) {
           report("Cluster has not fully formed..." + cs.getName() + " " + cs.getState());
         }
       }
     }
   }
 }

 public static void main(String[] args) {
   try {
     new UMSCluster(args);
   } catch (Exception ex) {
     ex.printStackTrace();
   }
 }

 /**
  * New member added to the cluster
  */
 public void memberAdded(nRealmNode node) {
   report("Member added to cluster " + node.getName());
 }

 /**
  * Member deleted from the cluster
  */
 public void memberDeleted(nRealmNode node) {
   report("Member deleted from cluster " + node.getName());
 }

 /**
  * quorum reached, i.e. master realm has been elected
  */
 public void quorumReached(nRealmNode masterNode) {
   report("quorum reach, master - " + masterNode.getName());
 }

 /**
  * quorum lost, i.e. master realm has been lost, no current master
  */
 public void quorumLost() {
   report("!!! Quorum lost !!!");
 }

 /**
  * Cluster status updatet
  */
 public void statusUpdate(nClusterStatus update) {
   report("Cluster status changed ");
   report(update.getName() + " elected Master = " + update.getElectedMaster());
   for (int x = 0; x < update.size(); x++) {
     nClusterStatusEntry entry = update.getStatus(x);
     report("*******" + entry.getName() + " Online = " + entry.isOnline());
   }
   report("");
 }

 /**
  * Cluster state change
  */
 public void stateChange(nRealmNode node, String newState) {
   report("Node " + node.getName() + " changed state to " + newState);
 }

 /**
  * Cluster log event
  */
 public void report(String source, String message) {
   report("CLUSTER>> " + source + "> " + message);
 }

 /**
  * Update from the realm node that a cluster has been created
  */
 public void update(Observable o, Object arg) {
   if (arg instanceof nClusterNode) {
     report("New cluster formed, name = " + ((nClusterNode) arg).getName());
     ((nClusterNode) arg).addListener(this);
   }
 }

 public synchronized void report(String tmp) {
   Date dt = new Date();
   System.out.println(dt.toString() + " > " + tmp);
 }

 private class RealmMonitor extends Thread {

   private nRealmNode myRealm;
   private nChannel myMonitor;
   private String myName;
   private long myLastEID;

   public RealmMonitor(nRealmNode node) {
     myRealm = node;
     try {
       myName = myRealm.getSession().getServerRealmName();
       super.setName(myName);
     } catch (nSessionNotConnectedException e) {

     }
     nChannelAttributes ca = new nChannelAttributes();
     try {
       ca.setName("ClusterMonitor");
       myMonitor = myRealm.getSession().findChannel(ca);
     } catch (Exception e) {
       e.printStackTrace();
       return;
     }
     setDaemon(true);
     start();
   }

   public long getLastEID() {
     try {
       myLastEID = myMonitor.getLastEID();
     } catch (Exception e) {
       e.printStackTrace();
     }
     return myLastEID;
   }

   public nRealmNode getNode() {
     return myRealm;
   }

   public String getRealmName() {
     return myName;
   }

   public void run() {

     while (true) {
       try {
         Thread.sleep(60000);
       } catch (InterruptedException e) {
       }
       nConsumeEvent ce = new nConsumeEvent("1", myName.getBytes());
       try {
         myMonitor.publish(ce);
         try {
           Thread.sleep(2000);
         } catch (InterruptedException e) {
         }
       } catch (Exception e) {
       }
     }
   }
 }
}
