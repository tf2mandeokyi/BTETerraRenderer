package com.mndk.bteterrarenderer.jsonscript;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ConcatenationWithEmptyString")
public class JsonScriptTest {

    @Test
    public void givenYaml_testPrint() {
        String yml = "" +
                "- print: 1\n" +
                "- print: [ 5 ]\n";
        executeAndAssertPrintEquals(yml, bigIntNode(1), bigIntNode(5));
    }

    @Test
    public void givenYaml_testVariable() {
        String yml = "" +
                "- let: [ a, 3 ]\n" +
                "- let: [ b, print: [ get: a ] ]\n" +
                "- print: [ get: b ]";
        executeAndAssertPrintEquals(yml, bigIntNode(3), NullNode.getInstance());
    }

    @Test
    public void givenYaml_testLiteral() throws JsonProcessingException {
        String yml = "" +
                "- let: [ a, literal: { print: 3 } ]\n" +
                "- print: [ get: a ]";
        executeAndAssertPrintEquals(yml, BTETerraRendererConstants.JSON_MAPPER.readTree("{ \"print\": 3 }"));
    }

    @Test
    public void givenYaml_testBinaryOperations() {
        String yml = "" +
                "- let: [ a, 2045 ]\n" +
                "- let: [ b, 512 ]\n" +
                "- print: [ bi-op: [ get: a,   '+', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,   '-', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,   '*', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,   '/', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,   '%', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,  '==', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,  '!=', get: b ] ]\n" +

                "- print: [ bi-op: [ true  , 'and', true   ] ]\n" +
                "- print: [ bi-op: [ true  , 'and', false  ] ]\n" +
                "- print: [ bi-op: [ false ,  'or', true   ] ]\n" +
                "- print: [ bi-op: [ false ,  'or', false  ] ]\n" +

                "- print: [ bi-op: [ get: a,  '>=', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,  '<=', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,   '>', get: b ] ]\n" +
                "- print: [ bi-op: [ get: a,   '<', get: b ] ]";
        executeAndAssertPrintEquals(yml,
                bigIntNode(2557),
                bigIntNode(1533),
                bigIntNode(1047040),
                bigIntNode(3),
                bigIntNode(509),
                BooleanNode.valueOf(false),
                BooleanNode.valueOf(true),

                BooleanNode.valueOf(true),
                BooleanNode.valueOf(false),
                BooleanNode.valueOf(true),
                BooleanNode.valueOf(false),

                BooleanNode.valueOf(true),
                BooleanNode.valueOf(false),
                BooleanNode.valueOf(true),
                BooleanNode.valueOf(false));
    }

    @Test
    public void givenYaml_testOperations() {
        // TODO: add more tests (and it would be better to split these as unit tests)
        String yml = "" +
                "- let: [ a, 3 ]\n" +
                "- let: [ b, 4 ]\n" +
                "- print: { ops: [ get: a,'+',get: b ] }\n" +
                "- print: { ops: [ '-',get: a,'*',get: b ] }\n" +
                "- print: { ops: [ '-',[get: a,'-',get: b] ] }\n" +
                "- print: { ops: [ get: a,'*',57,'+',get: b ] }\n" +
                "- print: { ops: [ get: a,'+',57,'*',get: b ] }\n" +
                "- print: { ops: [ 3,'-',[get: a,'+',get: b],'*',7 ] }";
        executeAndAssertPrintEquals(yml,
                bigIntNode(7),
                bigIntNode(-12),
                bigIntNode(1),
                bigIntNode(175),
                bigIntNode(231),
                bigIntNode(-46));
    }

    @Test
    public void givenYaml_testClosure() {
        String yml = "" +
                "- let: [ b, 4 ]\n" +
                "- - print: [ get: b ]\n" +
                "  - let: [ b, 5 ]\n" +
                "  - print: [ get: b ]\n" +
                "- print: [ get: b ]";
        executeAndAssertPrintEquals(yml,
                bigIntNode(4),
                bigIntNode(5),
                bigIntNode(4));
    }

    @Test
    public void givenYaml_testStringTemplate() {
        String yml = "" +
                "- let: [ urlTemplate, 'https://tile.googleapis.com/v1/createSession?key={KEY}' ]\n" +
                "- let: [ token, 'GOOGLE_MAPS_TOKEN' ]\n" +
                "- let: [ url, str-template: {\n" +
                "    template: { get: 'urlTemplate' },\n" +
                "    parameters: {\n" +
                "      'KEY': { get: 'token' }\n" +
                "    }\n" +
                "  }]\n" +
                "- print: [ get: url ]";
        executeAndAssertPrintEquals(yml,
                new TextNode("https://tile.googleapis.com/v1/createSession?key=GOOGLE_MAPS_TOKEN"));
    }

    private void executeAndAssertPrintEquals(String yml, JsonNode... nodes) {
        try {
            List<JsonNode> expected = Arrays.asList(nodes);
            Assert.assertEquals(expected, getPrintOutput(yml));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JsonNode> getPrintOutput(String yml) throws JsonProcessingException {
        JsonExpression expression = BTETerraRendererConstants.YAML_MAPPER.readValue(yml, JsonExpression.class);
        List<JsonNode> outputs = new ArrayList<>();
        JsonScriptRuntime runtime = new JsonScriptRuntime(output -> {
            if(output instanceof JsonScriptJsonValue) {
                outputs.add(((JsonScriptJsonValue) output).getNode());
            }
        });
        ExpressionResult result = expression.run(runtime, null);
        if(result.isError()) {
            throw result.makeException();
        }
        return outputs;
    }

    private static BigIntegerNode bigIntNode(int value) {
        return new BigIntegerNode(BigInteger.valueOf(value));
    }

}
