package com.example.authservice.exceptions;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.authservice.dto.APIResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });
        return new ResponseEntity<Map<String, String>>(response,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponse> handleInvalidEnum(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException formatException) {
            String fieldName = formatException.getPath().get(0).getFieldName();
            String invalidValue = formatException.getValue().toString();
            String message = String.format(
                "Invalid value '%s' for field '%s'. Allowed values are: USER, SELLER.",
                invalidValue, fieldName
            );
            return new ResponseEntity<>(new APIResponse(message, false), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new APIResponse("Malformed request.", false), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(StackOverflowError.class)
    public ResponseEntity<APIResponse> handleStackOverflow(StackOverflowError ex) {
        String message = "Stack overflow error occurred. Likely due to infinite recursion in entity relationships.";
        return new ResponseEntity<>(new APIResponse(message, false), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new APIResponse(ex.getMessage(), false), HttpStatus.FORBIDDEN);
    }

    
}
