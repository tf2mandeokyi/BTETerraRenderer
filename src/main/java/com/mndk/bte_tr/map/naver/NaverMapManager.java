package com.mndk.bte_tr.map.naver;

import com.mndk.bte_tr.map.RenderMapSource;
import com.mndk.bte_tr.map.mercator.MercatorMapManager;

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
