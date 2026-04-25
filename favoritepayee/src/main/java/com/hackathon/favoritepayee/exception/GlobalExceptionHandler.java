package com.hackathon.favoritepayee.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hackathon.favoritepayee.exception.response.ErrorResponse;
import com.hackathon.favoritepayee.exception.response.MethodArgumentErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MethodArgumentErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        MethodArgumentErrorResponse mr=new MethodArgumentErrorResponse(HttpStatus.BAD_REQUEST,"Validation failed",errors);

        return new ResponseEntity<>(mr, HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNODataFOundException(
    		ResourceNotFoundException ex) {

       

        return new ResponseEntity<>(new  ErrorResponse(404,ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
	
	
}
