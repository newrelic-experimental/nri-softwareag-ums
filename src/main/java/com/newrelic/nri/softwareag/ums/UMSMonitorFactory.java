package com.newrelic.nri.softwareag.ums;

import java.util.Map;

public class UMSMonitorFactory {

	private static final Number DEFAULT_PORT = 9000;
	private static String MANGLED_PREFIX = "-UMS-";

	public UMSMonitor createAgent(Map<String, Object> properties) throws Exception {
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


		UMServer ems = new UMServer(name, host, port.intValue(), username, password, aeskey, encryptPassword);

		UMSMonitor agent = new UMSMonitor(ems);

		return agent;
	}


}
