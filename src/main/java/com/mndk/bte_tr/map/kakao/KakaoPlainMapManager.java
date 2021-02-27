package com.mndk.bte_tr.map.kakao;

import com.mndk.bte_tr.map.RenderMapSource;

public class KakaoPlainMapManager extends KakaoMapManager {
    public KakaoPlainMapManager() {
        super(RenderMapSource.KAKAO_AERIAL);
    }
    
    @Override
    public String getUrlTemplate(int tileX, int tileY, int zoom) {
        if(domain_num >= 3) domain_num = -1;
        domain_num++;

        return "http://map" + domain_num + ".daumcdn.net/map_2d/2012tlq" +
                "/L" + zoom + "/" + tileY + "/" + tileX + ".png";
    }

}
