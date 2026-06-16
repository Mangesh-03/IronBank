package com.mangesh.IronBank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedAccessException extends  RuntimeException
{
    public UnauthorizedAccessException(String msg)
    {
        super(msg);
    }
}
