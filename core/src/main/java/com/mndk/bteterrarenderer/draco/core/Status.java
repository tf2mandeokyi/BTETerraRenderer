package com.mndk.bteterrarenderer.draco.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Status {

    @Getter @RequiredArgsConstructor
    public enum Code {
        OK(0),
        DRACO_ERROR(-1),
        IO_ERROR(-2),
        INVALID_PARAMETER(-3),
        UNSUPPORTED_VERSION(-4),
        UNKNOWN_VERSION(-5),
        UNSUPPORTED_FEATURE(-6);

        private final int value;

        public static Code fromValue(int value) {
            for(Code code : Code.values()) {
                if(code.value == value) return code;
            }
            return null;
        }
    }

    public static final Status OK = new Status();

    public static StatusChain newChain() {
        return new StatusChain() {};
    }

    private final Code code;
    private final String errorMessage;
    @Getter
    private final StackTraceElement[] stackTrace;

    private Status() {
        this(Code.OK);
    }
    public Status(Status status) {
        this(status.code, status.errorMessage, status.stackTrace);
    }
    public Status(Code code) {
        this(code, null, code != Code.OK ? generateStackTrace() : new StackTraceElement[0]);
    }
    public Status(Code code, String errorMessage) {
        this(code, errorMessage, generateStackTrace());
    }

    private static StackTraceElement[] generateStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length - 3];
        System.arraycopy(stackTrace, 3, newStackTrace, 0, newStackTrace.length);
        return newStackTrace;
    }

    public boolean isOk() {
        return this.code == Code.OK;
    }

    public boolean isError(@Nullable StatusChain chain) {
        if(this.code != Code.OK) {
            if(chain != null) chain.set(this);
            return true;
        }
        return false;
    }

    public String getErrorMessage() {
        return (this.code == null ? "UNKNOWN_STATUS_VALUE" : this.code) + ": " + this.errorMessage;
    }

    public DracoCompressionException getException() {
        return new DracoCompressionException(this);
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
