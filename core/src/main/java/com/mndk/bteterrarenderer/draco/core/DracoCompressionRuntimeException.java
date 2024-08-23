package com.mndk.bteterrarenderer.draco.core;

public class DracoCompressionRuntimeException extends RuntimeException {
    public DracoCompressionRuntimeException(Status status) {
        super(status.toString());
        this.setStackTrace(status.getStackTrace());
    }
    public DracoCompressionRuntimeException(Status status, Throwable cause) {
        super(status.toString(), cause);
        this.setStackTrace(status.getStackTrace());
    }
}
