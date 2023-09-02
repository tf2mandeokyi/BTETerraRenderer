package com.mndk.bteterrarenderer.ogc3dtiles;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.net.URL;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class TileData {

    private final TileDataFormat dataFormat;
    @Nullable
    private TileData parent;
    @Nullable
    private Matrix4 parentLocalTransform;
    @Nullable
    private URL url;

    public Matrix4 getTrueTransform() {
        Matrix transform = parentLocalTransform;
        if(transform == null) transform = Matrix4.IDENTITY;

        TileData parent = this.parent;
        while(parent != null) {
            if(parent.parentLocalTransform != null) {
                transform = transform.multiply(parent.parentLocalTransform);
            }
            parent = parent.parent;
        }

        return transform.toMatrix4();
    }

}