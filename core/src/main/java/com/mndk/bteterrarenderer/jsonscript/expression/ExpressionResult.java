package com.mndk.bteterrarenderer.jsonscript.expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;
import java.util.stream.Collectors;

public interface ExpressionResult {
    /**
     * @return A value expression has returned, or {@code null} if it's either break or continue
     */
    default JsonScriptValue getValue() {
        return null;
    }

    default ExpressionResult passedBy(ExpressionCallerInfo callerInfo) {
        return this;
    }

    default ResultTransformer.Exp transformer() {
        return ResultTransformer.of(this);
    }

    default RuntimeException makeException() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return {@code true} if it's either break, continue, or return. {@code false} otherwise
     */
    default boolean isBreakType() {
        return false;
    }
    default boolean isError() {
        return false;
    }
    default boolean isReturn() {
        return false;
    }
    default boolean isLoopBreak(String loopLabel) {
        return false;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // xD
    default boolean isLoopContinue(String loopLabel) {
        return false;
    }

    // ########## STATIC METHODS ##########

    static ExpressionResult ok(JsonNode node) {
        return ok(JsonScriptValue.json(node));
    }

    static ExpressionResult ok() {
        return ok(JsonScriptValue.jsonNull());
    }

    static ExpressionResult ok(@Nonnull JsonScriptValue value) {
        return new Ok(value);
    }

    static ExpressionResult returnExpression() {
        return new Return(JsonScriptValue.jsonNull());
    }

    static ExpressionResult returnExpression(@Nonnull JsonScriptValue value) {
        return new Return(value);
    }

    static ExpressionResult error(JsonScriptValue reason, @Nullable ExpressionCallerInfo callerInfo) {
        return new Error(reason).passedBy(callerInfo);
    }

    static ExpressionResult error(JsonNode reason, @Nullable ExpressionCallerInfo callerInfo) {
        return error(JsonScriptValue.json(reason), callerInfo);
    }

    static ExpressionResult error(String reason, @Nullable ExpressionCallerInfo callerInfo) {
        return error(new TextNode(reason), callerInfo);
    }

    static ExpressionResult breakLoop(@Nullable String loopName) {
        return new BreakLoop(loopName);
    }

    static ExpressionResult continueLoop(@Nullable String loopName) {
        return new ContinueLoop(loopName);
    }

    // ########## RESULT IMPLEMENTATIONS ##########

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
        public boolean isLoopBreak(String loopLabel) {
            if(this.loopName == null) return true;
            return this.loopName.equals(loopLabel);
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ContinueLoop extends BreakType {
        private final String loopName;
        public boolean isLoopContinue(String loopLabel) {
            if(this.loopName == null) return true;
            return this.loopName.equals(loopLabel);
        }
    }

    @Getter
    class Error extends BreakType {
        private final JsonScriptValue value;
        private final Stack<ExpressionCallerInfo> stack = new Stack<>();

        private Error(JsonScriptValue value) {
            this.value = value;
        }

        public boolean isError() {
            return true;
        }

        public ExpressionResult passedBy(@Nullable ExpressionCallerInfo callerInfo) {
            if(callerInfo != null) {
                this.stack.push(callerInfo);
            }
            return this;
        }

        public RuntimeException makeException() {
            String stackTrace = stack.stream()
                    .map(info -> "\tat " + info.getCallerName() + ": " + String.join(" - ", info.getExtraInfo()))
                    .collect(Collectors.joining("\n"));
            return new RuntimeException("JsonScript error: " + this.value + "\n" + stackTrace);
        }
    }
}
