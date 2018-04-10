package com.bjss.nhsd.a2si.capacityservice.exceptions.handlers;

public class ExceptionResponse {

    private String date;
    private String message;
    private String details;

    public ExceptionResponse(String date, String message, String details) {
        this.date = date;
        this.message = message;
        this.details = details;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ExceptionResponse{" +
                "date=" + date +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
