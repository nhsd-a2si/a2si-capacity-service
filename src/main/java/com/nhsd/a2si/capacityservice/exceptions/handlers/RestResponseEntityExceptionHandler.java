package com.nhsd.a2si.capacityservice.exceptions.handlers;

import com.nhsd.a2si.capacityservice.exceptions.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nhsd.a2si.capacityinformation.domain.CapacityInformation.STRING_DATE_FORMAT;

/**
 * The Exception Handler is automatically registered by Spring Each annotated method will declare the exception
 * class it handles.
 */

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    // AuthenticationException is thrown if the username and password http headers don't match the
    // values in the configuration.
    // A http status of 403 (forbidden) is returned if the header values are wrong.
    // (If the header names themselves are wrong, a 400 (bad request) is automatically returned.
    @ExceptionHandler(value = { AuthenticationException.class })
    protected ResponseEntity<Object> handleAuthenticationException(Exception exception, WebRequest request) {

        logger.debug("Handling Exception: {}", exception );

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                new SimpleDateFormat(STRING_DATE_FORMAT).format(new Date().getTime()),
                exception.getMessage(),
                request.getDescription(false));

        // Return Error Response and appropriate HTTP Status Code
        return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);

    }

    // UnsupportedOperationException is thrown if the resource and http method are not supported.
    // For example, a request to delete all capacity information objects is only supported when
    // using the Stub Repository, not when using the Redis implementation.
    //
    @ExceptionHandler(value = { UnsupportedOperationException.class })
    protected ResponseEntity<Object> handleUnsupportedOperationException(Exception exception, WebRequest request) {
        logger.debug("Handling Exception: {}", exception );

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                new SimpleDateFormat(STRING_DATE_FORMAT).format(new Date().getTime()),
                exception.getMessage(),
                request.getDescription(false));

        // Return Error Response and appropriate HTTP Status Code
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_IMPLEMENTED);

    }

    // Exposes the Validation API messages
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        logger.debug("Handling Exception: {}", ex );

        BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getAllErrors();
        List<String> errors = new ArrayList<String>();

        Map<String, String> errorMap = new HashMap<String, String>();
        
        
        
        
        for (ObjectError violation : bindingResult.getAllErrors()) {
        	
        	String key = violation.getObjectName();
        	
            errors.add(violation.getDefaultMessage());
        }
        
        List<ExceptionResponse> exceptionResponses = new ArrayList<ExceptionResponse>();
        
        for(String error : errors)
        {

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                new SimpleDateFormat(STRING_DATE_FORMAT).format(new Date().getTime()),
                "Validation Failed",
                error.toString());
        
        
        exceptionResponses.add(exceptionResponse);
        
        }

        return new ResponseEntity<>(exceptionResponses, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    // Handles malformed message bodies
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        logger.debug("Handling Exception: {}", ex);

        String details = "Not disclosed";

        if(ex.getCause().getMessage().toLowerCase().contains("duplicate field")){
            details = "Duplicate key";
        }

        if(ex.getCause().getMessage().toLowerCase().contains("cannot deserialize value of type")){
            final String message = ex.getCause().getMessage();
            final String element = message.substring(message.indexOf("[\"") +2, message.indexOf("\"]"));
            Matcher matcher = Pattern.compile("[`.]([^`.]+)?`").matcher(message);
            if(matcher.find()){
                details = String.join(
                        "", "Incorrect data type used for '", element, "'. ", "The expected data type is '", matcher.group(1), "'"
                );
            } else {
                details = String.join(
                        "","Incorrect data type used for '", element, "'"
                );
            }
        }
        return new ResponseEntity<>(new ExceptionResponse(new SimpleDateFormat(STRING_DATE_FORMAT).format(new Date().getTime()), "Not readable", details), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

}