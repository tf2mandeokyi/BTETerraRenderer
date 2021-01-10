package com.mndk.kmap4bte.projection;

import com.mndk.kmap4bte.projection.terra121.InvertedOrientation;
import com.mndk.kmap4bte.projection.terra121.ModifiedAirocean;
import com.mndk.kmap4bte.projection.terra121.UprightOrientation;
import com.mndk.kmap4bte.projection.wtm.WTMProjection;

public class GeographicProjection {

    public static double EARTH_CIRCUMFERENCE = 40075017;
    public static double EARTH_POLAR_CIRCUMFERENCE = 40008000;


    public static enum Orientation {
        none, upright, swapped
    };

    public static GeographicProjection orientProjection(GeographicProjection base, Orientation o) {
        if(base.upright()) {
            if(o==Orientation.upright)
                return base;
            base = new UprightOrientation(base);
        }

        if(o==Orientation.swapped) {
            return new InvertedOrientation(base);
        } else if(o==Orientation.upright) {
            base = new UprightOrientation(base);
        }

        return base;
    }

    public double[] toGeo(double x, double y) {
        return new double[] {x,y};
    }

    public double[] fromGeo(double lon, double lat) {
        return new double[] {lon, lat};
    }

    public double metersPerUnit() {
        return 100000;
    }

    public double[] bounds() {

        //get max in by using extreme coordinates
        double[] b = new double[] {
                fromGeo(-180,0)[0],
                fromGeo(0,-90)[1],
                fromGeo(180,0)[0],
                fromGeo(0,90)[1]
        };

        if(b[0]>b[2]) {
            double t = b[0];
            b[0] = b[2];
            b[2] = t;
        }

        if(b[1]>b[3]) {
            double t = b[1];
            b[1] = b[3];
            b[3] = t;
        }

        return b;
    }

    public boolean upright() {
        return fromGeo(0,90)[1]<=fromGeo(0,-90)[1];
    }

    public double[] vector(double x, double y, double north, double east) {
        double geo[] = toGeo(x,y);

        //TODO: east may be slightly off because earth not a sphere
        double off[] = fromGeo(geo[0] + east*360.0/(Math.cos(geo[1]*Math.PI/180.0)*EARTH_CIRCUMFERENCE),
                geo[1] + north*360.0/EARTH_POLAR_CIRCUMFERENCE);

        return new double[] {off[0]-x,off[1]-y};
    }

    public double[] tissot(double lon, double lat, double d) {

        double R = EARTH_CIRCUMFERENCE/(2*Math.PI);

        double ddeg = d*180.0/Math.PI;

        double[] base = fromGeo(lon, lat);
        double[] lonoff = fromGeo(lon+ddeg, lat);
        double[] latoff = fromGeo(lon,lat+ddeg);

        double dxdl = (lonoff[0]-base[0])/d;
        double dxdp = (latoff[0]-base[0])/d;
        double dydl = (lonoff[1]-base[1])/d;
        double dydp = (latoff[1]-base[1])/d;

        double cosp = Math.cos(lat*Math.PI/180.0);

        double h = Math.sqrt(dxdp*dxdp + dydp*dydp)/R;
        double k = Math.sqrt(dxdl*dxdl + dydl*dydl)/(cosp*R);

        double sint = Math.abs(dydp*dxdl - dxdp*dydl)/(R*R*cosp*h*k);
        double ap = Math.sqrt(h*h + k*k + 2*h*k*sint);
        double bp = Math.sqrt(h*h + k*k - 2*h*k*sint);

        double a = (ap+bp)/2;
        double b = (ap-bp)/2;

        return new double[] {h*k*sint, 2*Math.asin(bp/ap), a, b};
    }

    public static void main(String[] args) { // 37.43224724326348, 126.99475062580593
        /*CoordPoint b = new CoordPoint(498840/2.5, 1092640/2.5);
        CoordPoint c = TransCoord.getTransCoord(b, TransCoord.COORD_TYPE_WTM, TransCoord.COORD_TYPE_WGS84);
        System.out.println(c);*/
        double[] a = new WTMProjection().toGeo(498840/2.5, 1092640/2.5);
        System.out.println(a[1] + "," + a[0]);
        double asdf = new ModifiedAirocean().metersPerUnit();
        double[] b = new ModifiedAirocean().fromGeo(a[0], a[1]);
        System.out.println(b[0] * asdf + ", " + -b[1] * asdf);
    }
}