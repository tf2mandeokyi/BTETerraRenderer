package com.mndk.bteterrarenderer.draco.core;

import org.junit.Assert;

public class StatusAssert {

    public static void assertOk(Status status) {
        if(!status.isOk()) {
            Assert.fail(status.getStackErrorMessage());
        }
    }

    public static void assertError(Status status) {
        if(status.isOk()) {
            Assert.fail("Expected error, but got OK");
        }
    }

}
