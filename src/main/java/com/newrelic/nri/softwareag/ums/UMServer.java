package com.newrelic.nri.softwareag.ums;

import com.pcbsys.nirvana.nAdminAPI.nClusterNode;

public class UMServer {
    private String host;
    private int port = 9000; // default port
    private String name;
    private boolean valid = true;
	private String username;
	private String password;
	private String aeskey;
	private boolean encpwd=false;
	

    public UMServer(String name, String host, int port, String username, String password, String aeskey, boolean encpwd) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.aeskey = aeskey;
        this.encpwd = encpwd;


    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUMSURL() {
        if (host != null) {
            return "nsp://" + host + ":" + port;
        }
        return null;
    }



    public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public boolean getEncPwd() {
		return encpwd;
	}

	public String getAesKey() {
		// TODO Auto-generated method stub
		return aeskey;
	}
}
