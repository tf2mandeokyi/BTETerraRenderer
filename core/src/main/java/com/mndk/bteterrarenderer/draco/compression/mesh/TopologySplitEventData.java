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

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class TopologySplitEventData {
    private int splitSymbolId;
    private int sourceSymbolId;
    @Getter @Setter
    private EdgeFaceName sourceEdge;

    public TopologySplitEventData() {}

    public UInt getSplitSymbolId() { return UInt.of(splitSymbolId); }
    public UInt getSourceSymbolId() { return UInt.of(sourceSymbolId); }

    public void setSplitSymbolId(UInt splitSymbolId) { this.splitSymbolId = splitSymbolId.intValue(); }
    public void setSourceSymbolId(UInt sourceSymbolId) { this.sourceSymbolId = sourceSymbolId.intValue(); }
}
