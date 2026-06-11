package com.rikkei.bank.exception;

import lombok.Getter;

@Getter
public class InsufficientBalanceException extends RuntimeException {
    private final int status = 409;

    public InsufficientBalanceException(String message) {
        super(message);
    }
}