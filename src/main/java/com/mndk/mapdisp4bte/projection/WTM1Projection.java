package com.mndk.mapdisp4bte.projection;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;

@Deprecated
public class WTM1Projection implements GeographicProjection {

    private static final double EARTH_RADIUS = 6378137.0;
    private static final double EARTH_RADIUS2 = EARTH_RADIUS * EARTH_RADIUS;
    private static final double SOME_CONSTANT = 0.0033528106647474805;

    @Override
    public double[] toGeo(double x, double y) {
        return wtmToGeo(new double[] {x, y}, 500000.0, 200000.0, 1.0, 38, 127);
    }

    @Override
    public double[] fromGeo(double lon, double lat) {
        return geoToWTM(new double[] {lon, lat}, 500000.0, 200000.0, 1.0, 38, 127);
    }

    @Override
    public double metersPerUnit() {
        return 0; // Idk
    }

    private static double[] geoToWTM(double[] coord,
                                     final double h, final double g, final double j, final double l, final double m) {
        double[] result = new double[2];
        double lat = coord[1], lon = coord[0];

        double w = SOME_CONSTANT, A, o, D, u, z, G, E, I, J, L, M, H, B = g;
        if (w > 1.0) w = 1.0 / w;
        A = Math.atan(1.0) / 45.0;
        o = lat * A;
        D = lon * A;
        u = l * A;
        A *= m;
        w = 1.0 / w;
        z = EARTH_RADIUS * (w - 1.0) / w;
        G = (EARTH_RADIUS2 - z*z) / EARTH_RADIUS2;
        w = (EARTH_RADIUS2 - z*z) / (z*z);
        z = (EARTH_RADIUS - z) / (EARTH_RADIUS + z);
        double z2_z3 = z*z - z*z*z;
        double z3_z4 = z*z*z - z*z*z*z;
        double z5 = z*z*z*z*z;
        double z4_z5 = z*z*z*z - z5;
        E = EARTH_RADIUS * (1.0 - z + 5.0 * z2_z3 / 4.0 + 81.0 * z4_z5 / 64.0);
        I = 3.0 * EARTH_RADIUS * (z - (z*z) + 7.0 * z3_z4 / 8.0 + 55.0 * z5 / 64.0) / 2.0;
        J = 15.0 * EARTH_RADIUS * (z2_z3 + 3.0 * z4_z5 / 4.0) / 16.0;
        L = 35.0 * EARTH_RADIUS * (z3_z4 + 11.0 * z5 / 16.0) / 48.0;
        M = 315.0 * EARTH_RADIUS * z4_z5 / 512.0;
        D -= A;
        u = E * u - I * Math.sin(2.0 * u) + J * Math.sin(4.0 * u) - L * Math.sin(6.0 * u) + M * Math.sin(8.0 * u);
        z = u * j;
        H = Math.sin(o);
        u = Math.cos(o);
        A = H / u;
        double A2 = A*A, A4 = A*A*A*A;
        w *= u*u;
        double w2 = w*w, w3 = w*w*w, w4 = w*w*w*w;
        G = EARTH_RADIUS / Math.sqrt(1.0 - G * Math.pow(Math.sin(o), 2.0));
        o = E * o - I * Math.sin(2.0 * o) + J * Math.sin(4.0 * o) - L * Math.sin(6.0 * o) + M * Math.sin(8.0 * o);
        o *= j;
        E = G * H * u * j / 2.0;
        I = G * H * (u*u*u) * j * (5.0 - A2 + 9.0 * w + 4.0 * w2) / 24.0;
        J = G * H * (u*u*u*u*u) * j * (61.0 - 58.0 * A2 + A4 + 270.0 * w - 330.0 * A2 * w + 445.0 * w2 + 324.0 * w3 - 680.0 * A2 * w2 + 88.0 * w4 - 600.0 * A2 * w3 - 192.0 * A2 * w4) / 720.0;
        H = G * H * Math.pow(u, 7.0) * j * (1385.0 - 3111.0 * A2 + 543.0 * A4 - Math.pow(A, 6.0)) / 40320.0;
        o = o + (D*D) * E + (D*D*D*D) * I + Math.pow(D, 6.0) * J + Math.pow(D, 8.0) * H;
        result[1] = o - z + h;
        o = G * u * j;
        z = G * (u*u*u) * j * (1.0 - A2 + w) / 6.0;
        w = G * (u*u*u*u*u) * j * (5.0 - 18.0 * A2 + A4 + 14.0 * w - 58.0 * A2 * w + 13.0 * w2 + 4.0 * w3 - 64.0 * A2 * w2 - 25.0 * A2 * w3) / 120.0;
        u = G * Math.pow(u, 7.0) * j * (61.0 - 479.0 * A2 + 179.0 * A4 - Math.pow(A, 6.0)) / 5040.0;
        result[0] = B + D * o + (D*D*D) * z + (D*D*D*D*D) * w + Math.pow(D, 7.0) * u;

        return result;
    }

