package copy.kr.hyosang.coordinate;

/* Copied from https://hyosang82.tistory.com/269, thus its license is followed by Daum OpenAPI. */

@Deprecated
public class CoordPoint
{
    public double x;
    public double y;
    private double m_imode;
    private double m_ds;
    private double m_kappa;
    private double m_phi;
    private double m_omega;
    private double m_dz;
    private double m_dy;
    private double m_dx;
    private static final double m_AW = 6378137.0;
    private static final double m_FW = 0.0033528106647474805;
    private static final double m_AB = 6377397.155;
    private static final double m_FB = 0.0033427731799399794;
    private static final double m_OKKTM = 1.0;
    private static final double m_OKUTM = 0.9996;
    private static final double m_OKGTM = 0.9999;
    private static final double m_TX = 115.8;
    private static final double m_TY = -474.99;
    private static final double m_TZ = -674.11;
    private static final double m_TOMEGA = 1.16;
    private static final double m_TPHI = -2.31;
    private static final double m_TKAPPA = -1.63;
    private static final double m_TS = -6.43;
    private static final double m_TMODE = 1.0;
    private static final double m_ux0 = 0.0;
    private static final double m_uy0 = 500000.0;
    private static final double m_x0 = 500000.0;
    private static final double m_y0 = 200000.0;
    private static final double m_x1 = 600000.0;
    private static final double m_y1 = 400000.0;
    private final CoordRect[] rectArray1;
    private final CoordRect[] rectArray2;
    private double[][] deltaValue1;
    private double[][] deltaValue2;

    public CoordPoint() {
        this.m_imode = 0.0;
        this.m_ds = 0.0;
        this.m_kappa = 0.0;
        this.m_phi = 0.0;
        this.m_omega = 0.0;
        this.m_dz = 0.0;
        this.m_dy = 0.0;
        this.m_dx = 0.0;
        this.rectArray1 = new CoordRect[] { new CoordRect(112500.0, -50000.0, 33500.0, 53000.0), new CoordRect(146000.0, -50000.0, 54000.0, 58600.0), new CoordRect(130000.0, 44000.0, 15000.0, 14000.0), new CoordRect(532500.0, 437500.0, 25000.0, 25000.0), new CoordRect(625000.0, 412500.0, 25000.0, 25000.0), new CoordRect(-12500.0, 462500.0, 17500.0, 50000.0) };
        this.rectArray2 = new CoordRect[] { new CoordRect(112500.0, -50000.0, 33500.0, 53000.0), new CoordRect(146000.0, -50000.0, 54000.0, 58600.0), new CoordRect(130000.0, 44000.0, 15000.0, 14000.0), new CoordRect(532500.0, 437500.0, 25000.0, 25000.0), new CoordRect(625000.0, 412500.0, 25000.0, 25000.0), new CoordRect(-12500.0, 462500.0, 17500.0, 50000.0) };
        this.deltaValue1 = new double[][] { { 0.0, 50000.0 }, { 0.0, 50000.0 }, { 0.0, 10000.0 }, { -70378.0, -136.0 }, { -144738.0, -2161.0 }, { 23510.0, -111.0 } };
        this.deltaValue2 = new double[][] { { 0.0, -50000.0 }, { 0.0, -50000.0 }, { 0.0, -10000.0 }, { 70378.0, 136.0 }, { 144738.0, 2161.0 }, { -23510.0, 111.0 } };
        for (int i = 0; i < this.rectArray2.length; ++i) {
            final CoordRect coordRect = this.rectArray2[i];
            coordRect.x += this.deltaValue1[i][0];
            final CoordRect coordRect2 = this.rectArray2[i];
            coordRect2.y += this.deltaValue1[i][1];
        }
    }

