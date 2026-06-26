package com.laft.commons.response;

public enum ResponseCode {

    OK_000("OK-000", ResponseType.SUCCESS, "Operación exitosa"),
    ERR_001("ERR-001", ResponseType.ERROR, "Saldo no disponible"),
    ERR_002("ERR-002", ResponseType.ERROR, "Recurso no encontrado"),
    ERR_003("ERR-003", ResponseType.ERROR, "Operación no permitida"),
    ERR_004("ERR-004", ResponseType.ERROR, "Argumento inválido"),
    ERR_005("ERR-005", ResponseType.ERROR, "Violación de regla de negocio"),
    ERR_006("ERR-006", ResponseType.ERROR, "Credenciales inválidas o usuario inactivo"),
    ERR_999("ERR-999", ResponseType.ERROR, "Error interno del servidor"),
    WRN_001("WRN-001", ResponseType.WARNING, "Advertencia");

    private final String code;
    private final ResponseType type;
    private final String defaultMessage;

    ResponseCode(String code, ResponseType type, String defaultMessage) {
        this.code = code;
        this.type = type;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public ResponseType getType() {
        return type;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
