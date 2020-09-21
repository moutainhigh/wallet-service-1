package io.jingwei.wallet.biz.exception;

public class ParseTxException extends RuntimeException {

    public ParseTxException(String message) {
        super(message);
    }

    public ParseTxException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseTxException(Throwable cause) {
        super(cause);
    }

}
