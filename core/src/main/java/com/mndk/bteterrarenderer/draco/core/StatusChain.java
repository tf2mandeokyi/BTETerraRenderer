package com.mndk.bteterrarenderer.draco.core;

public abstract class StatusChain {

    private Status status = new Status();

    StatusChain() {}

    public Status get() {
        return status;
    }

    public void set(Status status) {
        this.status = status;
    }
}
