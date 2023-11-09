

<a href="https://opensource.newrelic.com/oss-category/#new-relic-experimental"><picture><source media="(prefers-color-scheme: dark)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/dark/Experimental.png"><source media="(prefers-color-scheme: light)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"><img alt="New Relic Open Source experimental project banner." src="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"></picture></a>


![GitHub forks](https://img.shields.io/github/forks/newrelic-experimental/nri-softwareag-ums?style=social)
![GitHub stars](https://img.shields.io/github/stars/newrelic-experimental/nri-softwareag-ums?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/newrelic-experimental/nri-softwareag-ums?style=social)

![GitHub all releases](https://img.shields.io/github/downloads/newrelic-experimental/nri-softwareag-ums/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/newrelic-experimental/nri-softwareag-ums)
![GitHub last commit](https://img.shields.io/github/last-commit/newrelic-experimental/nri-softwareag-ums)
![GitHub Release Date](https://img.shields.io/github/release-date/newrelic-experimental/nri-softwareag-ums)


![GitHub issues](https://img.shields.io/github/issues/newrelic-experimental/nri-softwareag-ums)
![GitHub issues closed](https://img.shields.io/github/issues-closed/newrelic-experimental/nri-softwareag-ums)
![GitHub pull requests](https://img.shields.io/github/issues-pr/newrelic-experimental/nri-softwareag-ums)
![GitHub pull requests closed](https://img.shields.io/github/issues-pr-closed/newrelic-experimental/nri-softwareag-ums)



# New Relic Infrastructure Integration for SoftwareAG Universal Messaging Server ( UMS)

The existing implementation reports the metrics for both Channels (including topics)  and Queues of the SoftwareAG Universal Messaging Server. This implementation currently operates with the nps connection protocol and utilizes basic authentication. To ensure successful interaction, the Basic authentication relies on appropriately configured policies within the SoftwareAG Universal Messaging Server's ACL.

## Disclaimer

New Relic has open-sourced this integration to enable monitoring of this technology. This integration is provided AS-IS WITHOUT WARRANTY OR SUPPORT, although you can report issues and contribute to this integration via GitHub.
    
## Requirements

 - New Relic Infrastructure Agent

## Using Mangled Passwords in config files
  
In order to encrypt the password of the user connecting to UMS, you need to run the included utility (encyptPwd.bat/ encyptPwd.sh ) to mangle the password.  Then place the encrypted password in the password field and include the encryptPassword field with a value of true.   

In the extracted release directory run the encyptPwd.sh / encyptPwd.bat script as per the instruction below .  It will output the aesKey and mangled password to use in softwareag-ums-server-config.json.  

  ### Step 1: Generate the 128 bit AES Key [ Example ]
          encyptPwd generateKey
   Generated AES Key: <generated_aes_key>
  ### Step 2: Generate the encypted password [ Example ]
	  encyptPwd encryptPassword <generated_aes_key> <cleartext_password>
   Encrypted Password: <encrypted_password>            
   Success !
### Sample  softwareag-ums-server-config.json
       
	{
	  "instances": [
	    {
	      "name": "SoftwareAG-UMS-Realm",
	      "host": "localhost",
	      "port": 9000,
	      "username": "myuser4",
	      "password": "<cleartext_password>",
	      "encryptPassword": false,
	      "isCluster": false
	    },
	    {
	      "name": "SoftwareAG-UMS-Cluster",
	      "host": "localhost",
	      "port": 9001,
	      "username": "myuser4",
	      "password": "<encrypted_password>",
	      "encryptPassword": true,
	      "aeskey": "<generated_aes_key>"
	    }
	  ]
	}

Edit *softwareag-ums-server-config.json* file to edit the tibco server(s) connection information. 
Edit *softwareag-ums-config.yml* file to edit the path for the json file above. 
Edit *softwareag-ums-definition.yml* file to edit the path for softwareag-ums.jar. 

| Attribute | Description |
| --- | --- |
| name | Name describing the UM Server |
| host | DNS name of IP of the UM Server |
| port | port number of UMS Server, typically 9000 |
| username | username for connecting |
| password | provide password for user  |
| encryptPassword | set to true if password is mangled (encrypted), else set to false | 
| aeskey | (optional) when encryptPassword is false and (mandatory) when encryptPassword is true |
| isCluster | (optional) when isCluster is false it will be traeted as Realm node else it will be treated as cluster (by default) |

## Value

|Metrics | Events | Logs | Traces | Visualization | Automation |
|:-:|:-:|:-:|:-:|:-:|:-:|
|:x:|:white_check_mark:|:x:|:x:|:x:|:x:|

The data collected is reported to New Relic as Custom Events.  The following is a list of events that can be reported.   An event is only recorded if data present for that item so not all events may be reported.

### Event Types
   
| Event Type | Description |
| ---- | ---- |
| **EMSQueue** | Metrics related to an UMS Queue.  Attribute "Queue Name" is the name of the queue |
| **EMSChannel** | Metrics related to an UMS Channel and UMS Topics.  Attribute "Channel Name" is the name of the channel |
| **UMSCluster** | Metrics related to a Cluster.  Attribute "Cluster Name" is the name of the Cluster |



## Installation

1. Extract the Release archive to the local disk
2. Edit *softwareag-ums-server-config.json* according to the Confguration section above.   
3. Either run the install script or follow the instructions below for manual installation.
4. Restart the infrastructure agent

```sh
sudo systemctl stop newrelic-infra

sudo systemctl start newrelic-infra
```


### By Script

## Linux
As the root user, run the following command:

```sh

./install.sh
```
### Windows
```sh

./install.bat
```

### Manual

Install the SoftwareAG UMS monitoring plugin

### Linux
```sh

cp softwareag-ums.jar /var/db/newrelic-infra/custom-integrations

cp softwareag-ums-definition.yml /var/db/newrelic-infra/custom-integrations/

cp softwareag-ums-config.yml /etc/newrelic-infra/integrations.d/

cp softwareag-ums-server-config.json /etc/newrelic-infra/integrations.d/

```
### Windows
```sh

copy softwareag-ums.jar "C:\Program Files\New Relic\newrelic-infra\custom-integrations\"

copy softwareag-ums-definition.yml "C:\Program Files\New Relic\newrelic-infra\custom-integrations\"

copy softwareag-ums-config.yml "C:\Program Files\New Relic\newrelic-infra\integrations.d"

copy softwareag-ums-server-config.json "C:\Program Files\New Relic\newrelic-infra\integrations.d"
```

## Compatibility

* Supported OS: Windows, Linux

## Support

New Relic has open-sourced this project. This project is provided AS-IS WITHOUT WARRANTY OR DEDICATED SUPPORT. Issues and contributions should be reported to the project here on GitHub.

>We encourage you to bring your experiences and questions to the [Explorers Hub](https://discuss.newrelic.com) where our community members collaborate on solutions and new ideas.


## Contributing

We encourage your contributions to improve [Project Name]! Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project. If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

## License

[nri-softwareag-ums] is licensed under the [Apache 2.0](http://apache.org/licenses/LICENSE-2.0.txt) License.

