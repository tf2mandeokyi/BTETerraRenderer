package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PlyDecoder {

    @Getter(AccessLevel.PROTECTED)
    private final DecoderBuffer buffer = new DecoderBuffer();
    private Mesh outMesh = null;
    private PointCloud outPointCloud = null;

    public Status decodeFromFile(File file, Mesh outMesh) {
        this.outMesh = outMesh;
        return decodeFromFile(file, (PointCloud) outMesh);
    }

    public Status decodeFromFile(File file, PointCloud outPointCloud) {
        try(InputStream inputStream = Files.newInputStream(file.toPath())) {
            this.buffer.init(inputStream);
        } catch(Exception e) {
            return Status.ioError("Unable to read input file", e);
        }
        return decodeFromBuffer(this.buffer, outPointCloud);
    }

    public Status decodeFromBuffer(DecoderBuffer buffer, Mesh outMesh) {
        this.outMesh = outMesh;
        return decodeFromBuffer(buffer, (PointCloud) outMesh);
    }

    public Status decodeFromBuffer(DecoderBuffer buffer, PointCloud outPointCloud) {
        this.outPointCloud = outPointCloud;
        this.buffer.init(buffer.getDataHead(), buffer.getRemainingSize());
        return decodeInternal();
    }

    protected Status decodeInternal() {
        StatusChain chain = new StatusChain();

        PlyReader plyReader = new PlyReader();
        if(plyReader.read(buffer).isError(chain)) return chain.get();
        if(outMesh != null) {
            if(decodeFaceData(plyReader.getElementByName("face")).isError(chain)) return chain.get();
        }
        if(decodeVertexData(plyReader.getElementByName("vertex")).isError(chain)) return chain.get();

        if(outMesh != null && outMesh.getNumFaces() != 0) {
            if(outPointCloud.deduplicateAttributeValues().isError(chain)) return chain.get();
            outPointCloud.deduplicatePointIds();
        }
        return Status.ok();
    }

    private Status decodeFaceData(PlyElement faceElement) {
        if(faceElement == null) {
            return Status.ok();
        }
        PlyProperty vertexIndices = faceElement.getPropertyByName("vertex_indices");
        if(vertexIndices == null) {
            vertexIndices = faceElement.getPropertyByName("vertex_index");
        }
        if(vertexIndices == null || !vertexIndices.isList()) {
            return Status.ioError("No faces defined");
        }

        outMesh.setNumFaces(countNumTriangles(faceElement, vertexIndices));
        long numPolygons = faceElement.getNumEntries();

        PlyPropertyReader<Integer> vertexIndexReader = new PlyPropertyReader<>(DataType.int32(), vertexIndices);
        FaceIndex faceIndex = FaceIndex.of(0);
        for(int i = 0; i < numPolygons; i++) {
            Mesh.Face face = new Mesh.Face();
            long listOffset = vertexIndices.getListEntryOffset(i);
            long listSize = vertexIndices.getListEntryNumValues(i);
            if(listSize < 3) continue;

            long numTriangles = listSize - 2;
            face.set(0, PointIndex.of(vertexIndexReader.readValue((int) listOffset)));
            for(long ti = 0; ti < numTriangles; ti++) {
                for(int c = 1; c < 3; c++) {
                    face.set(c, PointIndex.of(vertexIndexReader.readValue((int) (listOffset + ti + c))));
                }
                outMesh.setFace(faceIndex, face);
                faceIndex = faceIndex.increment();
            }
        }
        outMesh.setNumFaces(faceIndex.getValue());
        return Status.ok();
    }

    private Status decodeVertexData(PlyElement vertexElement) {
        if(vertexElement == null) {
            return Status.invalidParameter("vertex_element is null");
        }

        PlyProperty xProp = vertexElement.getPropertyByName("x");
        if(xProp == null) return Status.invalidParameter("x property is missing");
        PlyProperty yProp = vertexElement.getPropertyByName("y");
        if(yProp == null) return Status.invalidParameter("y property is missing");
        PlyProperty zProp = vertexElement.getPropertyByName("z");
        if(zProp == null) return Status.invalidParameter("z property is missing");

        int numVertices = vertexElement.getNumEntries();
        outPointCloud.setNumPoints(numVertices);

        {
            if (xProp.getDataType() != yProp.getDataType() || yProp.getDataType() != zProp.getDataType()) {
                return Status.invalidParameter("x, y, and z properties must have the same type");
            }
            DracoDataType dt = xProp.getDataType();
            if (dt != DracoDataType.FLOAT32 && dt != DracoDataType.INT32) {
                return Status.invalidParameter("x, y, and z properties must be of type float32 or int32");
            }

            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.POSITION, null, 3, dt, false);
            int attId = outPointCloud.addAttribute(va, true, numVertices);
            List<PlyProperty> properties = new ArrayList<>();
            properties.add(xProp);
            properties.add(yProp);
            properties.add(zProp);
            if (dt == DracoDataType.FLOAT32) {
                readPropertiesToAttribute(DataType.float32(), properties, outPointCloud.getAttribute(attId), numVertices);
            } else {
                readPropertiesToAttribute(DataType.int32(), properties, outPointCloud.getAttribute(attId), numVertices);
            }

            PlyProperty nXProp = vertexElement.getPropertyByName("nx");
            PlyProperty nYProp = vertexElement.getPropertyByName("ny");
            PlyProperty nZProp = vertexElement.getPropertyByName("nz");
            if (nXProp != null && nYProp != null && nZProp != null) {
                DracoDataType nXType = nXProp.getDataType();
                DracoDataType nYType = nYProp.getDataType();
                DracoDataType nZType = nZProp.getDataType();
                if (nXType == DracoDataType.FLOAT32 && nYType == DracoDataType.FLOAT32 && nZType == DracoDataType.FLOAT32) {
                    PlyPropertyReader<Float> xReader = new PlyPropertyReader<>(DataType.float32(), nXProp);
                    PlyPropertyReader<Float> yReader = new PlyPropertyReader<>(DataType.float32(), nYProp);
                    PlyPropertyReader<Float> zReader = new PlyPropertyReader<>(DataType.float32(), nZProp);
                    GeometryAttribute normalAttribute = new GeometryAttribute();
                    normalAttribute.init(GeometryAttribute.Type.NORMAL, null, 3, DracoDataType.FLOAT32, false);
                    int normalAttributeId = outPointCloud.addAttribute(normalAttribute, true, numVertices);
                    for (int i = 0; i < numVertices; i++) {
                        float[] val = new float[3];
                        val[0] = xReader.readValue(i);
                        val[1] = yReader.readValue(i);
                        val[2] = zReader.readValue(i);
                        Pointer<Float> pointer = Pointer.wrap(val);
                        outPointCloud.getAttribute(normalAttributeId).setAttributeValue(AttributeValueIndex.of(i), pointer);
                    }
                }
            }
        }

        int numColors = 0;
        PlyProperty rProp = vertexElement.getPropertyByName("red");
        PlyProperty gProp = vertexElement.getPropertyByName("green");
        PlyProperty bProp = vertexElement.getPropertyByName("blue");
        PlyProperty aProp = vertexElement.getPropertyByName("alpha");
        if(rProp != null) numColors++;
        if(gProp != null) numColors++;
        if(bProp != null) numColors++;
        if(aProp != null) numColors++;

        if(numColors > 0) {
            List<PlyPropertyReader<UByte>> colorReaders = new ArrayList<>();
            if(rProp != null) {
                if(rProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'red' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), rProp));
            }
            if(gProp != null) {
                if(gProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'green' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), gProp));
            }
            if(bProp != null) {
                if(bProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'blue' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), bProp));
            }
            if(aProp != null) {
                if(aProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'alpha' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), aProp));
            }

            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.COLOR, null, UByte.of(numColors), DracoDataType.UINT8, true, 4 * numColors, 0);
            int attId = outPointCloud.addAttribute(va, true, numVertices);
            for(int i = 0; i < numVertices; i++) {
                Pointer<UByte> val = Pointer.newUByteArray(4);
                for(int j = 0; j < numColors; j++) {
                    val.set(j, colorReaders.get(j).readValue(i));
                }
                outPointCloud.getAttribute(attId).setAttributeValue(AttributeValueIndex.of(i), val);
            }
        }

        return Status.ok();
    }

    private <T> void readPropertiesToAttribute(DataNumberType<T> type, List<PlyProperty> properties,
                                               PointAttribute attribute, int numVertices) {
        List<PlyPropertyReader<T>> readers = new ArrayList<>(properties.size());
        for(PlyProperty property : properties) {
            readers.add(new PlyPropertyReader<>(type, property));
        }
        CppVector<T> memory = new CppVector<>(type, properties.size());
        for(int i = 0; i < numVertices; i++) {
            for(int prop = 0; prop < properties.size(); prop++) {
                memory.set(prop, readers.get(prop).readValue(i));
            }
            attribute.setAttributeValue(AttributeValueIndex.of(i), memory.getPointer());
        }
    }

    private static long countNumTriangles(PlyElement faceElement, PlyProperty vertexIndices) {
        long numTriangles = 0;
        for (int i = 0; i < faceElement.getNumEntries(); ++i) {
            long listSize = vertexIndices.getListEntryNumValues(i);
            if (listSize < 3) continue;
            numTriangles += listSize - 2;
        }
        return numTriangles;
    }

}
