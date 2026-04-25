package com.hackathon.favoritepayee.exception.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MethodArgumentErrorResponse {
	 
		private int status;
	    private String message;
	    private Map<String, String> errors;
	    
	    
}
