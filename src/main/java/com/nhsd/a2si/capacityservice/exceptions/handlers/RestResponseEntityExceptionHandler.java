package com.nhsd.a2si.capacityservice.exceptions.handlers;

import com.nhsd.a2si.capacityservice.exceptions.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The Exception Handler is automatically registered by Spring Each annotated method will declare the exception
 * class it handles.
 */

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // AuthenticationException is thrown if the username and password http headers don't match the
    // values in the configuration.
    // A http status of 403 (forbidden) is returned if the header values are wrong.
    // (If the header names themselves are wrong, a 400 (bad request) is automatically returned.
    @ExceptionHandler(value = { AuthenticationException.class })
    protected ResponseEntity<ExceptionResponse> handleAuthenticationException(Exception exception,
                                                                              WebRequest request) {

        logger.debug("Handling Exception: {}", exception );

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                LocalDateTime.now().format(dateTimeFormatter),
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
    protected ResponseEntity<ExceptionResponse> handleUnsupportedOperationException(Exception exception,
                                                                                    WebRequest request) {

        logger.debug("Handling Exception: {}", exception );

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                LocalDateTime.now().format(dateTimeFormatter),
                exception.getMessage(),
                request.getDescription(false));

        // Return Error Response and appropriate HTTP Status Code
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_IMPLEMENTED);

    }
}