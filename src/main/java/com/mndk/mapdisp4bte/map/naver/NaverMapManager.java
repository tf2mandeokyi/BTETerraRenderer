package com.mndk.mapdisp4bte.map.naver;

import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.mercator.MercatorMapManager;

public class NaverMapManager extends MercatorMapManager {

    public NaverMapManager() {
        super(RenderMapSource.NAVER,
                "https://map.pstatic.net/nrb/styles/basic/{z}/{x}/{y}.png?mt=bg.ol.ts.lko",
                "https://map.pstatic.net/nrb/styles/satellite/{z}/{x}/{y}.png?mt=bg.ol.ts.lko",
                2);
    }

    @Override
    protected int getZoomFromLevel(int level) {
        return 20 - level;
    }

}
