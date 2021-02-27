package com.mndk.bte_tr.map.kakao;

import com.mndk.bte_tr.map.RenderMapSource;

public class KakaoAerialMapManager extends KakaoMapManager {
    public KakaoAerialMapManager() {
        super(RenderMapSource.KAKAO_PLAIN);
    }

    @Override
    public String getUrlTemplate(int tileX, int tileY, int zoom) {
        if(domain_num >= 3) domain_num = -1;
        domain_num++;

        return "http://map" + domain_num + ".daumcdn.net/map_skyview/" +
                "L" + zoom + "/" + tileY + "/" + tileX + ".jpg";
    }

}
