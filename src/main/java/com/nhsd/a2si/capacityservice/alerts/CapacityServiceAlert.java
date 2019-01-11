package com.nhsd.a2si.capacityservice.alerts;

/**
 * Enumeration to encapsulate alert codes. These codes can be searched for in Splunk.
 * 
 * @author jonathanpearce
 *
 */
public enum CapacityServiceAlert 
{
	ALERT_001 ("ALERT_001", "Failure to store metric header records", false),
	ALERT_002 ("ALERT_002", "Failure to store metric detail records for services with wait times", false),
	ALERT_003 ("ALERT_003", "Failure to store metric detail records for services with no wait times", false);
	
	private String alertCode;
	private String alertMessage;
	private boolean fatal;

	CapacityServiceAlert(String alertCode,
			String alertMessage,
			boolean fatal)
	{
		this.alertCode = alertCode;
		this.alertMessage = alertMessage;
		this.fatal = fatal;
	}
	
	public String getAlertCode() 
	{
		return alertCode;
	}

	public void setAlertCode(String alertCode) 
	{
		this.alertCode = alertCode;
	}

	public String getAlertMessage() 
	{
		return alertMessage;
	}

	public void setAlertMessage(String alertMessage) 
	{
		this.alertMessage = alertMessage;
	}
	
	public boolean isFatal() 
	{
		return fatal;
	}

	public void setFatal(boolean fatal) 
	{
		this.fatal = fatal;
	}

}
