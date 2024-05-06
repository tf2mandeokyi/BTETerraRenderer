package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.mndk.bteterrarenderer.ogc3dtiles.Ogc3dTiles;
import de.javagl.jgltf.model.ModelElement;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.util.Map;

@UtilityClass
public class GltfExtensionsUtil {
    private <T> T getExtension(ModelElement modelElement, String extensionName, Class<T> clazz) {
        Map<String, Object> extensions = modelElement.getExtensions();
        if(extensions == null || !extensions.containsKey(extensionName)) return null;
        Object result = extensions.get(extensionName);
        if(result == null) return null;
        return Ogc3dTiles.jsonMapper().convertValue(result, clazz);
    }

    /**
     * Gets the extension from the gltf model element
     * @param clazz {@link GltfExtension} annotated class type
     * @return The extension object. {@code null} if not exists
     */
    @Nullable
    public <T> T getExtension(ModelElement modelElement, Class<T> clazz) {
        GltfExtension annotation = clazz.getAnnotation(GltfExtension.class);
        if(annotation == null) throw new RuntimeException("gltf extension not annotated for class " + clazz);
        return getExtension(modelElement, annotation.value(), clazz);
    }
}
