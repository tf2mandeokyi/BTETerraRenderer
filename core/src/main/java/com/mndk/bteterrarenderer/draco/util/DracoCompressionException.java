package com.mndk.bteterrarenderer.draco.util;

import com.mndk.bteterrarenderer.draco.core.Status;

public class DracoCompressionException extends Exception {
    public DracoCompressionException(Status status) {
        super(status.toString());
    }
    public DracoCompressionException(String message) {
        super(message);
    }
}
