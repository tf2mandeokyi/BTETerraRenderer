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

package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;

import java.util.Iterator;

public class VertexRingIterator<T extends ICornerTable> implements Iterator<VertexIndex> {

    private final T cornerTable;
    private final CornerIndex startCorner;
    private CornerIndex corner;
    private boolean leftTraversal;

    public VertexRingIterator(T cornerTable, VertexIndex vertexIndex) {
        this.cornerTable = cornerTable;
        this.startCorner = cornerTable.getLeftMostCorner(vertexIndex);
        this.corner = null;
        this.leftTraversal = true;
    }

    @Override public boolean hasNext() { return getNext(false).isValid(); }
    @Override public VertexIndex next() { return getNext(true); }

    private VertexIndex getNext(boolean apply) {
        CornerIndex tempCorner = corner;
        boolean tempTraversal = leftTraversal;

        if(tempCorner == null) {
            tempCorner = startCorner;
        } else if(tempTraversal) {
            tempCorner = cornerTable.swingLeft(tempCorner);
            if (tempCorner.isInvalid()) {
                tempCorner = startCorner;
                tempTraversal = false;
            } else if (tempCorner.equals(startCorner)) {
                tempCorner = CornerIndex.INVALID;
            }
        } else {
            tempCorner = cornerTable.swingRight(tempCorner);
        }

        CornerIndex ringCorner = tempTraversal ? cornerTable.previous(tempCorner) : cornerTable.next(tempCorner);
        if(apply) {
            corner = tempCorner;
            leftTraversal = tempTraversal;
        }
        return cornerTable.getVertex(ringCorner);
    }
}
