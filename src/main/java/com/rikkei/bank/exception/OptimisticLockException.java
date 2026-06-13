package com.rikkei.bank.exception;

import lombok.Getter;

@Getter
public class OptimisticLockException extends RuntimeException {
    private final int status = 409;

    public OptimisticLockException(String message) {
        super(message);
    }
}