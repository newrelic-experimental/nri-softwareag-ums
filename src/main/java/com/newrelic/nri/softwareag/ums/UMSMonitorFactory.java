package com.newrelic.nri.softwareag.ums;

import java.util.Map;

public class UMSMonitorFactory {

	private static final Number DEFAULT_PORT = 9000;
	private static String MANGLED_PREFIX = "-UMS-";

	public Object createAgent(Map<String, Object> properties) throws Exception {

		String name = (String) properties.get("name");
		String host = (String) properties.get("host");
		Number port = (Number) properties.get("port");
		if (port == null) {
			port = DEFAULT_PORT;
		}
		String username = (String) properties.get("username");
		String password = (String) properties.get("password");
		String aeskey = (String) properties.get("aeskey");
		Boolean encryptPassword = (Boolean) properties.get("encryptPassword");

		if (encryptPassword != null && encryptPassword && aeskey != null )
		{
			try {
				password= AESCrypto.decrypt(password,aeskey);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else {
			encryptPassword = false;
		}

		Boolean isCluster = (Boolean) properties.get("isCluster");

		if (null == isCluster)
		{
			isCluster = true; // default is true
		}

		Object agent = null;
		UMServer ems = new UMServer(name, host, port.intValue(), username, password, aeskey, encryptPassword);

		if(isCluster) {
				 agent = new UMSClusterMonitor(ems);
		}
		else
			 agent = new UMSMonitor(ems);

		return agent;
	}


}
