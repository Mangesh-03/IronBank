package com.mangesh.IronBank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundsException extends RuntimeException
{
    public InsufficientFundsException(String msg)
    {
        super(msg);
    }
}
