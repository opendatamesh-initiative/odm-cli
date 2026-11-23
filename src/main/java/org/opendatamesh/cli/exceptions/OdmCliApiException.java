package org.opendatamesh.cli.exceptions;

import org.springframework.http.HttpStatus;

public abstract class OdmCliApiException extends RuntimeException {

    private static final long serialVersionUID = 3876573329263306459L;

    public OdmCliApiException() {
        super();
    }

    public OdmCliApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public OdmCliApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public OdmCliApiException(String message) {
        super(message);
    }

    public OdmCliApiException(Throwable cause) {
        super(cause);
    }

    /**
     * @return the errorName
     */
    public String getErrorName() {
        return getClass().getSimpleName();
    }

    /**
     * @return the status
     */
    public abstract HttpStatus getStatus();

}
