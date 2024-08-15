package com.mndk.bteterrarenderer.draco.core;

public class DracoCompressionRuntimeException extends RuntimeException {
    public DracoCompressionRuntimeException(Status status) {
        super(status.toString());
        this.setStackTrace(status.getStackTrace());
    }
}
