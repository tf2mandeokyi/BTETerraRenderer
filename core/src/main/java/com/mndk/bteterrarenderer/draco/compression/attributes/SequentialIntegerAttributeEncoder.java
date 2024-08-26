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

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeEncoderFactory;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeTypedEncoderInterface;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeWrapEncodingTransform;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.config.SequentialAttributeEncoderType;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolEncoding;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;

public class SequentialIntegerAttributeEncoder extends SequentialAttributeEncoder {

    private PSchemeTypedEncoderInterface<Integer, Integer> predictionScheme = null;

    @Override
    public UByte getUniqueId() {
        return UByte.of(SequentialAttributeEncoderType.INTEGER.getValue());
    }

    @Override
    public Status init(PointCloudEncoder encoder, int attributeId) {
        StatusChain chain = new StatusChain();

        if(super.init(encoder, attributeId).isError(chain)) return chain.get();

        if(this.getUniqueId().equals(SequentialAttributeEncoderType.INTEGER.getValue())) {
            switch(this.getAttribute().getDataType()) {
                case INT8:  case UINT8:
                case INT16: case UINT16:
                case INT32: case UINT32:
                    break;
                default:
                    return Status.dracoError("Invalid data type for integer attribute encoder");
            }
        }

        // Init prediction scheme.
        PredictionSchemeMethod predictionSchemeMethod =
                PSchemeEncoderFactory.getPredictionMethodFromOptions(attributeId, encoder.getOptions());
        this.predictionScheme = this.createIntPredictionScheme(predictionSchemeMethod);
        if(predictionScheme != null && this.initPredictionScheme(predictionScheme).isError()) {
            predictionScheme = null;
        }
        return Status.ok();
    }

    @Override
    public Status transformAttributeToPortableFormat(CppVector<PointIndex> pointIds) {
        StatusChain chain = new StatusChain();

        if(this.getEncoder() != null) {
            if(this.prepareValues(pointIds, this.getEncoder().getPointCloud().getNumPoints()).isError(chain))
                return chain.get();
        } else {
            if(this.prepareValues(pointIds, 0).isError(chain))
                return chain.get();
        }

        // Update point to attribute mapping with the portable attribute if the
        // attribute is a parent attribute.
        if(this.isParentEncoder()) {
            PointAttribute origAtt = this.getAttribute();
            PointAttribute portableAtt = this.getPortableAttributeInternal();
            IndexTypeVector<AttributeValueIndex, AttributeValueIndex> valueToValueMap =
                    new IndexTypeVector<>(AttributeValueIndex.type(), origAtt.size());
            for(int i = 0; i < pointIds.size(); ++i) {
                valueToValueMap.set(origAtt.getMappedIndex(pointIds.get(i)), AttributeValueIndex.of(i));
            }
            if(portableAtt.isMappingIdentity()) {
                portableAtt.setExplicitMapping(this.getEncoder().getPointCloud().getNumPoints());
            }
            for(PointIndex i : PointIndex.range(0, this.getEncoder().getPointCloud().getNumPoints())) {
                portableAtt.setPointMapEntry(i, valueToValueMap.get(origAtt.getMappedIndex(i)));
            }
        }
        return Status.ok();
    }

