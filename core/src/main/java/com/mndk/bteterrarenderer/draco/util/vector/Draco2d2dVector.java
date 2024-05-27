package com.mndk.bteterrarenderer.draco.util.vector;

public class Draco2d2dVector<E> extends Draco2dVector<Draco2dVector<E>> {

    public Draco2d2dVector() {
        super(Draco2dVector::new);
    }

}
