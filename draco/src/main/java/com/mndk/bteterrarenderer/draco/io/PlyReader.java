/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.util.StringUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class PlyReader {

    private enum Format {
        LITTLE_ENDIAN, ASCII
    }

    private final List<PlyElement> elements = new ArrayList<>();
    private final Map<String, Integer> elementIndex = new HashMap<>();
    private Format format = Format.LITTLE_ENDIAN;

    public Status read(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        AtomicReference<String> valueRef = new AtomicReference<>();
        if (DracoParserUtils.parseString(buffer, valueRef).isError(chain)) return chain.get();
        String value = valueRef.get();
        if (!value.equals("ply")) {
            return Status.invalidParameter("Not a valid ply file");
        }
        DracoParserUtils.skipLine(buffer);

        DracoParserUtils.parseLine(buffer, valueRef);
        value = valueRef.get();
        String format, version;
        List<String> words = this.splitWords(value);
        if (words.size() >= 3 && words.get(0).equals("format")) {
            format = words.get(1);
            version = words.get(2);
        } else {
            return Status.invalidParameter("Missing or wrong format line");
        }
        if (!version.equals("1.0")) {
            return Status.unsupportedVersion("Unsupported PLY version");
        }
        if (format.equals("binary_big_endian")) {
            return Status.unsupportedVersion("Unsupported format. Currently we support only ascii and" +
                    " binary_little_endian format.");
        }
        if (format.equals("ascii")) {
            this.format = Format.ASCII;
        } else {
            this.format = Format.LITTLE_ENDIAN;
        }

        if (this.parseHeader(buffer).isError(chain)) return chain.get();
        if (this.parsePropertiesData(buffer).isError(chain)) return chain.get();
        return Status.ok();
    }

    public PlyElement getElementByName(String name) {
        if (!elementIndex.containsKey(name)) return null;
        return elements.get(elementIndex.get(name));
    }

    public int getNumElements() {
        return elements.size();
    }

    public PlyElement getElement(int elementIndex) {
        return elements.get(elementIndex);
    }

    private Status parseHeader(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        while (true) {
            StatusOr<Boolean> end = this.parseEndHeader(buffer);
            if (end.isError(chain)) return chain.get();
            if (end.getValue()) break;

            if (this.parseElement(buffer)) continue;

            StatusOr<Boolean> propertyParsed = this.parseProperty(buffer);
            if (propertyParsed.isError(chain)) return chain.get();
            if (propertyParsed.getValue()) continue;

            DracoParserUtils.skipLine(buffer);
        }
        return Status.ok();
    }

    private StatusOr<Boolean> parseEndHeader(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        DracoParserUtils.skipWhitespace(buffer);
        RawPointer c = RawPointer.newArray(10);
        if (buffer.peek(c, 10).isError(chain)) return StatusOr.error(chain.get());
        if (!PointerHelper.rawToString(c, 10).equals("end_header")) {
            return StatusOr.ok(false);
        }
        DracoParserUtils.skipLine(buffer);
        return StatusOr.ok(true);
    }

    private boolean parseElement(DecoderBuffer buffer) {
        DecoderBuffer lineBuffer = new DecoderBuffer(buffer);
        AtomicReference<String> lineRef = new AtomicReference<>();
        DracoParserUtils.parseLine(lineBuffer, lineRef);
        String line = lineRef.get();

        String elementName;
        long count;
        List<String> words = this.splitWords(line);
        if (words.size() >= 3 && words.get(0).equals("element")) {
            elementName = words.get(1);
            String countStr = words.get(2);
            count = Long.parseLong(countStr);
        } else {
            return false;
        }
        elementIndex.put(elementName, elements.size());
        elements.add(new PlyElement(elementName, count));
        buffer.reset(lineBuffer);
        return true;
    }

    private StatusOr<Boolean> parseProperty(DecoderBuffer buffer) {
        if (elements.isEmpty()) return StatusOr.ok(false); // Ignore properties if there is no active element.
        DecoderBuffer lineBuffer = new DecoderBuffer(buffer);
        AtomicReference<String> lineRef = new AtomicReference<>();
        DracoParserUtils.parseLine(lineBuffer, lineRef);
        String line = lineRef.get();

        String dataTypeStr = "", listTypeStr = "", propertyName = "";
        boolean propertySearch = false;
        List<String> words = this.splitWords(line);
        if (words.size() >= 3 && words.get(0).equals("property") && !words.get(1).equals("list")) {
            propertySearch = true;
            dataTypeStr = words.get(1);
            propertyName = words.get(2);
        }

        boolean propertyListSearch = false;
        if (words.size() >= 5 && words.get(0).equals("property") && words.get(1).equals("list")) {
            propertyListSearch = true;
            listTypeStr = words.get(2);
            dataTypeStr = words.get(3);
            propertyName = words.get(4);
        }
        if (!propertySearch && !propertyListSearch) {
            return StatusOr.ok(false);
        }
        DracoDataType dataType = this.getDataTypeFromString(dataTypeStr);
        if (dataType == DracoDataType.INVALID) {
            return StatusOr.invalidParameter("Wrong property data type");
        }
        DracoDataType listType = DracoDataType.INVALID;
        if (propertyListSearch) {
            listType = this.getDataTypeFromString(listTypeStr);
            if (listType == DracoDataType.INVALID) {
                return StatusOr.invalidParameter("Wrong property list type");
            }
        }
        elements.get(elements.size() - 1).addProperty(new PlyProperty(propertyName, dataType, listType));
        buffer.reset(lineBuffer);
        return StatusOr.ok(true);
    }

    private Status parsePropertiesData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        for (int i = 0; i < elements.size(); i++) {
            if (format == Format.LITTLE_ENDIAN) {
                if (this.parseElementData(buffer, i).isError(chain)) return chain.get();
            } else if (format == Format.ASCII) {
                if (this.parseElementDataAscii(buffer, i).isError(chain)) return chain.get();
            }
        }
        return Status.ok();
    }

    private <T> Status parseElementData(DecoderBuffer buffer, int elementIndex) {
        StatusChain chain = new StatusChain();

        PlyElement element = this.elements.get(elementIndex);
        for (int entry = 0; entry < element.getNumEntries(); entry++) {
            for (int i = 0; i < element.getNumProperties(); i++) {
                PlyProperty prop = element.getProperty(i);
                if (prop.isList()) {
                    // Parse the number of entries for the list element.
                    DataNumberType<T> listDataType = prop.getListDataType().getActualType();
                    Pointer<T> numEntriesRef = listDataType.newOwned();
                    if (buffer.decode(numEntriesRef).isError(chain)) return chain.get();
                    long numEntries = listDataType.toLong(numEntriesRef.get());
                    // Store offset to the main data entry.
                    prop.getListData().pushBack(prop.getData().size() / prop.getDataTypeNumBytes());
                    // Store the number of entries.
                    prop.getListData().pushBack(numEntries);
                    // Read and store the actual property data
                    long numBytesToRead = prop.getDataTypeNumBytes() * numEntries;
                    prop.getData().insert(prop.getData().size(), buffer.getDataHead().toUByte(), numBytesToRead);
                    buffer.advance(numBytesToRead);
                } else {
                    prop.getData().insert(prop.getData().size(), buffer.getDataHead().toUByte(), prop.getDataTypeNumBytes());
                    buffer.advance(prop.getDataTypeNumBytes());
                }
            }
        }
        return Status.ok();
    }

    private Status parseElementDataAscii(DecoderBuffer buffer, int elementIndex) {
        StatusChain chain = new StatusChain();

        PlyElement element = this.elements.get(elementIndex);
        for (int entry = 0; entry < element.getNumEntries(); entry++) {
            for (int i = 0; i < element.getNumProperties(); i++) {
                PlyProperty prop = element.getProperty(i);
                PlyPropertyWriter<Double> propWriter = new PlyPropertyWriter<>(DataType.float64(), prop);
                int numEntries = 1;
                if (prop.isList()) {
                    DracoParserUtils.skipWhitespace(buffer);
                    AtomicReference<Integer> numEntriesRef = new AtomicReference<>();
                    if (DracoParserUtils.parseSignedInt(buffer, numEntriesRef).isError(chain)) return chain.get();
                    numEntries = numEntriesRef.get();
                    prop.getListData().pushBack(prop.getData().size() / prop.getDataTypeNumBytes());
                    prop.getListData().pushBack((long) numEntries);
                }
                for (int v = 0; v < numEntries; v++) {
                    DracoParserUtils.skipWhitespace(buffer);
                    if (prop.getDataType() == DracoDataType.FLOAT32 || prop.getDataType() == DracoDataType.FLOAT64) {
                        AtomicReference<Float> valRef = new AtomicReference<>();
                        if (DracoParserUtils.parseFloat(buffer, valRef).isError(chain)) return chain.get();
                        propWriter.pushBackValue((double) valRef.get());
                    } else {
                        AtomicReference<Integer> valRef = new AtomicReference<>();
                        if (DracoParserUtils.parseSignedInt(buffer, valRef).isError(chain)) return chain.get();
                        propWriter.pushBackValue((double) valRef.get());
                    }
                }
            }
        }
        return Status.ok();
    }

    private List<String> splitWords(String line) {
        List<String> output = new ArrayList<>();
        int start = 0;
        int end;

        while((end = StringUtil.indexOf(Pattern.compile("[ \t\n\f\r]"), line, start)) != -1) {
            String word = line.substring(start, end);
            if (!word.chars().allMatch(Character::isWhitespace)) {
                output.add(word);
            }
            start = end + 1;
        }

        String lastWord = line.substring(start);
        if (!lastWord.chars().allMatch(Character::isWhitespace)) {
            output.add(lastWord);
        }
        return output;
    }

    private DracoDataType getDataTypeFromString(String name) {
        switch (name) {
            case "char": case "int8": return DracoDataType.INT8;
            case "uchar": case "uint8": return DracoDataType.UINT8;
            case "short": case "int16": return DracoDataType.INT16;
            case "ushort": case "uint16": return DracoDataType.UINT16;
            case "int": case "int32": return DracoDataType.INT32;
            case "uint": case "uint32": return DracoDataType.UINT32;
            case "float": case "float32": return DracoDataType.FLOAT32;
            case "double": case "float64": return DracoDataType.FLOAT64;
            default: return DracoDataType.INVALID;
        }
    }

}
