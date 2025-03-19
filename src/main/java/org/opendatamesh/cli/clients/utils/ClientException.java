package org.opendatamesh.cli.clients.utils;

public class ClientException extends RuntimeException {
    private final int code;
    private final String responseBody;

    public ClientException(int statusCode, String responseBody) {
        this.code = statusCode;
        this.responseBody = responseBody;
    }

    @Override
    public String getMessage() {
        return "{" +
                "code=" + code +
                ", responseBody='" + responseBody +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

}
