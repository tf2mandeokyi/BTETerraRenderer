package com.mndk.bteterrarenderer.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is dumb. in 1.18.2 class loaders can't even find unloaded impl classes
 * <br/>
 * TODO I should find another method better than this
 * <br/>
 * (Won't make this deprecated though, since this works in 1.12.2)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConnectorImpl {}
