package com.mndk.kmap4bte.projection;

import com.mndk.kmap4bte.projection.terra121.ModifiedAirocean;
import com.mndk.kmap4bte.projection.wtm.WTMProjection;

public class Projections {
    public static final GeographicProjection BTE = new ModifiedAirocean();
    public static final GeographicProjection WTM = new WTMProjection();
}
