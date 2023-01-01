package com.mndk.bteterrarenderer.projection;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import org.osgeo.proj4j.*;

/**
 * Proj4j + {@link GeographicProjection}
 */
public class Proj4jProjection implements GeographicProjection {

	private static final CRSFactory crsFactory = new CRSFactory();
	private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

	private static final CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", new String[] {
			"+proj=longlat",
			"+datum=WGS84",
			"+no_defs"
	});

	protected final CoordinateReferenceSystem targetCrs;

	private final CoordinateTransform toWgs;
	private final CoordinateTransform toTargetCrs;

	public Proj4jProjection(CoordinateReferenceSystem crs) {
		this.targetCrs = crs;
		this.toWgs = ctFactory.createTransform(targetCrs, WGS84);
		this.toTargetCrs = ctFactory.createTransform(WGS84, targetCrs);
	}

	public Proj4jProjection(String crsName, String crsParameter) {
		this(crsFactory.createFromParameters(crsName, crsParameter));
	}

	@Override
	public double[] fromGeo(double longitude, double latitude) {
		ProjCoordinate result = new ProjCoordinate();
		toTargetCrs.transform(new ProjCoordinate(longitude, latitude), result);
		return new double[] {result.x, result.y};
	}

	@Override
	public double metersPerUnit() {
		return 0;
	}

	@Override
	public double[] toGeo(double x, double z) {
		ProjCoordinate result = new ProjCoordinate();
		toWgs.transform(new ProjCoordinate(x, z), result);
		return new double[] {result.x, result.y};
	}

}
