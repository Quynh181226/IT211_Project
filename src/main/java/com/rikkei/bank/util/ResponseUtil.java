package com.rikkei.bank.util;

import com.rikkei.bank.dto.common.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<StandardResponse<T>> success(T data, String message) {
        StandardResponse<T> response = new StandardResponse<>(true, message, data, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<StandardResponse<T>> created(T data, String message) {
        StandardResponse<T> response = new StandardResponse<>(true, message, data, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public static ResponseEntity<StandardResponse<Void>> noContent(String message) {
        StandardResponse<Void> response = new StandardResponse<>(true, message, null, HttpStatus.NO_CONTENT.value());
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    public static <T> ResponseEntity<StandardResponse<T>> error(String message, HttpStatus status) {
        StandardResponse<T> response = new StandardResponse<>(false, message, null, status.value());
        return new ResponseEntity<>(response, status);
    }
}