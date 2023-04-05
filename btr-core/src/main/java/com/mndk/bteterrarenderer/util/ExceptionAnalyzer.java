package com.mndk.bteterrarenderer.util;

public class ExceptionAnalyzer {

    public static boolean isProjectionBoundsException(Exception e) {
        return e.getClass().getSimpleName().toLowerCase().contains("projectionbounds");
    }

}
