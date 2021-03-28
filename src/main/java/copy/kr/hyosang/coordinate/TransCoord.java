package copy.kr.hyosang.coordinate;

/* Copied from https://hyosang82.tistory.com/269 */

@Deprecated
public class TransCoord
{
	public static final int COORD_TYPE_TM = 1;
	public static final int COORD_TYPE_KTM = 2;
	public static final int COORD_TYPE_UTM = 3;
	public static final int COORD_TYPE_CONGNAMUL = 4;
	public static final int COORD_TYPE_WGS84 = 5;
	public static final int COORD_TYPE_BESSEL = 6;
	public static final int COORD_TYPE_WTM = 7;
	public static final int COORD_TYPE_WKTM = 8;
	public static final int COORD_TYPE_WCONGNAMUL = 10;
	public static final double BASE_TM_LON = 127.0;
	public static final double BASE_TM_LAT = 38.0;
	public static final double BASE_KTM_LON = 128.0;
	public static final double BASE_KTM_LAT = 38.0;
	public static final double BASE_UTM_LON = 129.0;
	public static final double BASE_UTM_LAT = 0.0;
	private static final int[][] COORD_BASE;

	static {
		COORD_BASE = new int[][] { new int[2], { 127, 38 }, { -1, -1 }, { 129, 0 }, { -1, -1 }, { -1, -1 }, { -1, -1 }, { 127, 38 }, { -1, -1 }, new int[2], { -1, -1 } };
	}

	public static CoordPoint getTransCoord(final CoordPoint inPoint, final int fromType, final int toType) {
		return convertCoord(inPoint, fromType, toType, TransCoord.COORD_BASE[fromType][0], TransCoord.COORD_BASE[fromType][1], TransCoord.COORD_BASE[toType][0], TransCoord.COORD_BASE[toType][1]);
	}