    public CoordPoint(final double x, final double y) {
        this.m_imode = 0.0;
        this.m_ds = 0.0;
        this.m_kappa = 0.0;
        this.m_phi = 0.0;
        this.m_omega = 0.0;
        this.m_dz = 0.0;
        this.m_dy = 0.0;
        this.m_dx = 0.0;
        this.rectArray1 = new CoordRect[] { new CoordRect(112500.0, -50000.0, 33500.0, 53000.0), new CoordRect(146000.0, -50000.0, 54000.0, 58600.0), new CoordRect(130000.0, 44000.0, 15000.0, 14000.0), new CoordRect(532500.0, 437500.0, 25000.0, 25000.0), new CoordRect(625000.0, 412500.0, 25000.0, 25000.0), new CoordRect(-12500.0, 462500.0, 17500.0, 50000.0) };
        this.rectArray2 = new CoordRect[] { new CoordRect(112500.0, -50000.0, 33500.0, 53000.0), new CoordRect(146000.0, -50000.0, 54000.0, 58600.0), new CoordRect(130000.0, 44000.0, 15000.0, 14000.0), new CoordRect(532500.0, 437500.0, 25000.0, 25000.0), new CoordRect(625000.0, 412500.0, 25000.0, 25000.0), new CoordRect(-12500.0, 462500.0, 17500.0, 50000.0) };
        this.deltaValue1 = new double[][] { { 0.0, 50000.0 }, { 0.0, 50000.0 }, { 0.0, 10000.0 }, { -70378.0, -136.0 }, { -144738.0, -2161.0 }, { 23510.0, -111.0 } };
        this.deltaValue2 = new double[][] { { 0.0, -50000.0 }, { 0.0, -50000.0 }, { 0.0, -10000.0 }, { 70378.0, 136.0 }, { 144738.0, 2161.0 }, { -23510.0, 111.0 } };
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public CoordPoint clone() {
        return new CoordPoint(this.x, this.y);
    }

    public void convertBESSEL2KTM() {
        this.GP2TM(6377397.155, 0.0033427731799399794, 600000.0, 400000.0, 0.9999, 38.0, 128.0);
    }

    public void convertBESSEL2CONG() {
        this.GP2TM(6377397.155, 0.0033427731799399794, 500000.0, 200000.0, 1.0, 38.0, 127.00289027777778);
        this.shiftIsland(true);
    }

    public void convertBESSEL2WGS() {
        this.setParameter(115.8, -474.99, -674.11, 1.16, -2.31, -1.63, -6.43, 1.0);
        final double[] rtn = this.GP2WGP(this.y, this.x, 0.0, 6377397.155, 0.0033427731799399794);
        this.x = rtn[1];
        this.y = rtn[0];
    }

    public void convertKTM2BESSEL() {
        this.TM2GP(6377397.155, 0.0033427731799399794, 600000.0, 400000.0, 0.9999, 38.0, 128.0);
    }

    public void convertBESSEL2TM(final double d, final double e) {
        this.GP2TM(6377397.155, 0.0033427731799399794, 500000.0, 200000.0, 1.0, e, d + 0.0028902777777777776);
    }

    public void convertTM2BESSEL(final double d, final double e) {
        this.TM2GP(6377397.155, 0.0033427731799399794, 500000.0, 200000.0, 1.0, e, d + 0.0028902777777777776);
    }

    public void convertWGS2UTM(final double d, final double e) {
        this.setParameter(115.8, -474.99, -674.11, 1.16, -2.31, -1.63, -6.43, 1.0);
        this.GP2TM(6378137.0, 0.0033528106647474805, 0.0, 500000.0, 0.9996, e, d);
    }

    public void convertWGS2WTM(final double d, final double e) {
        this.GP2TM(6378137.0, 0.0033528106647474805, 500000.0, 200000.0, 1.0, e, d);
    }

    public void convertWGS2WKTM() {
        this.GP2TM(6378137.0, 0.0033528106647474805, 600000.0, 400000.0, 0.9999, 38.0, 128.0);
    }

    public void convertWGS2WCONG() {
        this.GP2TM(6378137.0, 0.0033528106647474805, 500000.0, 200000.0, 1.0, 38.0, 127.0);
        this.x = (double)Math.round(this.x * 2.5);
        this.y = (double)Math.round(this.y * 2.5);
    }

    public void convertUTM2WGS(final double d, final double e) {
        this.setParameter(115.8, -474.99, -674.11, 1.16, -2.31, -1.63, -6.43, 1.0);
        this.TM2GP(6378137.0, 0.0033528106647474805, 0.0, 500000.0, 0.9996, e, d);
    }

    public void convertWGS2BESSEL() {
        this.setParameter(115.8, -474.99, -674.11, 1.16, -2.31, -1.63, -6.43, 1.0);
        final double[] rtn = this.WGP2GP(this.y, this.x, 0.0, 6377397.155, 0.0033427731799399794);
        this.x = rtn[1];
        this.y = rtn[0];
    }

    public void convertCONG2BESSEL() {
        this.shiftIsland(false);
        this.TM2GP(6377397.155, 0.0033427731799399794, 500000.0, 200000.0, 1.0, 38.0, 127.00289027777778);
    }

    public void convertWTM2WGS(final double d, final double e) {
        this.TM2GP(6378137.0, 0.0033528106647474805, 500000.0, 200000.0, 1.0, e, d);
    }

    public void convertWKTM2WGS() {
        this.TM2GP(6378137.0, 0.0033528106647474805, 600000.0, 400000.0, 0.9999, 38.0, 128.0);
    }

    public void convertWCONG2WGS() {
        this.x /= 2.5;
        this.y /= 2.5;
        this.TM2GP(6378137.0, 0.0033528106647474805, 500000.0, 200000.0, 1.0, 38.0, 127.0);
    }

    private double[] WGP2GP(final double a, final double b, final double d, final double e, final double h) {
        double[] rtn = this.WGP2WCTR(a, b, d);
        if (this.m_imode == 1.0) {
            rtn = this.TransMolod(rtn[0], rtn[1], rtn[2]);
        }
        else {
            rtn = this.TransBursa(rtn[0], rtn[1], rtn[2]);
        }
        return this.CTR2GP(rtn[0], rtn[1], rtn[2], e, h);
    }

    private double[] WGP2WCTR(final double a, final double b, final double d) {
        return this.GP2CTR(a, b, d, 6378137.0, 0.0033528106647474805);
    }

    private double[] GP2WGP(final double a, final double b, final double d, final double e, final double h) {
        double[] rtn = this.GP2CTR(a, b, d, e, h);
        if (this.m_imode == 1.0) {
            rtn = this.InverseMolod(rtn[0], rtn[1], rtn[2]);
        }
        else {
            rtn = this.InverseBursa(rtn[0], rtn[1], rtn[2]);
        }
        return this.WCTR2WGP(rtn[0], rtn[1], rtn[2]);
    }

    private double[] GP2CTR(final double a, final double b, final double d, final double e, final double h) {
        final double[] rtn = new double[3];
        double j = 0.0;
        double l = 0.0;
        double o = 0.0;
        double m = h;
        if (m > 1.0) {
            m = 1.0 / m;
        }
        j = Math.atan(1.0) / 45.0;
        l = a * j;
        j *= b;
        m = 1.0 / m;
        m = e * (m - 1.0) / m;
        o = ((e*e) - (m*m)) / (e*e);
        o = e / Math.sqrt(1.0 - o * Math.pow(Math.sin(l), 2.0));
        rtn[0] = (o + d) * Math.cos(l) * Math.cos(j);
        rtn[1] = (o + d) * Math.cos(l) * Math.sin(j);
        rtn[2] = ((m*m) / (e*e) * o + d) * Math.sin(l);
        return rtn;
    }

    private double[] InverseMolod(final double a, final double b, final double d) {
        final double[] rtn = new double[3];
        double e = 0.0;
        double h = 0.0;
        double g = 0.0;
        e = (a - this.m_dx) * (1.0 + this.m_ds);
        h = (b - this.m_dy) * (1.0 + this.m_ds);
        g = (d - this.m_dz) * (1.0 + this.m_ds);
        rtn[0] = 1.0 / (1.0 + this.m_ds) * (e - this.m_kappa * h + this.m_phi * g);
        rtn[1] = 1.0 / (1.0 + this.m_ds) * (this.m_kappa * e + h - this.m_omega * g);
        rtn[2] = 1.0 / (1.0 + this.m_ds) * (-1.0 * this.m_phi * e + this.m_omega * h + g);
        return rtn;
    }

    private double[] InverseBursa(final double a, final double b, final double d) {
        final double e = a - this.m_dx;
        final double h = b - this.m_dy;
        final double g = d - this.m_dz;
        final double[] rtn = { 1.0 / (1.0 + this.m_ds) * (e - this.m_kappa * h + this.m_phi * g), 1.0 / (1.0 + this.m_ds) * (this.m_kappa * e + h - this.m_omega * g), 1.0 / (1.0 + this.m_ds) * (-1.0 * this.m_phi * e + this.m_omega * h + g) };
        return rtn;
    }

    private double[] TransMolod(final double a, final double b, final double d) {
        final double[] rtn = { a + (1.0 + this.m_ds) * (this.m_kappa * b - this.m_phi * d) + this.m_dx, b + (1.0 + this.m_ds) * (-1.0 * this.m_kappa * a + this.m_omega * d) + this.m_dy, d + (1.0 + this.m_ds) * (this.m_phi * a - this.m_omega * b) + this.m_dz };
        return rtn;
    }

    private double[] TransBursa(final double a, final double b, final double d) {
        final double[] rtn = { (1.0 + this.m_ds) * (a + this.m_kappa * b - this.m_phi * d) + this.m_dx, (1.0 + this.m_ds) * (-1.0 * this.m_kappa * a + b + this.m_omega * d) + this.m_dy, (1.0 + this.m_ds) * (this.m_phi * a - this.m_omega * b + d) + this.m_dz };
        return rtn;
    }

    private double[] WCTR2WGP(final double a, final double b, final double d) {
        return this.CTR2GP(a, b, d, 6378137.0, 0.0033528106647474805);
    }

    private double[] CTR2GP(final double a, double b, final double d, final double e, final double h) {
        double m = h;
        double w = 0.0;
        double g = 0.0;
        double o = 0.0;
        double D = 0.0;
        double A = 0.0;
        double u = 0.0;
        double l = 0.0;
        double j = 0.0;
        if (m > 1.0) {
            m = 1.0 / m;
        }
        g = Math.atan(1.0) / 45.0;
        m = 1.0 / m;
        o = e * (m - 1.0) / m;
        D = ((e*e) - (o*o)) / (e*e);
        m = Math.atan(b / a);
        A = Math.sqrt(a * a + b * b);
        u = e;
        b = 0.0;
        do {
            ++b;
            w = Math.pow((o*o) / (e*e) * u + w, 2.0) - (d*d);
            w = d / Math.sqrt(w);
            l = Math.atan(w);
            if (Math.abs(l - j) < 1.0E-18) {
                break;
            }
            u = e / Math.sqrt(1.0 - D * Math.pow(Math.sin(l), 2.0));
            w = A / Math.cos(l) - u;
            j = l;
        } while (b <= 30.0);
        final double[] rtn = { l / g, m / g };
        if (a < 0.0) {
            rtn[1] += 180.0;
        }
        if (rtn[1] < 0.0) {
            rtn[1] += 360.0;
        }
        return rtn;
    }

    private void GP2TM(final double d, final double e, final double h, final double g, final double j, final double l, final double m) {
        final double a = this.y;
        final double b = this.x;
        double w = e;
        double A = 0.0;
        double o = 0.0;
        double D = 0.0;
        double u = 0.0;
        double z = 0.0;
        double G = 0.0;
        double E = 0.0;
        double I = 0.0;
        double J = 0.0;
        double L = 0.0;
        double M = 0.0;
        double H = 0.0;
        final double B = g;
        if (w > 1.0) {
            w = 1.0 / w;
        }
        A = Math.atan(1.0) / 45.0;
        o = a * A;
        D = b * A;
        u = l * A;
        A *= m;
        w = 1.0 / w;
        z = d * (w - 1.0) / w;
        G = ((d*d) - (z*z)) / (d*d);
        w = ((d*d) - (z*z)) / (z*z);
        z = (d - z) / (d + z);
        E = d * (1.0 - z + 5.0 * ((z*z) - (z*z*z)) / 4.0 + 81.0 * (z*z*z*z - z*z*z*z*z) / 64.0);
        I = 3.0 * d * (z - (z*z) + 7.0 * (z*z*z - z*z*z*z) / 8.0 + 55.0 * (z*z*z*z*z) / 64.0) / 2.0;
        J = 15.0 * d * ((z*z) - (z*z*z) + 3.0 * (z*z*z*z - z*z*z*z*z) / 4.0) / 16.0;
        L = 35.0 * d * (z*z*z - z*z*z*z + 11.0 * (z*z*z*z*z) / 16.0) / 48.0;
        M = 315.0 * d * (z*z*z*z - z*z*z*z*z) / 512.0;
        D -= A;
        u = E * u - I * Math.sin(2.0 * u) + J * Math.sin(4.0 * u) - L * Math.sin(6.0 * u) + M * Math.sin(8.0 * u);
        z = u * j;
        H = Math.sin(o);
        u = Math.cos(o);
        A = H / u;
        double A2 = A*A, A4 = A*A*A*A;
        w *= (u*u);
        double w2 = w*w, w3 = w*w*w, w4 = w*w*w*w;
        G = d / Math.sqrt(1.0 - G * Math.pow(Math.sin(o), 2.0));
        o = E * o - I * Math.sin(2.0 * o) + J * Math.sin(4.0 * o) - L * Math.sin(6.0 * o) + M * Math.sin(8.0 * o);
        o *= j;
        E = G * H * u * j / 2.0;
        I = G * H * (u*u*u) * j * (5.0 - A2 + 9.0 * w + 4.0 * w2) / 24.0;
        J = G * H * (u*u*u*u*u) * j * (61.0 - 58.0 * A2 + A4 + 270.0 * w - 330.0 * A2 * w + 445.0 * w2 + 324.0 * w3 - 680.0 * A2 * w2 + 88.0 * (w*w*w*w) - 600.0 * A2 * w3 - 192.0 * A2 * (w*w*w*w)) / 720.0;
        H = G * H * Math.pow(u, 7.0) * j * (1385.0 - 3111.0 * A2 + 543.0 * A4 - Math.pow(A, 6.0)) / 40320.0;
        o = o + (D*D) * E + (D*D*D*D) * I + Math.pow(D, 6.0) * J + Math.pow(D, 8.0) * H;
        this.y = o - z + h;
        o = G * u * j;
        z = G * (u*u*u) * j * (1.0 - A2 + w) / 6.0;
        w = G * (u*u*u*u*u) * j * (5.0 - 18.0 * A2 + A4 + 14.0 * w - 58.0 * A2 * w + 13.0 * w2 + 4.0 * w3 - 64.0 * A2 * w2 - 25.0 * A2 * w3) / 120.0;
        u = G * Math.pow(u, 7.0) * j * (61.0 - 479.0 * A2 + 179.0 * A4 - Math.pow(A, 6.0)) / 5040.0;
        this.x = B + D * o + (D*D*D) * z + (D*D*D*D*D) * w + Math.pow(D, 7.0) * u;
    }

    private void TM2GP(final double d, final double e, final double h, final double g, final double j, final double l, final double m) {
        double u = e;
        double A = 0.0;
        double w = 0.0;
        double o = 0.0;
        double D = 0.0;
        double B = 0.0;
        double z = 0.0;
        double G = 0.0;
        double E = 0.0;
        double I = 0.0;
        double J = 0.0;
        double L = 0.0;
        double M = 0.0;
        double H = 0.0;
        double a = this.y;
        final double b = this.x;
        System.out.println("DEBUG: " + a + " " + b);
        if (u > 1.0) {
            u = 1.0 / u;
        }
        A = g;
        w = Math.atan(1.0) / 45.0;
        o = l * w;
        D = m * w;
        u = 1.0 / u;
        B = d * (u - 1.0) / u;
        z = ((d*d) - (B*B)) / (d*d);
        u = ((d*d) - (B*B)) / (B*B);
        B = (d - B) / (d + B);
        G = d * (1.0 - B + 5.0 * ((B*B) - (B*B*B)) / 4.0 + 81.0 * ((B*B*B*B) - (B*B*B*B*B)) / 64.0);
        E = 3.0 * d * (B - (B*B) + 7.0 * ((B*B*B) - (B*B*B*B)) / 8.0 + 55.0 * (B*B*B*B*B) / 64.0) / 2.0;
        I = 15.0 * d * ((B*B) - (B*B*B) + 3.0 * ((B*B*B*B) - (B*B*B*B*B)) / 4.0) / 16.0;
        J = 35.0 * d * ((B*B*B) - (B*B*B*B) + 11.0 * (B*B*B*B*B) / 16.0) / 48.0;
        L = 315.0 * d * ((B*B*B*B) - (B*B*B*B*B)) / 512.0;
        o = G * o - E * Math.sin(2.0 * o) + I * Math.sin(4.0 * o) - J * Math.sin(6.0 * o) + L * Math.sin(8.0 * o);
        o *= j;
        o = a + o - h;
        M = o / j;
        H = d * (1.0 - z) / Math.pow(Math.sqrt(1.0 - z * Math.pow(Math.sin(0.0), 2.0)), 3.0);
        o = M / H;
        for (a = 1.0; a <= 5.0; ++a) {
            B = G * o - E * Math.sin(2.0 * o) + I * Math.sin(4.0 * o) - J * Math.sin(6.0 * o) + L * Math.sin(8.0 * o);
            H = d * (1.0 - z) / Math.pow(Math.sqrt(1.0 - z * Math.pow(Math.sin(o), 2.0)), 3.0);
            o += (M - B) / H;
        }
        H = d * (1.0 - z) / Math.pow(Math.sqrt(1.0 - z * Math.pow(Math.sin(o), 2.0)), 3.0);
        G = d / Math.sqrt(1.0 - z * Math.pow(Math.sin(o), 2.0));
        B = Math.sin(o);
        z = Math.cos(o);
        E = B / z;
        u *= (z*z);
        A = b - A;
        B = E / (2.0 * H * G * (j*j));
        I = E * (5.0 + 3.0 * (E*E) + u - 4.0 * (u*u) - 9.0 * (E*E) * u) / (24.0 * H * (G*G*G) * (j*j*j*j));
        J = E * (61.0 + 90.0 * (E*E) + 46.0 * u + 45.0 * (E*E*E*E) - 252.0 * (E*E) * u - 3.0 * (u*u) + 100.0 * (u*u*u) - 66.0 * (E*E) * (u*u) - 90.0 * (E*E*E*E) * u + 88.0 * (u*u*u*u) + 225.0 * (E*E*E*E) * (u*u) + 84.0 * (E*E) * (u*u*u) - 192.0 * (E*E) * (u*u*u*u)) / (720.0 * H * (G*G*G*G*G) * Math.pow(j, 6.0));
        H = E * (1385.0 + 3633.0 * (E*E) + 4095.0 * (E*E*E*E) + 1575.0 * Math.pow(E, 6.0)) / (40320.0 * H * Math.pow(G, 7.0) * Math.pow(j, 8.0));
        o = o - (A*A) * B + (A*A*A*A) * I - Math.pow(A, 6.0) * J + Math.pow(A, 8.0) * H;
        B = 1.0 / (G * z * j);
        H = (1.0 + 2.0 * (E*E) + u) / (6.0 * (G*G*G) * z * (j*j*j));
        u = (5.0 + 6.0 * u + 28.0 * (E*E) - 3.0 * (u*u) + 8.0 * (E*E) * u + 24.0 * (E*E*E*E) - 4.0 * (u*u*u) + 4.0 * (E*E) * (u*u) + 24.0 * (E*E) * (u*u*u)) / (120.0 * (G*G*G*G*G) * z * (j*j*j*j*j));
        z = (61.0 + 662.0 * (E*E) + 1320.0 * (E*E*E*E) + 720.0 * Math.pow(E, 6.0)) / (5040.0 * Math.pow(G, 7.0) * z * Math.pow(j, 7.0));
        A = A * B - (A*A*A) * H + (A*A*A*A*A) * u - Math.pow(A, 7.0) * z;
        D += A;
        this.x = D / w;
        this.y = o / w;
    }

    private void setParameter(final double a, final double b, final double d, final double e, final double h, final double g, final double j, final double l) {
        final double m = Math.atan(1.0) / 45.0;
        this.m_dx = a;
        this.m_dy = b;
        this.m_dz = d;
        this.m_omega = e / 3600.0 * m;
        this.m_phi = h / 3600.0 * m;
        this.m_kappa = g / 3600.0 * m;
        this.m_ds = j * 1.0E-6;
        this.m_imode = l;
    }

    private void shiftIsland(final boolean d) {
        double e = 0.0;
        double h = 0.0;
        double x;
        double y;
        if (d) {
            for (int i = 0; i < this.rectArray1.length; ++i) {
                if (this.x - this.rectArray1[i].x >= 0.0 && this.x - this.rectArray1[i].x <= this.rectArray1[i].w && this.y - this.rectArray1[i].y >= 0.0 && this.y - this.rectArray1[i].y <= this.rectArray1[i].h) {
                    e += this.deltaValue1[i][0];
                    h += this.deltaValue1[i][1];
                    break;
                }
            }
            x = (int)((this.x + e) * 2.5 + 0.5);
            y = (int)((this.y + h) * 2.5 + 0.5);
        }
        else {
            x = this.x / 2.5;
            y = this.y / 2.5;
            for (int i = 0; i < this.rectArray2.length; ++i) {
                if (x - this.rectArray2[i].x >= 0.0 && x - this.rectArray2[i].x <= this.rectArray2[i].w && y - this.rectArray2[i].y >= 0.0 && y - this.rectArray2[i].y <= this.rectArray2[i].h) {
                    x += this.deltaValue2[i][0];
                    y += this.deltaValue2[i][1];
                    break;
                }
            }
        }
        this.x = x;
        this.y = y;
    }

    private class CoordRect
    {
        public double x;
        public double y;
        public double w;
        public double h;

        public CoordRect(final double x, final double y, final double w, final double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}