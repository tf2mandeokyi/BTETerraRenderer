package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BatchTable;
import org.junit.Assert;
import org.junit.Test;

public class BatchTableTest {
    @Test
    public void givenBatchTableJsonAndBinary_testReadability() throws JsonProcessingException {
        String json = "{" +
                "    \"id\" : [\"unique id\", \"another unique id\"]," +
                "    \"displayName\" : [\"Building name\", \"Another building name\"]," +
                "    \"yearBuilt\" : [1999, 2015]," +
                "    \"address\" : [{\"street\" : \"Main Street\", \"houseNumber\" : \"1\"}, {\"street\" : \"Main Street\", \"houseNumber\" : \"2\"}]," +
                "    \"binary\" : { \"byteOffset\": 0, \"type\": \"SCALAR\", \"componentType\": \"INT\" }" +
                "}";
        byte[] binary = new byte[] { 12, 0, 0, 0, 24, 0, 0, 0 };
        BatchTable table = BatchTable.from(2, json, binary);

        Assert.assertEquals(table.get(0).getByName("id"), "unique id");
        Assert.assertEquals(table.get(1).getByName("yearBuilt"), 2015);
        Assert.assertEquals(table.get(1).getByName("binary"), 24);
    }
}
