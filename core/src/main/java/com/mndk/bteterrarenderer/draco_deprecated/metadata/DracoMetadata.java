package com.mndk.bteterrarenderer.draco_deprecated.metadata;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DracoMetadata {
//    private final Map<Integer, Element> attributeMetadataMap;
//    private final Element fileMetadata;
//
//    /**
//     * @implNote Draco 4.1: <a href="https://google.github.io/draco/spec/#decodemetadata">
//     *     DecodeMetadata</a>
//     */
//    public static DracoMetadata decode(ByteBuf buf) {
//        Map<Integer, Element> att_metadata = new HashMap<>();
//        // Draco 4.2: ParseMetadataCount {
//        int num_att_metadata = (int) BitUtils.readBase128LE(buf);
//        // }
//        for(int i = 0; i < num_att_metadata; i++) {
//            // Draco 4.3: ParseAttributeMetadataId {
//            int att_metadata_id = (int) BitUtils.readBase128LE(buf);
//            // }
//            Element element = Element.decode(buf);
//            att_metadata.put(att_metadata_id, element);
//        }
//        Element file_metadata = Element.decode(buf);
//        return new DracoMetadata(att_metadata, file_metadata);
//    }
//
//    @RequiredArgsConstructor
//    public static class Element {
//        private final Map<String, String> elementMap;
//        private final Map<String, Element> subMetadataMap;
//
//        /**
//         * @implNote Draco 4.6: <a href="https://google.github.io/draco/spec/#decodemetadataelement">
//         *     DecodeMetadataElement</a>
//         */
//        private static Element decode(ByteBuf buf) {
//            Map<String, String> map = new HashMap<>();
//            Map<String, Element> sub_metadata = new HashMap<>();
//
//            // Draco 4.4: ParseMetadataElement {
//            int num_entries = (int) BitUtils.readBase128LE(buf);
//            for(int i = 0; i < num_entries; i++) {
//                short key_size = buf.readUnsignedByte();
//                String key = buf.readBytes(key_size).toString();
//                short value_size = buf.readUnsignedByte();
//                String value = buf.readBytes(value_size).toString();
//                map.put(key, value);
//            }
//            int num_sub_metadata = (int) BitUtils.readBase128LE(buf);
//            // }
//
//            for(int i = 0; i < num_sub_metadata; i++) {
//                // Draco 4.5: ParseSubMetadataKey {
//                short sub_metadata_key_size = buf.readUnsignedByte();
//                String sub_metadata_key = buf.readBytes(sub_metadata_key_size).toString();
//                // }
//                Element element = decode(buf);
//                sub_metadata.put(sub_metadata_key, element);
//            }
//
//            return new Element(map, sub_metadata);
//        }
//    }
}
