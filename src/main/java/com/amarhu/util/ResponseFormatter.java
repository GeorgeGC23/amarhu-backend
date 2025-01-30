package com.amarhu.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseFormatter {

    public static ResponseEntity<Object> formatResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("message", message);
        response.put("data", data);

        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Object> formatErrorResponse(HttpStatus status, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", errorMessage);

        return new ResponseEntity<>(response, status);
    }
}