    private double[] wtmToGeo(double[] coord,
                              final double h, final double g, final double j, final double l, final double m) {
        double u = SOME_CONSTANT;
        double A, w, o, D, B, z, G, E, I, J, L, M, H;
        double x = coord[0], y = coord[1];

        if (u > 1.0) u = 1.0 / u;
        A = g;
        w = Math.atan(1.0) / 45.0;
        o = l * w;
        D = m * w;
        u = 1.0 / u;
        B = EARTH_RADIUS * (u - 1.0) / u;
        z = (EARTH_RADIUS2 - (B*B)) / EARTH_RADIUS2;
        u = (EARTH_RADIUS2 - (B*B)) / (B*B);
        B = (EARTH_RADIUS - B) / (EARTH_RADIUS + B);
        G = EARTH_RADIUS * (1.0 - B + 5.0 * ((B*B) - (B*B*B)) / 4.0 + 81.0 * ((B*B*B*B) - (B*B*B*B*B)) / 64.0);
        E = 3.0 * EARTH_RADIUS * (B - (B*B) + 7.0 * ((B*B*B) - (B*B*B*B)) / 8.0 + 55.0 * (B*B*B*B*B) / 64.0) / 2.0;
        I = 15.0 * EARTH_RADIUS * ((B*B) - (B*B*B) + 3.0 * ((B*B*B*B) - (B*B*B*B*B)) / 4.0) / 16.0;
        J = 35.0 * EARTH_RADIUS * ((B*B*B) - (B*B*B*B) + 11.0 * (B*B*B*B*B) / 16.0) / 48.0;
        L = 315.0 * EARTH_RADIUS * ((B*B*B*B) - (B*B*B*B*B)) / 512.0;
        o = G * o - E * Math.sin(2.0 * o) + I * Math.sin(4.0 * o) - J * Math.sin(6.0 * o) + L * Math.sin(8.0 * o);
        o *= j;
        o = y + o - h;
        M = o / j;
        H = EARTH_RADIUS * (1.0 - z) / Math.pow(Math.sqrt(1.0 - z * Math.pow(Math.sin(0.0), 2.0)), 3.0);
        o = M / H;
        for (y = 1.0; y <= 5.0; ++y) {
            B = G * o - E * Math.sin(2.0 * o) + I * Math.sin(4.0 * o) - J * Math.sin(6.0 * o) + L * Math.sin(8.0 * o);
            H = EARTH_RADIUS * (1.0 - z) / Math.pow(Math.sqrt(1.0 - z * Math.pow(Math.sin(o), 2.0)), 3.0);
            o += (M - B) / H;
        }
        H = EARTH_RADIUS * (1.0 - z) / Math.pow(Math.sqrt(1.0 - z * Math.pow(Math.sin(o), 2.0)), 3.0);
        G = EARTH_RADIUS / Math.sqrt(1.0 - z * Math.pow(Math.sin(o), 2.0));
        B = Math.sin(o);
        z = Math.cos(o);
        E = B / z;
        u *= (z*z);
        A = x - A;
        B = E / (2.0 * H * G * (j*j));
        I = E * (5.0 + 3.0 * (E*E) + u - 4.0 * (u*u) - 9.0 * (E*E) * u) / (24.0 * H * (G*G*G) * (j*j*j*j));
        J = E * (61.0 + 90.0 * (E*E) + 46.0 * u + 45.0 * (E*E*E*E) - 252.0 * (E*E) * u - 3.0 * (u*u) + 100.0 * (u*u*u) - 66.0 * (E*E) * (u*u) - 90.0 * (E*E*E*E) * u + 88.0 * (u*u*u*u) + 225.0 * (E*E*E*E) * (u*u) + 84.0 * (E*E) * (u*u*u) - 192.0 * (E*E) * (u*u*u*u)) / (720.0 * H * (G*G*G*G*G) * Math.pow(j, 6.0));
        H = E * (1385.0 + 3633.0 * (E*E) + 4095.0 * (E*E*E*E) + 1575.0 * Math.pow(E, 6.0)) / (40320.0 * H * Math.pow(G, 7.0) * Math.pow(j, 8.0));
        o = o - A*A * B + A*A*A*A * I - Math.pow(A, 6.0) * J + Math.pow(A, 8.0) * H;
        B = 1.0 / (G * z * j);
        H = (1.0 + 2.0 * (E*E) + u) / (6.0 * (G*G*G) * z * (j*j*j));
        u = (5.0 + 6.0 * u + 28.0 * (E*E) - 3.0 * (u*u) + 8.0 * (E*E) * u + 24.0 * (E*E*E*E) - 4.0 * (u*u*u) + 4.0 * (E*E) * (u*u) + 24.0 * (E*E) * (u*u*u)) / (120.0 * (G*G*G*G*G) * z * (j*j*j*j*j));
        z = (61.0 + 662.0 * (E*E) + 1320.0 * (E*E*E*E) + 720.0 * Math.pow(E, 6.0)) / (5040.0 * Math.pow(G, 7.0) * z * Math.pow(j, 7.0));
        A = A * B - (A*A*A) * H + (A*A*A*A*A) * u - Math.pow(A, 7.0) * z;
        D += A;
        return new double[] {D / w, o / w};
    }

}
