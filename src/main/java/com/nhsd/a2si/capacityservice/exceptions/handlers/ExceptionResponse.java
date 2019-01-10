package com.nhsd.a2si.capacityservice.exceptions.handlers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExceptionResponse {

    private String date;
    private String message;
    private String detail;
    private List<String> details = new ArrayList<String>();
    private List<ExceptionResponseDetail> exceptionResponseDetail = new ArrayList<ExceptionResponseDetail>();

	public ExceptionResponse(String date, String message, String detail) {
        this.date = date;
        this.message = message;
        this.detail = detail;
    }
    
    public ExceptionResponse(String date, String message, List<ExceptionResponseDetail> erd) {
        this.date = date;
        this.message = message;
        this.exceptionResponseDetail.addAll(erd);
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    @JsonInclude(Include.NON_NULL)
    public String getDetail() {
        return detail;
    }
    
    @JsonInclude(Include.NON_EMPTY)
    public List<String> getdetails()
    {
    	return this.details;
    }
    
    @JsonProperty("details")
    public List<ExceptionResponseDetail> getExceptionResponseDetail() 
    {
		return exceptionResponseDetail;
	}

	public void setExceptionResponseDetail(List<ExceptionResponseDetail> exceptionResponseDetail) 
	{
		this.exceptionResponseDetail = exceptionResponseDetail;
	}

    @Override
    public String toString() {
    	
    	if(!details.isEmpty())
    	{
    		return "ExceptionResponse{" +
	                "date=" + date +
	                ", message='" + message + '\'' +
	                ", details='" + details + '\'' +
	                '}';	
    	}
    	else
    	{
	        return "ExceptionResponse{" +
	                "date=" + date +
	                ", message='" + message + '\'' +
	                ", detail='" + detail + '\'' +
	                '}';
    	}
    }
}
