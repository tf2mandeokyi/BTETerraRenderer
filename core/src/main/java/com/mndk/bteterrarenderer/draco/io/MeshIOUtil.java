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

import com.mndk.bteterrarenderer.draco.compression.DracoDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Options;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.StatusOr;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@UtilityClass
public class MeshIOUtil {

    public static StatusOr<Mesh> decode(File file) {
        return decode(file, true);
    }

    public static StatusOr<Mesh> decode(File file, boolean deduplicateInputValues) {
        Options options = new Options();
        options.setBool("deduplicate_input_values", deduplicateInputValues);
        return decode(file, options);
    }

    public static StatusOr<Mesh> decodeWithMetadata(File file) {
        Options options = new Options();
        options.setBool("use_metadata", true);
        return decode(file, options);
    }

    public static StatusOr<Mesh> decodeWithPolygons(File file) {
        Options options = new Options();
        options.setBool("preserve_polygons", true);
        return decode(file, options);
    }

    public StatusOr<Mesh> decode(File file, Options options) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        MeshFileFormat format = MeshFileFormat.fromExtension(extension);
        if (format == null) return StatusOr.invalidParameter("Unsupported file format: " + extension);
        return decode(file, format, options);
    }

    public StatusOr<Mesh> decode(File file, MeshFileFormat format, Options options) {
        StatusChain chain = new StatusChain();
        Mesh mesh = new Mesh();

        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        switch (format) {
            case OBJ: {
                ObjDecoder decoder = new ObjDecoder();
                decoder.setUseMetadata(options.getBool("use_metadata", false));
                decoder.setPreservePolygons(options.getBool("preserve_polygons"));
                decoder.setDeduplicateInputValues(options.getBool("deduplicate_input_values"));

                if (decoder.decodeFromFile(file, mesh).isError(chain)) return StatusOr.error(chain.get());
                return StatusOr.ok(mesh);
            }
            case PLY: {
                PlyDecoder decoder = new PlyDecoder();
                if (decoder.decodeFromFile(file, mesh).isError(chain)) return StatusOr.error(chain.get());
                return StatusOr.ok(mesh);
            }
            case DRACO: {
                DecoderBuffer decoderBuffer = new DecoderBuffer();
                try (InputStream stream = Files.newInputStream(file.toPath())) {
                    decoderBuffer.init(stream);
                } catch (IOException e) {
                    return StatusOr.ioError(e.getMessage(), e);
                }
                DracoDecoder decoder = new DracoDecoder();
                return decoder.decodeMeshFromBuffer(decoderBuffer);
            }
            default: return StatusOr.invalidParameter("Unsupported file format: " + extension);
        }
    }
}
