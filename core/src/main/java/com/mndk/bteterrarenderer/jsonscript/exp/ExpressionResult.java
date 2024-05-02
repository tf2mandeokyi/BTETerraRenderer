package com.mndk.bteterrarenderer.jsonscript.exp;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

public interface ExpressionResult {
    /**
     * @return A value expression has returned, or {@code null} if it's either break or continue
     */
    default JsonScriptValue getValue() {
        return null;
    }

    /**
     * @return The name of a loop if any, {@code null} if it's neither break nor continue
     */
    @Nullable
    default String getLoopName() {
        return null;
    }

    /**
     * @return {@code true} if it's either break, continue, or return. {@code false} otherwise
     */
    default boolean isBreakType() {
        return false;
    }
    default boolean isReturn() {
        return false;
    }
    default boolean isLoopBreak() {
        return false;
    }
    default boolean isLoopContinue() {
        return false;
    }

    static ExpressionResult ok(JsonNode node) {
        return ok(JsonScriptValue.json(node));
    }

    static ExpressionResult ok() {
        return ok(JsonScriptValue.jsonNull());
    }

    static ExpressionResult ok(JsonScriptValue value) {
        return new Ok(value);
    }

    static ExpressionResult breakLoop(@Nullable String loopName) {
        return new BreakLoop(loopName);
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class Ok implements ExpressionResult {
        private final JsonScriptValue value;
    }

    abstract class BreakType implements ExpressionResult {
        public final boolean isBreakType() {
            return true;
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class Return extends BreakType {
        private final JsonScriptValue value;
        public boolean isReturn() {
            return true;
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class BreakLoop extends BreakType {
        private final String loopName;
        public boolean isLoopBreak() {
            return true;
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ContinueLoop extends BreakType {
        private final String loopName;
        public boolean isLoopContinue() {
            return true;
        }
    }
}
