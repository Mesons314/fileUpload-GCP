package com.xeroKarao.demo.exception;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ApiException {

    private String errorMessage;
    private Integer statusCode;
    private ZonedDateTime zonedDateTime;

    public void setErrorMessage(String string) {
    }

    public void setStatusCode(int value) {

    }

    public void setZonedDateTime(ZonedDateTime now) {
    }
}
