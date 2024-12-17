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

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.config.EncodingFeatures;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEdgebreakerConnectivityEncodingMethod;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;

public class MeshEdgebreakerEncoder extends MeshEncoder {

    private MeshEdgebreakerEncoderImplInterface impl = null;

    @Override
    public CornerTable getCornerTable() {
        return impl.getCornerTable();
    }

    @Override
    public MeshAttributeCornerTable getAttributeCornerTable(int attId) {
        return impl.getAttributeCornerTable(attId);
    }

    @Override
    public MeshAttributeIndicesEncodingData getAttributeEncodingData(int attId) {
        return impl.getAttributeEncodingData(attId);
    }

    @Override
    public UByte getEncodingMethod() {
        return UByte.of(MeshEncoderMethod.EDGEBREAKER.getValue());
    }

    @Override
    protected Status initializeEncoder() {
        boolean isStandardEdgebreakerAvailable = this.getOptions().isFeatureSupported(EncodingFeatures.EDGE_BREAKER);
        boolean isPredictiveEdgebreakerAvailable = this.getOptions().isFeatureSupported(EncodingFeatures.PREDICTIVE_EDGE_BREAKER);

        impl = null;
        // For tiny meshes it's usually better to use the basic edgebreaker.
        boolean isTinyMesh = this.getMesh().getNumFaces() < 1000;

        int selectedEdgebreakerMethodInt = this.getOptions().getGlobalInt("edgebreaker_method", -1);
        MeshEdgebreakerConnectivityEncodingMethod selectedEdgebreakerMethod =
                MeshEdgebreakerConnectivityEncodingMethod.valueOf(selectedEdgebreakerMethodInt);
        if (selectedEdgebreakerMethod == null) {
            if (isStandardEdgebreakerAvailable &&
                    (this.getOptions().getSpeed() >= 5 || !isPredictiveEdgebreakerAvailable || isTinyMesh)) {
                selectedEdgebreakerMethod = MeshEdgebreakerConnectivityEncodingMethod.STANDARD;
            } else {
                selectedEdgebreakerMethod = MeshEdgebreakerConnectivityEncodingMethod.VALENCE;
            }
        }

        if (selectedEdgebreakerMethod == MeshEdgebreakerConnectivityEncodingMethod.STANDARD) {
            if (isStandardEdgebreakerAvailable) {
                this.getBuffer().encode(UByte.of(MeshEdgebreakerConnectivityEncodingMethod.STANDARD.getValue()));
                impl = new MeshEdgebreakerEncoderImpl(new MeshEdgebreakerTraversalEncoder());
            }
        } else if (selectedEdgebreakerMethod == MeshEdgebreakerConnectivityEncodingMethod.VALENCE) {
            this.getBuffer().encode(UByte.of(MeshEdgebreakerConnectivityEncodingMethod.VALENCE.getValue()));
            impl = new MeshEdgebreakerEncoderImpl(new MeshEdgebreakerTraversalValenceEncoder());
        }

        if (impl == null) return Status.dracoError("Failed to initialize encoder");
        return impl.init(this);
    }

    @Override
    protected Status encodeConnectivity() {
        return impl.encodeConnectivity();
    }

    @Override
    protected Status generateAttributesEncoder(int attId) {
        return impl.generateAttributesEncoder(attId);
    }

    @Override
    protected Status encodeAttributesEncoderIdentifier(int attEncoderId) {
        return impl.encodeAttributesEncoderIdentifier(attEncoderId);
    }

    @Override
    protected void computeNumberOfEncodedPoints() {
        if (impl == null) return;

        CornerTable cornerTable = impl.getCornerTable();
        if (cornerTable == null) return;

        int numPoints = cornerTable.getNumVertices() - cornerTable.getNumIsolatedVertices();

        if (this.getMesh().getNumAttributes() > 1) {
            // Gather all corner tables for all non-position attributes.
            CppVector<MeshAttributeCornerTable> attributeCornerTables = new CppVector<>(MeshAttributeCornerTable::new);
            for (int i = 0; i < this.getMesh().getNumAttributes(); ++i) {
                if (this.getMesh().getAttribute(i).getAttributeType() == GeometryAttribute.Type.POSITION) {
                    continue;
                }
                MeshAttributeCornerTable attCornerTable = this.getAttributeCornerTable(i);
                if (attCornerTable != null) {
                    attributeCornerTables.pushBack(attCornerTable);
                }
            }

            for (VertexIndex vi : VertexIndex.range(0, cornerTable.getNumVertices())) {
                if (cornerTable.isVertexIsolated(vi)) continue;

                // Go around all corners of the vertex and keep track of the observed attribute seams.
                CornerIndex firstCornerIndex = cornerTable.getLeftMostCorner(vi);
                PointIndex firstPointIndex = this.getMesh().cornerToPointId(firstCornerIndex);

                PointIndex lastPointIndex = firstPointIndex;
                CornerIndex lastCornerIndex = firstCornerIndex;
                CornerIndex cornerIndex = cornerTable.swingRight(firstCornerIndex);
                int numAttributeSeams = 0;
                while (cornerIndex.isValid()) {
                    PointIndex pointIndex = this.getMesh().cornerToPointId(cornerIndex);
                    boolean seamFound = false;
                    if (!pointIndex.equals(lastPointIndex)) {
                        seamFound = true;
                        lastPointIndex = pointIndex;
                    } else {
                        for (int i = 0; i < attributeCornerTables.size(); ++i) {
                            VertexIndex vertex = attributeCornerTables.get(i).getVertex(cornerIndex);
                            VertexIndex lastVertex = attributeCornerTables.get(i).getVertex(lastCornerIndex);
                            if (!vertex.equals(lastVertex)) {
                                seamFound = true;
                                break;
                            }
                        }
                    }
                    if (seamFound) {
                        numAttributeSeams++;
                    }
                    if (cornerIndex.equals(firstCornerIndex)) break;
                    lastCornerIndex = cornerIndex;
                    cornerIndex = cornerTable.swingRight(cornerIndex);
                }

                if (!cornerTable.isOnBoundary(vi) && numAttributeSeams > 0) {
                    numPoints += numAttributeSeams - 1;
                } else {
                    numPoints += numAttributeSeams;
                }
            }
        }
        this.setNumEncodedPoints(numPoints);
    }

    @Override
    protected void computeNumberOfEncodedFaces() {
        if (impl == null) return;

        CornerTable cornerTable = impl.getCornerTable();
        if (cornerTable == null) return;

        this.setNumEncodedFaces(cornerTable.getNumFaces() - cornerTable.getNumDegeneratedFaces());
    }
}
