package com.laft.commons.response;

public enum ResponseType {
    SUCCESS("success"),
    ERROR("error"),
    WARNING("warning");

    private final String value;

    ResponseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
