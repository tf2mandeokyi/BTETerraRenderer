package com.mndk.bteterrarenderer.draco_deprecated.util;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FaceToVertex {

    private final List<List<Integer>> map = new ArrayList<List<Integer>>() {{
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
    }};

    /**
     * @implNote Draco 7.8: <a href="https://google.github.io/draco/spec/#replaceverts">
     *     ReplaceVerts</a>
     */
    public void replaceVerts(int from, int to) {
        for (int i = 0; i < this.size(); ++i) {
            if(this.get(0, i) == from) this.set(0, i, to);
            if(this.get(1, i) == from) this.set(1, i, to);
            if(this.get(2, i) == from) this.set(2, i, to);
        }
    }

    /**
     * Note that Draco 24.10: <a href="https://google.github.io/draco/spec/#cornertovert">CornerToVert</a> can be
     * implemented with this method by using like {@code cornerToVerts()[0]}.<br>
     * Draco 24.12: <a href="https://google.github.io/draco/spec/#cornertoverts">CornerToVerts</a> (att_dec = 0)<br>
     * @implNote Draco 24.11: <a href="https://google.github.io/draco/spec/#cornertovertsinternal">CornerToVertsInternal</a>
     */
    public int[] cornerToVertsInternal(int cornerId) {
        int local = cornerId % 3;
        int face = cornerId / 3;
        if     (local == 0) return new int[] { this.get(0, face), this.get(1, face), this.get(2, face) };
        else if(local == 1) return new int[] { this.get(1, face), this.get(2, face), this.get(0, face) };
        else if(local == 2) return new int[] { this.get(2, face), this.get(0, face), this.get(1, face) };
        else throw new RuntimeException();
    }

    public int size() {
        return map.get(0).size();
    }

    public int get(int i, int face) {
        return this.map.get(i).get(face);
    }

    public void set(int i, int face, int value) {
        this.map.get(i).set(face, value);
    }

    public void set(int face, int value0, int value1, int value2) {
        this.map.get(0).set(face, value0);
        this.map.get(1).set(face, value1);
        this.map.get(2).set(face, value2);
    }

    public void add(int value0, int value1, int value2) {
        this.map.get(0).add(value0);
        this.map.get(1).add(value1);
        this.map.get(2).add(value2);
    }
}