	private static CoordPoint convertCoord(final CoordPoint point, final int fromType, final int toType, final double frombx, double fromby, final double tobx, final double toby) {
		CoordPoint transPt = null;
		double bx = frombx;
		switch (fromType) {
			case 1: {
				if (frombx <= 0.0) {
					bx = 127.0;
					fromby = 38.0;
				}
				transPt = convertTM2(point, toType, bx, fromby, tobx, toby);
				break;
			}
			case 2: {
				if (frombx <= 0.0) {
					bx = 128.0;
					fromby = 38.0;
				}
				transPt = convertKTM2(point, toType, tobx, toby);
				break;
			}
			case 3: {
				if (frombx <= 0.0) {
					bx = 129.0;
					fromby = 0.0;
				}
				transPt = convertUTM2(point, toType, bx, fromby, tobx, toby);
				break;
			}
			case 4: {
				if (frombx <= 0.0) {
					bx = 127.0;
					fromby = 38.0;
				}
				transPt = convertCONGNAMUL2(point, toType, bx, fromby, tobx, toby);
				break;
			}
			case 5: {
				transPt = convertWGS2(point, toType, bx, fromby, tobx, toby);
				break;
			}
			case 6: {
				transPt = convertBESSEL2(point, toType, bx, fromby, tobx, toby);
				break;
			}
			case 7: {
				if (frombx <= 0.0) {
					bx = 127.0;
					fromby = 38.0;
				}
				transPt = convertWTM2(point, toType, bx, fromby, tobx, toby);
				break;
			}
			case 8: {
				if (frombx <= 0.0) {
					bx = 128.0;
					fromby = 38.0;
				}
				transPt = convertWKTM2(point, toType, bx, frombx, tobx, toby);
				break;
			}
			case 10: {
				if (frombx <= 0.0) {
					bx = 127.0;
					fromby = 38.0;
				}
				transPt = convertWCONGNAMUL2(point, toType, bx, fromby, tobx, toby);
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertTM2(final CoordPoint point, final int toType, final double frombx, final double fromby, double tobx, double toby) {
		final CoordPoint transPt = point.clone();
		switch (toType) {
			case 1: {
				if (tobx <= 0.0) {
					tobx = 127.0;
					toby = 38.0;
				}
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2TM(tobx, toby);
				break;
			}
			case 2: {
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (tobx <= 0.0) {
					tobx = 129.0;
					toby = 0.0;
				}
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2UTM(tobx, toby);
				break;
			}
			case 4: {
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2WGS();
				break;
			}
			case 6: {
				transPt.convertTM2BESSEL(frombx, fromby);
				break;
			}
			case 7: {
				if (tobx <= 0.0) {
					tobx = 127.0;
					toby = 38.0;
				}
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WTM(tobx, toby);
				break;
			}
			case 8: {
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertTM2BESSEL(frombx, fromby);
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertKTM2(final CoordPoint point, final int toType, double tobx, double toby) {
		final CoordPoint transPt = point.clone();
		switch (toType) {
			case 1: {
				if (tobx <= 0.0) {
					tobx = 127.0;
					toby = 38.0;
				}
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2TM(tobx, toby);
			}
			case 3: {
				if (tobx <= 0.0) {
					tobx = 129.0;
					toby = 0.0;
				}
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2UTM(tobx, toby);
				break;
			}
			case 4: {
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2WGS();
				break;
			}
			case 6: {
				transPt.convertKTM2BESSEL();
				break;
			}
			case 7: {
				if (tobx <= 0.0) {
					tobx = 127.0;
					toby = 38.0;
				}
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WTM(tobx, toby);
				break;
			}
			case 8: {
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertKTM2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertUTM2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2TM(g, j);
				break;
			}
			case 2: {
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2UTM(g, j);
				break;
			}
			case 4: {
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertUTM2WGS(e, h);
				break;
			}
			case 6: {
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				break;
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertUTM2WGS(e, h);
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertCONGNAMUL2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2TM(g, j);
				break;
			}
			case 2: {
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2UTM(g, j);
			}
			case 5: {
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2WGS();
				break;
			}
			case 6: {
				transPt.convertCONG2BESSEL();
				break;
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertCONG2BESSEL();
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertWGS2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2TM(g, j);
				break;
			}
			case 2: {
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertWGS2UTM(g, j);
				break;
			}
			case 4: {
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2CONG();
			}
			case 6: {
				transPt.convertWGS2BESSEL();
				break;
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertBESSEL2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertBESSEL2TM(g, j);
				break;
			}
			case 2: {
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2UTM(g, j);
				break;
			}
			case 4: {
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertBESSEL2WGS();
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertBESSEL2WGS();
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertWTM2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2TM(g, j);
				break;
			}
			case 2: {
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2UTM(g, j);
				break;
			}
			case 4: {
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2WGS();
				break;
			}
			case 6: {
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2BESSEL();
				break;
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertWTM2WGS(e, h);
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertWKTM2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWKTM2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2TM(g, j);
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertWKTM2WGS();
				transPt.convertWGS2UTM(g, j);
				break;
			}
			case 4: {
				transPt.convertWKTM2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertWKTM2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2WGS();
				break;
			}
			case 6: {
				transPt.convertWKTM2WGS();
				transPt.convertWGS2BESSEL();
				break;
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWKTM2WGS();
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertWKTM2WGS();
				transPt.convertWGS2WKTM();
				break;
			}
			case 10: {
				transPt.convertWKTM2WGS();
				transPt.convertWGS2WCONG();
				break;
			}
		}
		return transPt;
	}

	private static CoordPoint convertWCONGNAMUL2(final CoordPoint point, final int d, final double e, final double h, double g, double j) {
		final CoordPoint transPt = point.clone();
		switch (d) {
			case 1: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWCONG2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2TM(g, j);
				break;
			}
			case 2: {
				transPt.convertWCONG2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2KTM();
				break;
			}
			case 3: {
				if (g <= 0.0) {
					g = 129.0;
					j = 0.0;
				}
				transPt.convertWCONG2WGS();
				transPt.convertWGS2UTM(g, j);
				break;
			}
			case 4: {
				transPt.convertWCONG2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2CONG();
				break;
			}
			case 5: {
				transPt.convertWCONG2WGS();
				transPt.convertWGS2BESSEL();
				transPt.convertBESSEL2WGS();
				break;
			}
			case 6: {
				transPt.convertWCONG2WGS();
				transPt.convertWGS2BESSEL();
				break;
			}
			case 7: {
				if (g <= 0.0) {
					g = 127.0;
					j = 38.0;
				}
				transPt.convertWCONG2WGS();
				transPt.convertWGS2WTM(g, j);
				break;
			}
			case 8: {
				transPt.convertWCONG2WGS();
				transPt.convertWGS2WKTM();
				break;
			}
		}
		return transPt;
	}
}
