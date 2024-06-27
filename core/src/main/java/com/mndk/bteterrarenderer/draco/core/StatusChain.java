package com.mndk.bteterrarenderer.draco.core;

public abstract class StatusChain {

    private Status status = Status.OK;

    StatusChain() {}

    public Status get() {
        return status;
    }

    public void set(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status.toString();
    }
}
