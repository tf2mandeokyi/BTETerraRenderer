package com.mndk.bteterrarenderer.draco.core;

public class StatusChain {

    private Status status = Status.ok();

    public Status get() { return status; }
    public void set(Status status) { this.status = status; }

    public boolean isOk() { return status.isOk(); }
    public boolean isError() { return status.isError(); }

    @Override
    public String toString() {
        return status.toString();
    }
}
