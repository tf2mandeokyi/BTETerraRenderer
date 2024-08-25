package com.mndk.bteterrarenderer.draco.core;

public class DracoCompressionRuntimeException extends RuntimeException {
    public DracoCompressionRuntimeException(Status status) {
        super(status.toString());
        StackTraceElement[] stackTrace = status.getStackTrace();
        if(stackTrace != null) this.setStackTrace(stackTrace);
    }
    public DracoCompressionRuntimeException(Status status, Throwable cause) {
        super(status.toString(), cause);
        StackTraceElement[] stackTrace = status.getStackTrace();
        if(stackTrace != null) this.setStackTrace(stackTrace);
    }
}
