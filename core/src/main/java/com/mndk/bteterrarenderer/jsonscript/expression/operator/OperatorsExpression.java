package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.*;
import com.mndk.bteterrarenderer.jsonscript.expression.literal.LiteralExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@JsonDeserialize
public class OperatorsExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(OperatorsExpression.class);

    private final List<Element> postfixExpression;

    @JsonCreator
    public OperatorsExpression(List<JsonNode> elements) {
        List<Element> infix = parseNodesToInfix(elements);
        this.postfixExpression = convertInfixToPostfix(infix);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        Stack<JsonExpression> stack = new Stack<>();
        for(Element element : this.postfixExpression) {
            ExpressionResult result = element.evaluateStack(runtime, stack);
            if(result.isBreakType()) return result;
        }

        if(stack.size() != 1) {
            return ExpressionResult.error("operator error: expected stack size of 1 " +
                    "but found " + stack.size(), INFO);
        }
        return stack.pop().run(runtime, INFO);
    }

    private static List<Element> parseNodesToInfix(List<JsonNode> elements) {

        List<Element> result = new ArrayList<>();

        elements.forEach(node -> {
            Element peek = result.isEmpty() ? null : result.get(result.size() - 1);
            ElementType peekType = peek == null ? null : peek.getType();
            boolean peekIsExpressionType = peekType != null && peekType.isExpressionType;

            // Operators
            if (node.isTextual()) {
                if(peekType == ElementType.UNARY) {
                    throw new IllegalArgumentException("any operators cannot come after an unary operator");
                }
                String symbol = node.asText();
                result.add(peekIsExpressionType ? newBinary(symbol) : newUnary(symbol));
                return;
            }

            // Expressions & Parentheses
            if (peekIsExpressionType) {
                throw new IllegalArgumentException("expressions cannot come right after another expressions");
            }
            if(node.isArray()) {
                ArrayNode array = (ArrayNode) node;
                List<JsonNode> innerInfix = new ArrayList<>();
                array.forEach(innerInfix::add);

                List<Element> innerPostfix = parseNodesToInfix(innerInfix);
                result.add(ParenthesisElement.OPEN);
                result.addAll(innerPostfix);
                result.add(ParenthesisElement.CLOSE);
            }
            else {
                result.add(newExpression(node));
            }
        });

        return result;
    }

    private static List<Element> convertInfixToPostfix(List<Element> infix) {
        Stack<Element> stack = new Stack<>();
        List<Element> postfix = new ArrayList<>();

        infix.forEach(element -> {
            ElementType type = element.getType();
            if (type == ElementType.EXP) {
                postfix.add(element);
            }
            else if(type.isOperatorType) {
                int currentPrecedence = element.getPrecedence();
                while(!stack.isEmpty() && currentPrecedence <= stack.peek().getPrecedence()) {
                    postfix.add(stack.pop());
                }
                stack.push(element);
            }
            else if(type == ElementType.PAREN_OPEN) {
                stack.push(element);
            }
            else if(type == ElementType.PAREN_CLOSE) {
                while(stack.peek().getType() != ElementType.PAREN_OPEN) {
                    postfix.add(stack.pop());
                }
                stack.pop();
            }
        });

        while(!stack.empty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    private static ExpressionElement newExpression(JsonNode node) {
        try {
            JsonExpression parsed = JsonScript.jsonMapper().treeToValue(node, JsonExpression.class);
            return new ExpressionElement(parsed);
        } catch(JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static OperatorElement newUnary(String symbol) {
        return new OperatorElement(JsonUnaryOperator.fromSymbol(symbol));
    }

    private static OperatorElement newBinary(String symbol) {
        return new OperatorElement(JsonBinaryOperator.fromSymbol(symbol));
    }

    private static abstract class Element {
        protected abstract ElementType getType();
        protected abstract ExpressionResult evaluateStack(JsonScriptRuntime runtime, Stack<JsonExpression> stack);
        protected int getPrecedence() {
            return Integer.MIN_VALUE;
        }
    }

    @RequiredArgsConstructor
    private static class ExpressionElement extends Element {
        private final JsonExpression expression;
        protected ElementType getType() {
            return ElementType.EXP;
        }
        protected ExpressionResult evaluateStack(JsonScriptRuntime runtime, Stack<JsonExpression> stack) {
            stack.push(expression);
            return ExpressionResult.ok();
        }
        public String toString() {
            return expression.toString();
        }
    }

    @RequiredArgsConstructor
    private static class OperatorElement extends Element {
        private final JsonOperator operator;
        protected ElementType getType() {
            return operator.getArgumentCount() == 1 ? ElementType.UNARY : ElementType.BINARY;
        }
        protected int getPrecedence() {
            return operator.getType().getPrecedence();
        }
        public String toString() {
            return operator.getSymbol();
        }

        protected ExpressionResult evaluateStack(JsonScriptRuntime runtime, Stack<JsonExpression> stack) {
            int size = operator.getArgumentCount();
            JsonExpression[] expressions = new JsonExpression[size];
            for(int i = size - 1; i >= 0; i--) {
                expressions[i] = stack.pop();
            }

            ResultTransformer.JNode transformer = operator.run(runtime, expressions)
                    .transformer()
                    .asJsonValue(ErrorMessages.valueMustBeJson("value"), INFO)
                    .asNode();
            if(transformer.isBreakType()) return transformer.getResult();

            JsonNode node = transformer.getWrapped();
            stack.push(new LiteralExpression(node));
            return ExpressionResult.ok();
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class ParenthesisElement extends Element {
        private static final ParenthesisElement OPEN = new ParenthesisElement(ElementType.PAREN_OPEN);
        private static final ParenthesisElement CLOSE = new ParenthesisElement(ElementType.PAREN_CLOSE);
        private final ElementType type;

        protected ExpressionResult evaluateStack(JsonScriptRuntime runtime, Stack<JsonExpression> stack) {
            throw new UnsupportedOperationException();
        }
        public String toString() {
            return type == ElementType.PAREN_OPEN ? "(" : ")";
        }
    }

    @RequiredArgsConstructor
    private enum ElementType {
        PAREN_OPEN(false, false),
        PAREN_CLOSE(true, false),
        EXP(true, false),
        UNARY(false, true),
        BINARY(false, true);

        private final boolean isExpressionType;
        private final boolean isOperatorType;
    }
}
