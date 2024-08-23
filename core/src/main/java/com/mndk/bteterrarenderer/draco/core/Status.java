package com.mndk.bteterrarenderer.draco.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

import static com.mndk.bteterrarenderer.draco.core.Status.Code.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Status {

    @Getter @RequiredArgsConstructor
    enum Code {
        OK(0),
        DRACO_ERROR(-1),
        IO_ERROR(-2),
        INVALID_PARAMETER(-3),
        UNSUPPORTED_VERSION(-4),
        UNKNOWN_VERSION(-5),
        UNSUPPORTED_FEATURE(-6);

        private final int value;
    }

    private static final Status OK = new Status();
    public static Status ok() { return OK; }
    public static Status dracoError(String message) { return of(DRACO_ERROR, message); }
    public static Status dracoError(String message, Throwable t) { return of(DRACO_ERROR, message, t); }
    public static Status ioError(String message) { return of(IO_ERROR, message); }
    public static Status ioError(String message, Throwable t) { return of(IO_ERROR, message, t); }
    public static Status invalidParameter(String message) { return of(INVALID_PARAMETER, message); }
    public static Status invalidParameter(String message, Throwable t) { return of(INVALID_PARAMETER, message, t); }
    public static Status unsupportedVersion(String message) { return of(UNSUPPORTED_VERSION, message); }
    public static Status unsupportedVersion(String message, Throwable t) { return of(UNSUPPORTED_VERSION, message, t); }
    public static Status unknownVersion(String message) { return of(UNKNOWN_VERSION, message); }
    public static Status unknownVersion(String message, Throwable t) { return of(UNKNOWN_VERSION, message, t); }
    public static Status unsupportedFeature(String message) { return of(UNSUPPORTED_FEATURE, message); }
    public static Status unsupportedFeature(String message, Throwable t) { return of(UNSUPPORTED_FEATURE, message, t); }

    private static Status of(Code code, String message) {
        return new Status(code, message, generateStackTrace(), null);
    }
    private static Status of(Code code, String message, Throwable cause) {
        return new Status(code, message, generateStackTrace(), cause);
    }

    private final Code code;
    private final String errorMessage;
    @Getter
    private final StackTraceElement[] stackTrace;
    @Nullable
    private final Throwable cause;

    Status() {
        this(Code.OK, null, new StackTraceElement[0], null);
    }
    Status(Code code, String errorMessage) {
        this(code, errorMessage, generateStackTrace(), null);
    }

    static StackTraceElement[] generateStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length - 4];
        System.arraycopy(stackTrace, 3, newStackTrace, 0, newStackTrace.length);
        return newStackTrace;
    }

    public boolean isOk() {
        return this.code == Code.OK;
    }

    public boolean isError() {
        return this.code != Code.OK;
    }

    public boolean isError(@Nullable StatusChain chain) {
        if(this.code != Code.OK) {
            if(chain != null) chain.set(this);
            return true;
        }
        return false;
    }

    public String getErrorMessage() {
        return (this.code == null ? "UNKNOWN_STATUS_VALUE" : this.code) + " - " + this.errorMessage;
    }

    public DracoCompressionRuntimeException getException() {
        return cause != null
                ? new DracoCompressionRuntimeException(this, cause)
                : new DracoCompressionRuntimeException(this);
    }

    public void throwException() {
        if(this.isError()) throw this.getException();
    }

    @Override
    public String toString() {
        if(this.isOk()) return this.code.toString();
        return this.getErrorMessage();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof Status) {
            Status other = (Status) obj;
            return this.code == other.code && this.errorMessage.equals(other.errorMessage);
        }
        return false;
    }
}