    @Override
    protected Status encodeValues(CppVector<PointIndex> pointIds, EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        PointAttribute attrib = this.getAttribute();
        if(attrib.size() == 0) return Status.ok();

        PredictionSchemeMethod predictionSchemeMethod = PredictionSchemeMethod.NONE;
        if(predictionScheme != null) {
            if(this.setPredictionSchemeParentAttributes(predictionScheme).isError(chain)) return chain.get();
            predictionSchemeMethod = predictionScheme.getPredictionMethod();
        }
        outBuffer.encode(UByte.of(predictionSchemeMethod.getValue()));
        if(predictionScheme != null) {
            outBuffer.encode(UByte.of(predictionScheme.getTransformType().getValue()));
        }

        int numComponents = this.getPortableAttributeInternal().getNumComponents().intValue();
        int numValues = numComponents * this.getPortableAttributeInternal().size();
        Pointer<Integer> portableAttributeData = this.getPortableAttributeData();

        // We store and process all encoded data in a separate array to preserve the portable data.
        CppVector<Integer> encodedData = new CppVector<>(DataType.int32(), numValues);

        // All integer values are initialized.
        if(predictionScheme != null) {
            if(predictionScheme.computeCorrectionValues(portableAttributeData, encodedData.getPointer(),
                    numValues, numComponents, pointIds.getPointer()).isError(chain)) {
                return chain.get();
            }
        }

        if(predictionScheme == null || !predictionScheme.areCorrectionsPositive()) {
            Pointer<Integer> input = predictionScheme != null ? encodedData.getPointer() : portableAttributeData;
            BitUtils.convertSignedIntsToSymbols(input, numValues, encodedData.getPointer().asRawToUInt());
        }

        if(this.getEncoder() == null || this.getEncoder().getOptions().getGlobalBool(
                "use_built_in_attribute_compression", true)) {
            outBuffer.encode(UByte.of(1));
            Options symbolEncodingOptions = new Options();
            if(this.getEncoder() != null) {
                SymbolEncoding.setSymbolEncodingCompressionLevel(symbolEncodingOptions,
                        10 - this.getEncoder().getOptions().getSpeed());
            }
            if(SymbolEncoding.encode(
                    encodedData.getPointer().asRawToUInt(), (int) pointIds.size() * numComponents, numComponents,
                    symbolEncodingOptions, outBuffer).isError(chain)) {
                return chain.get();
            }
        }
        else {
            // No compression.
            // To compute the maximum bit-length, first OR all values.
            int maskedValue = 0;
            for(int i = 0; i < numValues; ++i) {
                maskedValue |= encodedData.get(i);
            }
            // Compute the msb of the ORed value.
            int valueMsbPos = 0;
            if(maskedValue != 0) {
                valueMsbPos = BitUtils.mostSignificantBit(DataType.uint32(), UInt.of(maskedValue));
            }
            int numBytes = 1 + valueMsbPos / 8;

            outBuffer.encode(UByte.of(0));
            outBuffer.encode(UByte.of(numBytes));

            if(numBytes == DracoDataType.INT32.getDataTypeLength()) {
                outBuffer.encode(encodedData.getPointer(), numValues);
            } else {
                if(this.encodeCustomNumberValues(numBytes, encodedData, outBuffer).isError(chain)) return chain.get();
            }
        }
        if(predictionScheme != null) {
            if(predictionScheme.encodePredictionData(outBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    private <T> Status encodeCustomNumberValues(int numBytes, CppVector<Integer> encodedData, EncoderBuffer outBuffer) {
        DataNumberType<T> storeType;
        switch(numBytes) {
            case 1: storeType = BTRUtil.uncheckedCast(DataType.uint8()); break;
            case 2: storeType = BTRUtil.uncheckedCast(DataType.uint16()); break;
            case 4: storeType = BTRUtil.uncheckedCast(DataType.uint32()); break;
            default: return Status.dracoError("Invalid number of bytes: " + numBytes);
        }
        for(int i = 0; i < encodedData.size(); ++i) {
            outBuffer.encode(storeType, storeType.from(encodedData.get(i)));
        }
        return Status.ok();
    }

    protected PSchemeTypedEncoderInterface<Integer, Integer>
    createIntPredictionScheme(PredictionSchemeMethod method) {
        int id = this.getAttributeId();
        PointCloudEncoder encoder = this.getEncoder();
        return PSchemeEncoderFactory.createPredictionSchemeForEncoder(
                method, id, encoder, new PSchemeWrapEncodingTransform<>(DataType.int32(), DataType.int32()));
    }

    protected Status prepareValues(CppVector<PointIndex> pointIds, int numPoints) {
        PointAttribute attrib = this.getAttribute();
        int numComponents = attrib.getNumComponents().intValue();
        int numEntries = (int) pointIds.size();
        this.preparePortableAttribute(numEntries, numComponents, numPoints);
        int dstIndex = 0;
        Pointer<Integer> portableAttributeData = this.getPortableAttributeData();
        for(PointIndex pi : pointIds) {
            AttributeValueIndex attId = attrib.getMappedIndex(pi);
            Status status = attrib.convertValue(attId, portableAttributeData.add(dstIndex));
            if(status.isError()) return status;
            dstIndex += numComponents;
        }
        return Status.ok();
    }

    protected void preparePortableAttribute(int numEntries, int numComponents, int numPoints) {
        GeometryAttribute va = new GeometryAttribute();
        DracoDataType type = DracoDataType.INT32;
        va.init(this.getAttribute().getAttributeType(), null, numComponents, type, false);
        PointAttribute portableAtt = new PointAttribute(va);
        portableAtt.reset(numEntries);
        this.setPortableAttribute(portableAtt);
        if(numPoints != 0) {
            portableAtt.setExplicitMapping(numPoints);
        }
    }

    protected Pointer<Integer> getPortableAttributeData() {
        return this.getPortableAttributeInternal().getAddress(AttributeValueIndex.of(0)).toInt();
    }
}
