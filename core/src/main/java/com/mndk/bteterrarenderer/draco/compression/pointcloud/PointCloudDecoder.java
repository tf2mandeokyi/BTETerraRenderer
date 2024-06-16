package com.mndk.bteterrarenderer.draco.compression.pointcloud;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.attributes.AttributesDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.config.DecoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.DracoHeader;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.core.DataType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.metadata.GeometryMetadata;
import com.mndk.bteterrarenderer.draco.metadata.MetadataDecoder;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PointCloudDecoder {

    private static final String IO_ERROR_MSG = "Failed to parse Draco header.";

    @Getter
    private PointCloud pointCloud;
    private final List<AttributesDecoderInterface> attributesDecoders = new ArrayList<>();
    private final List<Integer> attributeToDecoderMap = new ArrayList<>();
    @Getter
    private DecoderBuffer buffer;
    private short versionMajor;
    private short versionMinor;
    @Getter
    private DecoderOptions options;

    public PointCloudDecoder() {
        this.pointCloud = null;
        this.buffer = null;
        this.versionMajor = 0;
        this.versionMinor = 0;
        this.options = null;
    }

    public EncodedGeometryType getGeometryType() {
        return EncodedGeometryType.POINT_CLOUD;
    }

    public static Status decodeHeader(DecoderBuffer buffer, DracoHeader outHeader) {
        StatusChain chain = Status.newChain();
        if (buffer.decode(DataType.string(5), outHeader::setDracoString).isError(chain)) return chain.get();
        if (!outHeader.getDracoString().equals("DRACO")) {
            return new Status(Status.Code.DRACO_ERROR, "Not a Draco file.");
        }
        if (buffer.decode(DataType.UINT8, outHeader::setVersionMajor).isError(chain)) return chain.get();
        if (buffer.decode(DataType.UINT8, outHeader::setVersionMinor).isError(chain)) return chain.get();
        if (buffer.decode(DataType.UINT8, outHeader::setEncoderType).isError(chain)) return chain.get();
        if (buffer.decode(DataType.UINT8, outHeader::setEncoderMethod).isError(chain)) return chain.get();
        if (buffer.decode(DataType.UINT16, outHeader::setFlags).isError(chain)) return chain.get();
        return Status.OK;
    }

    protected Status decodeMetadata() {
        StatusChain chain = Status.newChain();

        GeometryMetadata metadata = new GeometryMetadata();
        MetadataDecoder metadataDecoder = new MetadataDecoder();
        if(metadataDecoder.decodeGeometryMetadata(buffer, metadata).isError(chain)) return chain.get();
        pointCloud.addMetadata(metadata);
        return Status.OK;
    }

    public Status decode(DecoderOptions options, DecoderBuffer inBuffer, PointCloud outPointCloud) {
        StatusChain chain = Status.newChain();

        this.options = options;
        this.buffer = inBuffer;
        this.pointCloud = outPointCloud;
        DracoHeader header = new DracoHeader();
        if (decodeHeader(buffer, header).isError(chain)) return chain.get();
        // Sanity check that we are really using the right decoder (mostly for cases
        // where the Decode method was called manually outside our main API)
        if (header.getEncoderType() != getGeometryType().getValue()) {
            return new Status(Status.Code.DRACO_ERROR, "Using incompatible decoder for the input geometry.");
        }
        versionMajor = header.getVersionMajor();
        versionMinor = header.getVersionMinor();

        short maxSupportedMajorVersion = header.getEncoderType() == EncodedGeometryType.POINT_CLOUD.getValue() ?
                DracoVersions.POINT_CLOUD_BIT_STREAM_VERSION_MAJOR : DracoVersions.MESH_BIT_STREAM_VERSION_MAJOR;
        short maxSupportedMinorVersion = header.getEncoderType() == EncodedGeometryType.POINT_CLOUD.getValue() ?
                DracoVersions.POINT_CLOUD_BIT_STREAM_VERSION_MINOR : DracoVersions.MESH_BIT_STREAM_VERSION_MINOR;

        // Check for version compatibility
        if (versionMajor < 1 || versionMajor > maxSupportedMajorVersion) {
            return new Status(Status.Code.UNKNOWN_VERSION, "Unknown major version.");
        }
        if (versionMajor == maxSupportedMajorVersion && versionMinor > maxSupportedMinorVersion) {
            return new Status(Status.Code.UNKNOWN_VERSION, "Unknown minor version.");
        }

        buffer.setBitstreamVersion(DracoVersions.getBitstreamVersion(versionMajor, versionMinor));

        if (this.getBitstreamVersion() >= DracoVersions.getBitstreamVersion(1, 3) &&
                (header.getFlags() & DracoHeader.METADATA_FLAG_MASK) != 0) {
            if (decodeMetadata().isError(chain)) return chain.get();
        }
        if (initializeDecoder().isError(chain)) return chain.get();
        if (decodeGeometryData().isError(chain)) return chain.get();
        if (decodePointAttributes().isError(chain)) return chain.get();
        return Status.OK;
    }

    public Status setAttributesDecoder(int attDecoderId, AttributesDecoderInterface decoder) {
        if (attDecoderId < 0) return new Status(Status.Code.INVALID_PARAMETER, "Invalid attribute decoder id.");
        if (attDecoderId >= attributesDecoders.size()) {
            attributesDecoders.add(attDecoderId, decoder);
        } else {
            attributesDecoders.set(attDecoderId, decoder);
        }
        return Status.OK;
    }

    public PointAttribute getPortableAttribute(int parentAttId) {
        if (parentAttId < 0 || parentAttId >= pointCloud.getNumAttributes()) return null;
        int parentAttDecoderId = attributeToDecoderMap.get(parentAttId);
        return attributesDecoders.get(parentAttDecoderId).getPortableAttribute(parentAttId);
    }

    public int getBitstreamVersion() {
        return DracoVersions.getBitstreamVersion(versionMajor, versionMinor);
    }

    public AttributesDecoderInterface getAttributesDecoder(int decId) {
        return attributesDecoders.get(decId);
    }
    public int getNumAttributesDecoders() {
        return attributesDecoders.size();
    }

    /**
     * Can be implemented by derived classes to perform any custom initialization
     * of the decoder. Called in the {@link #decode} method.
     */
    protected Status initializeDecoder() { return Status.OK; }

    /** Creates an attribute decoder. */
    protected abstract Status createAttributesDecoder(int attDecoderId);
    protected Status decodeGeometryData() { return Status.OK; }
    protected Status decodePointAttributes() {
        StatusChain chain = Status.newChain();

        AtomicReference<Short> numAttributesDecoders = new AtomicReference<>((short) 0);
        if (buffer.decode(DataType.UINT8, numAttributesDecoders::set).isError(chain)) return chain.get();
        // Create all attribute decoders. This is implementation specific and the
        // derived classes can use any data encoded in the
        // PointCloudEncoder::EncodeAttributesEncoderIdentifier() call.
        for (int i = 0; i < numAttributesDecoders.get(); ++i) {
            if(createAttributesDecoder(i).isError(chain)) return chain.get();
        }

        // Initialize all attributes decoders. No data is decoded here.
        for (AttributesDecoderInterface attDec : attributesDecoders) {
            if(attDec.init(this, pointCloud).isError(chain)) return chain.get();
        }

        // Decode any data needed by the attribute decoders.
        for (int i = 0; i < numAttributesDecoders.get(); ++i) {
            if(attributesDecoders.get(i).decodeAttributesDecoderData(buffer).isError(chain)) return chain.get();
        }

        // Create map between attribute and decoder ids.
        for (int i = 0; i < numAttributesDecoders.get(); ++i) {
            int numAttributes = attributesDecoders.get(i).getNumAttributes();
            for (int j = 0; j < numAttributes; ++j) {
                int attId = attributesDecoders.get(i).getAttributeId(j);
                if (attId >= attributeToDecoderMap.size()) {
                    attributeToDecoderMap.add(attId, i);
                } else {
                    attributeToDecoderMap.set(attId, i);
                }
            }
        }

        // Decode the actual attributes using the created attribute decoders.
        if (decodeAllAttributes().isError(chain)) return chain.get();
        return onAttributesDecoded();
    }

    protected Status decodeAllAttributes() {
        StatusChain chain = Status.newChain();
        for (AttributesDecoderInterface attDec : attributesDecoders) {
            if (attDec.decodeAttributes(buffer).isError(chain)) return chain.get();
        }
        return Status.OK;
    }
    protected Status onAttributesDecoded() { return Status.OK; }
}
