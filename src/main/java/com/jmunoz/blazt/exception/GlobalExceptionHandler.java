package com.jmunoz.blazt.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private ErrorAttributes errorAttributes;

    @ExceptionHandler(CatSurpriseException.class)
    public ResponseEntity<Map<String, Object>> handleCatSurpriseException(CatSurpriseException ex, WebRequest webRequest) {

        var status = HttpStatus.NOT_FOUND;
        var errorAttributesMap = mapToDefaultAttributes(ex, status, webRequest);
        errorAttributesMap.put("kitty", "=^._.^= ∫");

        return new ResponseEntity<>(errorAttributesMap, status);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<Map<String, Object>> handleInterruptedException(InterruptedException ex, WebRequest webRequest) {

        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var errorAttributesMap = mapToDefaultAttributes(ex, status, webRequest);

        return new ResponseEntity<>(errorAttributesMap, status);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleExecutionException(ExecutionException ex, WebRequest webRequest) {

        Throwable cause = ex.getCause();
        if (cause instanceof CatSurpriseException catEx) {
            return handleCatSurpriseException(catEx, webRequest);
        }

        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var errorAttributesMap = mapToDefaultAttributes(ex, status, webRequest);

        return new ResponseEntity<>(errorAttributesMap, status);
    }

    private Map<String, Object> mapToDefaultAttributes(Exception ex, HttpStatus status, WebRequest webRequest) {
        var errorAttributesMap = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        errorAttributesMap.put("error", status.getReasonPhrase());
        errorAttributesMap.put("status", status.value());
        errorAttributesMap.put("message", ex.getMessage());
        return errorAttributesMap;
    }
}
