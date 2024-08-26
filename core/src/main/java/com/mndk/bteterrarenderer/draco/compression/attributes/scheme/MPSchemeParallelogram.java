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

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class MPSchemeParallelogram {

    public void getParallelogramEntries(CornerIndex ci, ICornerTable table,
                                        CppVector<Integer> vertexToDataMap, AtomicInteger oppEntry,
                                        AtomicInteger nextEntry, AtomicInteger prevEntry) {
        oppEntry.set(vertexToDataMap.get(table.getVertex(ci).getValue()));
        nextEntry.set(vertexToDataMap.get(table.getVertex(table.next(ci)).getValue()));
        prevEntry.set(vertexToDataMap.get(table.getVertex(table.previous(ci)).getValue()));
    }

    public <DataT> Status computeParallelogramPrediction(
            int dataEntryId, CornerIndex ci, ICornerTable table, CppVector<Integer> vertexToDataMap,
            Pointer<DataT> inData, int numComponents, Pointer<DataT> outPrediction) {
        DataNumberType<DataT> dataType = outPrediction.getType().asNumber();

        CornerIndex oci = table.opposite(ci);
        if(oci.isInvalid()) return Status.ioError("Invalid corner index");
        AtomicInteger vertOppRef = new AtomicInteger();
        AtomicInteger vertNextRef = new AtomicInteger();
        AtomicInteger vertPrevRef = new AtomicInteger();
        getParallelogramEntries(oci, table, vertexToDataMap, vertOppRef, vertNextRef, vertPrevRef);
        int vertOpp = vertOppRef.get();
        int vertNext = vertNextRef.get();
        int vertPrev = vertPrevRef.get();
        if(vertOpp < dataEntryId && vertNext < dataEntryId && vertPrev < dataEntryId) {
            int vOppOff = vertOpp * numComponents;
            int vNextOff = vertNext * numComponents;
            int vPrevOff = vertPrev * numComponents;
            for(int c = 0; c < numComponents; c++) {
                long inDataNextOff = dataType.toLong(inData.get(vNextOff + c));
                long inDataPrevOff = dataType.toLong(inData.get(vPrevOff + c));
                long inDataOppOff = dataType.toLong(inData.get(vOppOff + c));
                long result = (inDataNextOff + inDataPrevOff) - inDataOppOff;
                outPrediction.set(c, dataType.from(result));
            }
            return Status.ok();
        }
        return Status.ioError("Not all data is available for prediction");
    }

}
