// Shamelessly copied from https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/share/classes/java/util/DualPivotQuicksort.java

package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.experimental.UtilityClass;

import java.util.Comparator;

@SuppressWarnings("StatementWithEmptyBody")
@UtilityClass
class PointerDualPivotQuicksort {
    private static final int MAX_RUN_COUNT = 67;
    private static final int MAX_RUN_LENGTH = 33;
    private static final int QUICKSORT_THRESHOLD = 286;
    private static final int INSERTION_SORT_THRESHOLD = 47;

    static <T> void sort(Pointer<T> a, long left, long right, Comparator<? super T> c,
                         Pointer<T> work, long workBase, long workLen) {
        // Use Quicksort on small arrays
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true, c);
            return;
        }

        long[] run = new long[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;

        for (long k = left; k < right; run[count] = k) {
            if (c.compare(a.get(k), a.get(k + 1)) < 0) {
                while (++k <= right && c.compare(a.get(k - 1), a.get(k)) <= 0);
            } else if (c.compare(a.get(k), a.get(k + 1)) > 0) {
                while (++k <= right && c.compare(a.get(k - 1), a.get(k)) >= 0);
                for (long lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    a.swap(lo, hi);
                }
            } else {
                for (int m = MAX_RUN_LENGTH; ++k <= right && c.compare(a.get(k - 1), a.get(k)) == 0; ) {
                    if (--m == 0) {
                        sort(a, left, right, true, c);
                        return;
                    }
                }
            }

            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true, c);
                return;
            }
        }

        if (run[count] == right++) {
            run[++count] = right;
        } else if (count == 1) {
            return;
        }

        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        Pointer<T> b;
        long ao, bo;
        long blen = right - left;
        if (work == null || workLen < blen || workBase + blen > workLen) {
            work = a.getType().newArray(blen);
            workBase = 0;
        }
        if (odd == 0) {
            PointerHelper.copyMultiple(a.add(left), work.add(workBase), blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                long hi = run[k], mi = run[k - 1];
                for (long i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && c.compare(a.get(p + ao), a.get(q + ao)) <= 0) {
                        b.set(i + bo, a.get(p++ + ao));
                    } else {
                        b.set(i + bo, a.get(q++ + ao));
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (long i = right, lo = run[count - 1]; --i >= lo;
                     b.set(i + bo, a.get(i + ao))
                );
                run[++last] = right;
            }
            Pointer<T> t = a; a = b; b = t;
            long o = ao; ao = bo; bo = o;
        }
    }

    private static <T> void sort(Pointer<T> a, long left, long right, boolean leftmost, Comparator<? super T> c) {
        long length = right - left + 1;

        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (long i = left, j = i; i < right; j = ++i) {
                    T ai = a.get(i + 1);
                    while (c.compare(ai, a.get(j)) < 0) {
                        a.set(j + 1, a.get(j));
                        if (j-- == left) {
                            break;
                        }
                    }
                    a.set(j + 1, ai);
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (c.compare(a.get(++left), a.get(left - 1)) >= 0);

                for (long k = left; ++left <= right; k = ++left) {
                    T a1 = a.get(k), a2 = a.get(left);

                    if (c.compare(a1, a2) < 0) {
                        a2 = a1; a1 = a.get(left);
                    }
                    while (c.compare(a1, a.get(--k)) < 0) {
                        a.set(k + 2, a.get(k));
                    }
                    a.set(++k + 1, a1);

                    while (c.compare(a2, a.get(--k)) < 0) {
                        a.set(k + 1, a.get(k));
                    }
                    a.set(k + 1, a2);
                }
                T last = a.get(right);

                while (c.compare(last, a.get(--right)) < 0) {
                    a.set(right + 1, a.get(right));
                }
                a.set(right + 1, last);
            }
            return;
        }

        long seventh = (length >> 3) + (length >> 6) + 1;

        long e3 = (left + right) >>> 1;
        long e2 = e3 - seventh;
        long e1 = e2 - seventh;
        long e4 = e3 + seventh;
        long e5 = e4 + seventh;

        if (c.compare(a.get(e2), a.get(e1)) < 0) a.swap(e2, e1);

        if (c.compare(a.get(e3), a.get(e2)) < 0) { T t = a.get(e3); a.swap(e3, e2);
            if (c.compare(t, a.get(e1)) < 0) { a.swap(e2, e1); }
        }
        if (c.compare(a.get(e4), a.get(e3)) < 0) { T t = a.get(e4); a.swap(e4, e3);
            if (c.compare(t, a.get(e2)) < 0) { a.swap(e3, e2);
                if (c.compare(t, a.get(e1)) < 0) { a.swap(e2, e1); }
            }
        }
        if (c.compare(a.get(e5), a.get(e4)) < 0) { T t = a.get(e5); a.swap(e5, e4);
            if (c.compare(t, a.get(e3)) < 0) { a.swap(e4, e3);
                if (c.compare(t, a.get(e2)) < 0) { a.swap(e3, e2);
                    if (c.compare(t, a.get(e1)) < 0) { a.swap(e2, e1); }
                }
            }
        }

        long less  = left;
        long great = right;

        if (c.compare(a.get(e1), a.get(e2)) != 0 && c.compare(a.get(e2), a.get(e3)) != 0
                && c.compare(a.get(e3), a.get(e4)) != 0 && c.compare(a.get(e4), a.get(e5)) != 0) {
            T pivot1 = a.get(e2);
            T pivot2 = a.get(e4);

            a.set(e2, a.get(left));
            a.set(e4, a.get(right));

            while (c.compare(a.get(++less), pivot1) < 0);
            while (c.compare(a.get(--great), pivot2) > 0);

            outer: for (long k = less - 1; ++k <= great; ) {
                T ak = a.get(k);
                if (c.compare(ak, pivot1) < 0) {
                    a.swap(k, less++);
                }
                else if (c.compare(ak, pivot2) > 0) {
                    while (c.compare(a.get(great), pivot2) > 0) {
                        if (great-- == k) break outer;
                    }
                    if (c.compare(a.get(great), pivot1) < 0) {
                        a.set(k, a.get(less));
                        a.set(less, a.get(great));
                        ++less;
                    } else {
                        a.set(k, a.get(great));
                    }
                    a.set(great, ak);
                    --great;
                }
            }

            a.set(left, a.get(less - 1)); a.set(less - 1, pivot1);
            a.set(right, a.get(great + 1)); a.set(great + 1, pivot2);

            sort(a, left, less - 2, leftmost, c);
            sort(a, great + 2, right, false, c);

            if (less < e1 && e5 < great) {
                while (c.compare(a.get(less), pivot1) == 0) ++less;
                while (c.compare(a.get(great), pivot2) == 0) --great;

                outer: for (long k = less - 1; ++k <= great; ) {
                    T ak = a.get(k);
                    if (c.compare(ak, pivot1) == 0) {
                        a.swap(k, less++);
                    }
                    else if (c.compare(ak, pivot2) == 0) {
                        while (c.compare(a.get(great), pivot2) == 0) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (c.compare(a.get(great), pivot1) == 0) {
                            a.set(k, a.get(less));
                            a.set(less, a.get(great));
                            ++less;
                        } else {
                            a.set(k, a.get(great));
                        }
                        a.set(great, ak);
                        --great;
                    }
                }
            }

            sort(a, less, great, false, c);

        } else {
            T pivot = a.get(e3);

            for (long k = less; k <= great; ++k) {
                if (c.compare(a.get(k), pivot) == 0) {
                    continue;
                }
                T ak = a.get(k);
                if (c.compare(ak, pivot) < 0) {
                    a.swap(k, less++);
                } else {
                    while (c.compare(a.get(great), pivot) > 0) {
                        --great;
                    }
                    if (c.compare(a.get(great), pivot) < 0) {
                        a.set(k, a.get(less));
                        a.set(less, a.get(great));
                        ++less;
                    } else {
                        a.set(k, a.get(great));
                    }
                    a.set(great, ak);
                    --great;
                }
            }

            sort(a, left, less - 1, leftmost, c);
            sort(a, great + 1, right, false, c);
        }
    }
}
