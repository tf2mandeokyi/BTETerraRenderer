package com.mndk.bteterrarenderer.ogc3dtiles.table;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.ogc3dtiles.Ogc3dTiles;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchTable implements Iterable<BatchTable.Row> {

    private final int batchModelCount;
    private final Map<String, Integer> columnIndexMap = new HashMap<>();
    private final List<Object[]> columns = new ArrayList<>();

    public static BatchTable from(int batchModelCount, String json, byte[] binary) throws JsonProcessingException {
        RawBatchTableJson rawBatchTableJson = Ogc3dTiles.jsonMapper().readValue(json, RawBatchTableJson.class);
        BatchTable batchTable = new BatchTable(batchModelCount);

        rawBatchTableJson.forEach((columnName, tableElement) ->
                batchTable.addColumn(batchModelCount, columnName, tableElement, binary));
        return batchTable;
    }

    private void addColumn(int batchModelCount, String columnName, BinaryJsonTableElement<?> tableElement, byte[] binary) {
        Object[] column = tableElement.makeColumn(batchModelCount, binary);
        int index = this.columns.size();
        this.columnIndexMap.put(columnName, index);
        this.columns.add(column);
    }

    public Row get(int index) {
        return new Row(index);
    }

    @Nonnull
    @Override
    public Iterator<Row> iterator() {
        return new RowIterator();
    }

    public String toString() {
        StringBuilder s = new StringBuilder("BatchTable[");
        Iterator<Row> iterator = this.iterator();
        while(iterator.hasNext()) {
            s.append(iterator.next());
            if(iterator.hasNext()) {
                s.append(", ");
            }
        }
        return s + "]";
    }

    @RequiredArgsConstructor
    public class Row {
        private final int rowIndex;

        /**
         * @return null if not found, else otherwise
         */
        public Object getByName(String columnName) {
            Integer columnIndex = columnIndexMap.get(columnName);
            if(columnIndex == null) return null;
            return columns.get(columnIndex)[rowIndex];
        }

        public String toString() {
            StringBuilder s = new StringBuilder("BatchTableElement[");
            s.append("index=").append(rowIndex);
            if(!columnIndexMap.isEmpty()) s.append(", ");
            s.append(columnIndexMap.keySet().stream()
                    .map(c -> c + "=" + this.getByName(c)).collect(Collectors.joining(", ")));
            return s + "]";
        }
    }

    class RowIterator implements Iterator<Row> {
        private int i = 0;

        @Override
        public boolean hasNext() {
            return i < batchModelCount;
        }

        @Override
        public Row next() {
            return get(i++);
        }
    }

    @JsonDeserialize(using = RawBatchTableJson.Deserializer.class)
    static class RawBatchTableJson extends HashMap<String, BinaryJsonTableElement<?>> {

        static class Deserializer extends JsonDeserializer<RawBatchTableJson> {
            @Override
            public RawBatchTableJson deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                if (p.currentToken() != JsonToken.START_OBJECT) {
                    throw JsonMappingException.from(p, "expected object start, found: " + p.currentToken());
                }

                RawBatchTableJson result = new RawBatchTableJson();
                while(p.nextToken() != JsonToken.END_OBJECT) {
                    if (p.currentToken() != JsonToken.FIELD_NAME) {
                        throw JsonMappingException.from(p, "expected field, found: " + p.currentToken());
                    }
                    String fieldName = p.currentName();
                    p.nextToken();
                    BinaryJsonTableElement<?> tableElement = p.readValueAs(new TypeReference<BinaryJsonTableElement<List<Object>>>() {});
                    result.put(fieldName, tableElement);
                }
                return result;
            }
        }

    }
}
