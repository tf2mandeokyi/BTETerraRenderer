package com.mndk.bteterrarenderer.draco.core;

public class DracoCompressionException extends Exception {
    public DracoCompressionException(Status status) {
        super(status.toString());
        this.setStackTrace(status.getStackTrace());
    }
    public DracoCompressionException(String message) {
        super(message);
    }
}
