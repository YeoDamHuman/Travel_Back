package com.example.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {  // abstract 없이!
    private String code;
    private String message;
}