package io.jingwei.wallet.biz.exception;

public class ForkException extends RuntimeException {

    public ForkException(String message) {
        super(message);
    }

    public ForkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForkException(Throwable cause) {
        super(cause);
    }

}
