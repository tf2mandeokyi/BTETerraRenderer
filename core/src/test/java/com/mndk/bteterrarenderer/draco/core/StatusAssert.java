package com.mndk.bteterrarenderer.draco.core;

import org.junit.Assert;

import java.util.function.Consumer;

public class StatusAssert {

    public static void fail(Status status) {
        throw status.getRuntimeException();
    }

    public static Consumer<Status> consumer() {
        return StatusAssert::assertOk;
    }

    public static void assertOk(Status status) {
        if(!status.isOk()) {
            fail(status);
        }
    }

    public static <T> T assertOk(StatusOr<T> statusOr) {
        Status status = statusOr.getStatus();
        assertOk(status);
        return statusOr.getValue();
    }

    public static void assertError(Status status) {
        if(status.isOk()) {
            Assert.fail("Expected error, but got OK");
        }
    }

}
