package com.faraz.expensemanagement.exception;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class Errors {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> bad(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> other(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Something went wrong"));
    }
}