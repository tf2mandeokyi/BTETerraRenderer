package com.mndk.bteterrarenderer.draco.core;

import org.junit.Assert;

public class StatusAssert {

    public static void fail(Status status) {
        throw status.getException();
    }

    public static void assertOk(Status status) {
        if (!status.isOk()) {
            fail(status);
        }
    }

    public static <T> T assertOk(StatusOr<T> statusOr) {
        Status status = statusOr.getStatus();
        assertOk(status);
        return statusOr.getValue();
    }

    public static void assertError(Status status) {
        if (status.isOk()) {
            Assert.fail("Expected error, but got OK");
        }
    }

}
