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

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import lombok.Getter;

@Getter
public class PlyProperty {

    private final CppVector<UByte> data = new CppVector<>(DataType.uint8());
    private final CppVector<Long> listData = new CppVector<>(DataType.int64());

    private final String name;
    private final DracoDataType dataType;
    private final int dataTypeNumBytes;
    private final DracoDataType listDataType;
    private final int listDataTypeNumBytes;

    public PlyProperty(String name, DracoDataType dataType, DracoDataType listDataType) {
        this.name = name;
        this.dataType = dataType;
        this.dataTypeNumBytes = (int) dataType.getDataTypeLength();
        this.listDataType = listDataType;
        this.listDataTypeNumBytes = (int) listDataType.getDataTypeLength();
    }

    public void reserveData(int numEntries) {
        data.reserve((long) numEntries * dataTypeNumBytes);
    }

    public long getListEntryOffset(int entryId) {
        return listData.get(entryId * 2L);
    }

    public long getListEntryNumValues(int entryId) {
        return listData.get(entryId * 2L + 1);
    }

    public <T> Pointer<T> getDataEntryAddress(int entryId, DataType<T> type) {
        return data.getRawPointer().rawAdd((long) entryId * dataTypeNumBytes).toType(type);
    }

    public <T> void pushBackValue(Pointer<T> data) {
        this.data.insert(this.data.size(), data.asRawToUByte(), dataTypeNumBytes);
    }

    public boolean isList() {
        return listDataType != DracoDataType.INVALID;
    }

}
