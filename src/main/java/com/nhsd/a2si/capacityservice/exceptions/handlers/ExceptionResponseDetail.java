package com.nhsd.a2si.capacityservice.exceptions.handlers;

public class ExceptionResponseDetail 
{
	
    private String serviceIdentifier;
    private String validationCode;
	private String detailMessage;
    
    public ExceptionResponseDetail(String validationCode,
    		String detailMessage,
    		String serviceIdentifier)
    {
    	this.detailMessage = detailMessage;
    	this.serviceIdentifier = serviceIdentifier;
    	this.validationCode = validationCode;
    }
    
    public String getValidationCode() 
    {
		return validationCode;
	}
    
	public void setValidationCode(String validationCode) 
	{
		this.validationCode = validationCode;
	}
	
	public String getDetailMessage() 
	{
		return detailMessage;
	}
	
	public void setDetailMessage(String detailMessage) 
	{
		this.detailMessage = detailMessage;
	}
	
	public String getServiceIdentifier() 
	{
		return serviceIdentifier;
	}
	
	public void setServiceIdentifier(String serviceIdentifier) 
	{
		this.serviceIdentifier = serviceIdentifier;
	}

}
