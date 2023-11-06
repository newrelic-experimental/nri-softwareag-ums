package com.newrelic.nri.softwareag.ums;

import com.pcbsys.nirvana.client.nIllegalArgumentException;
import com.pcbsys.nirvana.client.nRealmUnreachableException;
import com.pcbsys.nirvana.client.nSessionAttributes;
import com.pcbsys.nirvana.nAdminAPI.nBaseAdminException;
import com.pcbsys.nirvana.nAdminAPI.nClusterNode;
import com.pcbsys.nirvana.nAdminAPI.nClusterSite;
import com.pcbsys.nirvana.nAdminAPI.nClusterStatus;
import com.pcbsys.nirvana.nAdminAPI.nClusterStatusEntry;
import com.pcbsys.nirvana.nAdminAPI.nRealmNode;
import com.softwareag.um.tools.ErrorCodes;
import com.softwareag.um.tools.UMToolCommon;
import com.softwareag.um.tools.UMToolInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class ClusterState implements UMToolInterface {
	private static final String RNAME = "rname";
	private static final String COLUMN_DELIMITER = " | ";
	private static final String WHITESPACE = " ";
	private static final String SERVER_NAME_STR = "| Server Name";
	private static final String RNAMES_STR = "| Rnames";
	private static final String IS_CLUSTERED_STR = "| Is Clustered";
	private static final String SERVER_STATUS_STR = "| Server Status";
	private static final String CLUSTER_STATE_STR = "| Cluster State";
	private static final String BROADCAST_TIME_STR = "| Broadcast Time";
	private static final String CLIENT_REQUEST_SIZE_STR = "| Client Request Size";
	private static final String COMMS_QUEUE_SIZE_STR = "| Comms Queue Size";
	private static final String QUEUE_SIZE_STR = "| Queue Size";
	private static final String RESPONSE_TIME_STR = "| Response Time";
	nClusterNode clusterNode = null;
	nRealmNode realm = null;
	nRealmNode masterNode = null;
	private String rName;
	private Logger logger;

	public static void main(String[] var0)
			throws nIllegalArgumentException, nBaseAdminException, nRealmUnreachableException {
		UMToolCommon.executeTool(new ClusterState(), var0, false);
	}

	public String getToolName() {
		return "ClusterState";
	}

	public String getToolDescription() {
		return this.getToolName() + "Checks the cluster state by a give RNAME , which is part of a cluster";
	}

	public String[] getToolExamples() {
		return new String[]{this.getToolName() + " -rname=nsp://localhost:9000"};
	}

	public String getToolCategory() {
		return "2. Cluster tools";
	}

	public String[] getToolArgumentsRequired() {
		return new String[]{"rname"};
	}

	public String[] getToolArgumentsOptional() {
		return new String[0];
	}

	public String getNormalisedParameter(String var1) {
		return "rname".equalsIgnoreCase(var1) ? "rname" : var1;
	}

	public String getParameterDescription(String var1) {
		return var1.equals("rname") ? "Name of a realm , which is part of a cluster" : "Unknown Parameter";
	}

	public void initialise(Map<String, String> var1, Logger var2) throws Exception {
		this.logger = var2;
		this.rName = (String) var1.remove("rname");
		if (!var1.isEmpty()) {
			byte var3 = 32;
			var2.severe(
					ErrorCodes.getErrorContext(var3, (String) ((Entry) var1.entrySet().iterator().next()).getKey()));
			UMToolCommon.exit(var3);
		}

		this.logger.fine("We have initialised " + this.getToolName() + " with: " + this.rName);
	}

	public void execute() throws Exception {
		int var1 = this.getLengthOfLongest("| Server Name", "| Rnames", "| Is Clustered", "| Server Status",
				"| Cluster State", "| Broadcast Time", "| Client Request Size", "| Comms Queue Size", "| Queue Size",
				"| Response Time");
		ArrayList var2 = new ArrayList();
		ArrayList var3 = new ArrayList();
		ArrayList var4 = new ArrayList();
		ArrayList var5 = new ArrayList();
		ArrayList var6 = new ArrayList();
		ArrayList var7 = new ArrayList();
		ArrayList var8 = new ArrayList();
		ArrayList var9 = new ArrayList();
		ArrayList var10 = new ArrayList();
		ArrayList var11 = new ArrayList();

		try {
			this.realm = UMToolCommon.createRealmNode(new nSessionAttributes(this.rName));
			if (this.realm != null) {
				this.realm.waitForEntireNameSpace(5000L);
				this.clusterNode = this.realm.getCluster();
				if (this.clusterNode != null) {
					this.logger.info("--------------------------------------");
					this.logger.info("Cluster Name: " + this.clusterNode.getName());
					this.logger.info("--------------------------------------");
					Enumeration var12 = this.clusterNode.getNodes();
					this.masterNode = this.clusterNode.getMaster();

					int var16;
					while (var12.hasMoreElements()) {
						nRealmNode var13 = (nRealmNode) var12.nextElement();
						var2.add(var13.getName());
						StringBuilder var14 = new StringBuilder();
						String[] var15 = var13.getRealm().getProtocols();
						var16 = var15.length;

						for (int var17 = 0; var17 < var16; ++var17) {
							String var18 = var15[var17];
							var14.append(var18 + " ");
						}

						var3.add(var14.toString().replace(" ", "\n"));
						var4.add(var13.isClustered());
					}

					this.logger.info("Cluster Statuses: ");
					Iterator var22 = this.clusterNode.getClusterConnectionStatus().iterator();

					while (var22.hasNext()) {
						Object var24 = var22.next();
						nClusterStatus var26 = (nClusterStatus) var24;
						nClusterStatusEntry var28 = var26.getStatus(var26.getName());
						if (var28 != null) {
							var5.add(var28.isOnline() ? "online" : "offline");
							var6.add(var26.getState());
							var7.add(var28.getBroadcastTime());
							var8.add(var28.getClientRequestSize());
							var9.add(var28.getCommsQueueSize());
							var10.add(var28.getQueueSize());
							var11.add(var28.getResponseTime());
						}
					}

					int[] var23 = new int[var2.size() + 1];
					var23[0] = var1;

					int var25;
					for (var25 = 0; var25 < var2.size(); ++var25) {
						var23[var25 + 1] = this.getLengthOfLongest(var2.get(var25), var4.get(var25), var5.get(var25),
								var6.get(var25), var7.get(var25), var8.get(var25), var9.get(var25), var10.get(var25),
								var11.get(var25));
					}

					var25 = 0;

					for (int var27 = 0; var27 < var23.length; ++var27) {
						var25 += var23[var27];
					}

					StringBuilder var29 = new StringBuilder();
					var25 += " | ".length() * var23.length - 1;
					var29.append(this.getFence(var25));
					var29.append("\n");
					var29.append(this.getResultLine(var23, "| Server Name", var2));
					var29.append(this.getResultLine(var23, "| Is Clustered", var4));
					var29.append(this.getResultLine(var23, "| Server Status", var5));
					var29.append(this.getResultLine(var23, "| Cluster State", var6));
					var29.append(this.getResultLine(var23, "| Broadcast Time", var7));
					var29.append(this.getResultLine(var23, "| Client Request Size", var8));
					var29.append(this.getResultLine(var23, "| Comms Queue Size", var9));
					var29.append(this.getResultLine(var23, "| Queue Size", var10));
					var29.append(this.getResultLine(var23, "| Response Time", var11));
					var29.append(this.getFence(var25));
					var29.append("\n");

					for (var16 = 0; var16 < var3.size(); ++var16) {
						var29.append(var2.get(var16) + " Protocols:\n");
						var29.append(var3.get(var16) + "\n");
					}

					this.logger.info(var29.toString());
					this.logger.info("Remote Cluster Connections");
					this.logger.info("--------------------------------------");
					Set var30 = this.clusterNode.getKnownRemoteClusters();
					Iterator var31;
					if (var30.isEmpty()) {
						this.logger.info("No remote cluster connections have been found");
					} else {
						this.logger.info("The following remote cluster connections have been found:");
						var31 = var30.iterator();

						while (var31.hasNext()) {
							nClusterNode var32 = (nClusterNode) var31.next();
							this.logger.info("Cluster name: " + var32.getName());
						}
					}

					this.logger.info("--------------------------------------");
					this.logger.info("Cluster Sites");
					this.logger.info("--------------------------------------");
					var31 = this.clusterNode.getSites();
					if (var31 != null && var31.hasNext()) {
						while (var31.hasNext()) {
							nClusterSite var33 = (nClusterSite) var31.next();
							if (var33 != null) {
								this.logger.info("Site name: " + var33.getName());
								this.logger.info("Is prime: " + var33.isPrime());
							}
						}
					} else {
						this.logger.info("There are no sites in the cluster");
					}

					this.logger.fine(this.getToolName() + " has now executed");
				} else {
					this.logger.info("The provided RNAME is not part of a cluster");
				}
			} else {
				this.logger.info("There is no reachable realm with this RNAME");
			}
		} finally {
			if (this.masterNode != null) {
				this.masterNode.close();
			}

			if (this.realm != null) {
				this.realm.close();
			}

			if (this.clusterNode != null) {
				this.clusterNode.close();
			}

		}

	}

	private String getFence(int var1) {
		StringBuilder var2 = new StringBuilder();

		for (int var3 = 0; var3 < var1; ++var3) {
			var2.append("-");
		}

		return var2.toString();
	}

	private String getResultLine(int[] var1, String var2, ArrayList<String> var3) {
		var3.add(0, var2);
		StringBuilder var4 = new StringBuilder();

		for (int var5 = 0; var5 < var3.size(); ++var5) {
			Object var6 = var3.get(var5);
			var4.append(var6.toString());
			var4.append(this.getWhitespaces(var1[var5] - var6.toString().length()));
			var4.append(" | ");
		}

		var4.append("\n");
		var3.remove(0);
		return var4.toString();
	}

	private String getWhitespaces(int var1) {
		StringBuilder var2 = new StringBuilder();

		for (int var3 = 0; var3 < var1; ++var3) {
			var2.append(" ");
		}

		return var2.toString();
	}

	private int getLengthOfLongest(Object... var1) {
		int var2 = 0;
		Object[] var3 = var1;
		int var4 = var1.length;

		for (int var5 = 0; var5 < var4; ++var5) {
			Object var6 = var3[var5];
			String var7 = var6.toString();
			if (var2 < var7.length()) {
				var2 = var7.length();
			}
		}

		return var2;
	}

	public void close() throws Exception {
		if (this.masterNode != null) {
			this.masterNode.close();
		}

		if (this.realm != null) {
			this.realm.close();
		}

		if (this.clusterNode != null) {
			this.clusterNode.close();
		}

		this.logger.fine("We have now have cleaned up");
	}
}