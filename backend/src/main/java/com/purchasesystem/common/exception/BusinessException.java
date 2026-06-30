package com.purchasesystem.common.exception;

/**
 * 업무 예외.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
