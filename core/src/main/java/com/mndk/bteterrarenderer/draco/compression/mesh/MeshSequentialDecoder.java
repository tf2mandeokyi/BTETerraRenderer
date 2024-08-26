package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.AttributesDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.attributes.LinearSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.SequentialAttributeDecodersController;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolDecoding;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;

public class MeshSequentialDecoder extends MeshDecoder {
    @Override
    protected Status decodeConnectivity() {
        StatusChain chain = new StatusChain();

        Pointer<UInt> numFacesRef = Pointer.newUInt();
        Pointer<UInt> numPointsRef = Pointer.newUInt();
        if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(this.getBuffer().decode(numFacesRef).isError(chain)) return chain.get();
            if(this.getBuffer().decode(numPointsRef).isError(chain)) return chain.get();
        } else {
            if(this.getBuffer().decodeVarint(numFacesRef).isError(chain)) return chain.get();
            if(this.getBuffer().decodeVarint(numPointsRef).isError(chain)) return chain.get();
        }
        UInt numFaces = numFacesRef.get();
        UInt numPoints = numPointsRef.get();

        // Check that num_faces and num_points are valid values.
        // Compressed sequential encoding can only handle (2^32 - 1) / 3 indices.
        if(numFaces.gt(0xffffffffL / 3)) {
            return Status.ioError("Number of faces is too high.");
        }
        if(numFaces.gt(this.getBuffer().getRemainingSize() / 3)) {
            // The number of faces is unreasonably high, because face indices do not
            // fit in the remaining size of the buffer.
            return Status.ioError("Number of faces is too high.");
        }
        Pointer<UByte> connectivityMethodRef = Pointer.newUByte();
        if(this.getBuffer().decode(connectivityMethodRef).isError(chain)) return chain.get();
        UByte connectivityMethod = connectivityMethodRef.get();

        if(connectivityMethod.equals(0)) {
            if(this.decodeAndDecompressIndices(numFaces).isError(chain)) return chain.get();
            if(numPoints.lt(256)) {
                // Decode indices as uint8.
                for (int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for (int j = 0; j < 3; ++j) {
                        Pointer<UByte> val = Pointer.newUByte();
                        if(this.getBuffer().decode(val).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
            else if(numPoints.lt(1 << 16)) {
                // Decode indices as UINT16.
                for(int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for(int j = 0; j < 3; ++j) {
                        Pointer<UShort> val = Pointer.newUShort();
                        if(this.getBuffer().decode(val).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
            else if(numPoints.lt(1 << 21) &&
                    this.getBitstreamVersion() >= DracoVersions.getBitstreamVersion(2, 2)) {
                // Decode indices as uint32.
                for(int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for(int j = 0; j < 3; ++j) {
                        Pointer<UInt> val = Pointer.newUInt();
                        if(this.getBuffer().decodeVarint(val).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
            else {
                // Decode faces as uint32 (default).
                for(int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for(int j = 0; j < 3; ++j) {
                        Pointer<UInt> val = Pointer.newUInt();
                        if(this.getBuffer().decode(val).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
        }
        this.getPointCloud().setNumPoints(numPoints.intValue());
        return Status.ok();
    }

    @Override
    protected Status createAttributesDecoder(int attDecoderId) {
        // Always create the basic attribute decoder.
        PointsSequencer sequencer = new LinearSequencer(this.getPointCloud().getNumPoints());
        AttributesDecoderInterface decoder = new SequentialAttributeDecodersController(sequencer);
        return this.setAttributesDecoder(attDecoderId, decoder);
    }

    private Status decodeAndDecompressIndices(UInt numFaces) {
        StatusChain chain = new StatusChain();

        // Get decoded indices differences that were encoded with an entropy code.
        Pointer<UInt> indicesBuffer = Pointer.wrapUnsigned(new int[numFaces.intValue() * 3]);
        Status status = SymbolDecoding.decode(numFaces.mul(3), 1, this.getBuffer(), indicesBuffer);
        if(status.isError(chain)) return chain.get();

        // Reconstruct the indices from the differences.
        int lastIndexValue = 0;
        int vertexIndex = 0;
        for(UInt i = UInt.ZERO; i.lt(numFaces); i = i.add(1)) {
            Mesh.Face face = new Mesh.Face();
            for(int j = 0; j < 3; ++j) {
                UInt encodedVal = indicesBuffer.get(vertexIndex++);
                int indexDiff = encodedVal.shr(1).intValue();
                if(encodedVal.and(1).equals(1)) {
                    if(indexDiff > lastIndexValue) {
                        return Status.ioError("Index diff is too high.");
                    }
                    indexDiff = -indexDiff;
                } else {
                    if(indexDiff > Integer.MAX_VALUE - lastIndexValue) {
                        return Status.ioError("Index diff is too high.");
                    }
                }
                int indexValue = indexDiff + lastIndexValue;
                face.set(j, PointIndex.of(indexValue));
                lastIndexValue = indexValue;
            }
            this.getMesh().addFace(face);
        }
        return Status.ok();
    }
}
