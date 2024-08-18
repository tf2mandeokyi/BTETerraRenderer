// shamelessly copied from https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/share/classes/java/util/TimSort.java

package com.mndk.bteterrarenderer.datatype.pointer;

import java.util.Comparator;

class PointerTimSort<T> {

    private static final long MIN_MERGE = 32;
    private static final int MIN_GALLOP = 7;
    private static final int INITIAL_TMP_STORAGE_LENGTH = 256;

    private final Pointer<T> a;
    private final long length;
    private final Comparator<? super T> c;

    private Pointer<T> tmp;
    private long tmpBase;
    private long tmpLength;
    private int minGallop = MIN_GALLOP;

    private int stackSize = 0;
    private final long[] runBase;
    private final long[] runLen;

    private PointerTimSort(Pointer<T> a, long length, Comparator<? super T> c,
                           Pointer<T> work, long workBase, long workLength) {
        this.a = a;
        this.length = length;
        this.c = c;

        long tlen = (length < 2 * INITIAL_TMP_STORAGE_LENGTH) ? length >>> 1 : INITIAL_TMP_STORAGE_LENGTH;
        if (work == null || workLength < tlen || workBase + tlen > workLength) {
            tmp = a.getType().newArray(tlen);
            tmpBase = 0;
            tmpLength = tlen;
        }
        else {
            tmp = work;
            tmpBase = workBase;
            tmpLength = workLength;
        }

        int stackLen = (length < 120 ? 5 : length < 1542 ? 10 : length < 119151 ? 19 : 40);
        runBase = new long[stackLen];
        runLen = new long[stackLen];
    }

    static <T> void sort(Pointer<T> a, long low, long high, Comparator<? super T> c,
                         Pointer<T> work, long workBase, long workLength) {
        long nRemaining = high - low;
        if (nRemaining < 2) return;

        if (nRemaining < MIN_MERGE) {
            long initRunLen = countRunAndMakeAscending(a, low, high, c);
            binarySort(a, low, high, low + initRunLen, c);
            return;
        }

        PointerTimSort<T> ts = new PointerTimSort<>(a, nRemaining, c, work, workBase, workLength);
        long minRun = minRunLength(nRemaining);
        do {
            long runLen = countRunAndMakeAscending(a, low, high, c);
            if (runLen < minRun) {
                long force = Math.min(nRemaining, minRun);
                binarySort(a, low, low + force, low + runLen, c);
                runLen = force;
            }

            ts.pushRun(low, runLen);
            ts.mergeCollapse();

            low += runLen;
            nRemaining -= runLen;
        } while (nRemaining != 0);

        assert low == high;
        ts.mergeForceCollapse();
        assert ts.stackSize == 1;
    }

    private static <T> void binarySort(Pointer<T> a, long low, long high, long start, Comparator<? super T> c) {
        assert low <= start && start <= high;
        if(start == low) start++;

        for(; start < high; start++) {
            T pivot = a.get(start);

            long left = low;
            long right = start;
            assert left <= right;

            while(left < right) {
                long mid = (left + right) >>> 1;
                if(c.compare(pivot, a.get(mid)) < 0) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            assert left == right;

            long n = start - left;
            if(n >>> 32 == 0) switch ((int) n) {
                case 2: a.set(left + 2, a.get(left + 1));
                case 1: a.set(left + 1, a.get(left));
                    break;
                default: PointerHelper.copyMultiple(a.add(left), a.add(left + 1), n);
            } else {
                PointerHelper.copyMultiple(a.add(left), a.add(left + 1), n);
            }
            a.set(left, pivot);
        }
    }

    private static <T> long countRunAndMakeAscending(Pointer<T> a, long low, long high, Comparator<? super T> c) {
        assert low < high;
        long runHigh = low + 1;
        if(runHigh == high) return 1;

        if(c.compare(a.get(runHigh++), a.get(low)) < 0) {
            while(runHigh < high && c.compare(a.get(runHigh), a.get(runHigh - 1)) < 0) runHigh++;
            reverseRange(a, low, runHigh);
        } else {
            while(runHigh < high && c.compare(a.get(runHigh), a.get(runHigh - 1)) >= 0) runHigh++;
        }
        return runHigh - low;
    }

    private static <T> void reverseRange(Pointer<T> a, long low, long high) {
        high--;
        while(low < high) a.swap(low++, high--);
    }

    private static long minRunLength(long n) {
        assert n >= 0;
        long r = 0;
        while(n >= MIN_MERGE) {
            r |= (n & 1);
            n >>= 1;
        }
        return n + r;
    }

    private void pushRun(long runBase, long runLen) {
        this.runBase[stackSize] = runBase;
        this.runLen[stackSize] = runLen;
        stackSize++;
    }

    private void mergeCollapse() {
        while(stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) n--;
                mergeAt(n);
            } else if (runLen[n] <= runLen[n + 1]) {
                mergeAt(n);
            } else {
                break;
            }
        }
    }

    private void mergeForceCollapse() {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if(n > 0 && runLen[n - 1] < runLen[n + 1]) n--;
            mergeAt(n);
        }
    }

    private void mergeAt(int i) {
        assert stackSize >= 2;
        assert i >= 0;
        assert i == stackSize - 2 || i == stackSize - 3;

        long base1 = runBase[i];
        long len1 = runLen[i];
        long base2 = runBase[i + 1];
        long len2 = runLen[i + 1];
        assert len1 > 0 && len2 > 0;
        assert base1 + len1 == base2;

        runLen[i] = len1 + len2;
        if(i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        stackSize--;

        long k = gallopRight(a.get(base2), a, base1, len1, 0, c);
        assert k >= 0;
        base1 += k;
        len1 -= k;
        if (len1 == 0) return;

        len2 = gallopLeft(a.get(base1 + len1 - 1), a, base2, len2, len2 - 1, c);
        assert len2 >= 0;
        if (len2 == 0) return;

        // Merge remaining runs, using tmp array with min(len1, len2) elements
        if (len1 <= len2) mergeLow(base1, len1, base2, len2);
        else mergeHigh(base1, len1, base2, len2);
    }

    private static <T> long gallopLeft(T key, Pointer<T> a, long base, long length, long hint, Comparator<? super T> c) {
        assert 0 <= hint && hint < length;
        long lastOfs = 0;
        long ofs = 1;
        if (c.compare(key, a.get(base + hint)) > 0) {
            long maxOfs = length - hint;
            while (ofs < maxOfs && c.compare(key, a.get(base + hint + ofs)) > 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) ofs = maxOfs;
            }
            if (ofs > maxOfs) ofs = maxOfs;

            lastOfs += hint;
            ofs += hint;
        } else {
            long maxOfs = hint + 1;
            while (ofs < maxOfs && c.compare(key, a.get(base + hint - ofs)) <= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if(ofs <= 0) ofs = maxOfs;
            }
            if(ofs > maxOfs) ofs = maxOfs;

            long tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        }
        assert -1 <= lastOfs && lastOfs < ofs && ofs <= length;

        lastOfs++;
        while (lastOfs < ofs) {
            long m = lastOfs + ((ofs - lastOfs) >>> 1);

            if (c.compare(key, a.get(base + m)) > 0) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        assert lastOfs == ofs;
        return ofs;
    }

    private static <T> long gallopRight(T key, Pointer<T> a, long base, long length, long hint, Comparator<? super T> c) {
        assert 0 <= hint && hint < length;

        long ofs = 1;
        long lastOfs = 0;
        if (c.compare(key, a.get(base + hint)) < 0) {
            long maxOfs = hint + 1;
            while (ofs < maxOfs && c.compare(key, a.get(base + hint - ofs)) < 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) ofs = maxOfs;
            }
            if (ofs > maxOfs) ofs = maxOfs;

            long tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        } else {
            long maxOfs = length - hint;
            while (ofs < maxOfs && c.compare(key, a.get(base + hint + ofs)) >= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) ofs = maxOfs;
            }
            if (ofs > maxOfs) ofs = maxOfs;

            lastOfs += hint;
            ofs += hint;
        }
        assert -1 <= lastOfs && lastOfs < ofs && ofs <= length;

        lastOfs++;
        while (lastOfs < ofs) {
            long m = lastOfs + ((ofs - lastOfs) >>> 1);

            if (c.compare(key, a.get(base + m)) < 0) {
                ofs = m;
            } else {
                lastOfs = m + 1;
            }
        }
        assert lastOfs == ofs;
        return ofs;
    }

    private void mergeLow(long base1, long len1, long base2, long len2) {
        assert len1 > 0 && len2 > 0 && base1 + len1 == base2;

        Pointer<T> a = this.a;
        Pointer<T> tmp = ensureCapacity(len1);
        long cursor1 = 0;
        long cursor2 = base2;
        long dest = base1;
        PointerHelper.copyMultiple(a.add(base1), tmp, len1);

        a.set(dest++, a.get(cursor2++));
        if(--len2 == 0) {
            PointerHelper.copyMultiple(tmp.add(cursor1), a.add(dest), len1);
            return;
        }
        if(len1 == 1) {
            PointerHelper.copyMultiple(a.add(cursor2), a.add(dest), len2);
            a.set(dest + len2, tmp.get(cursor1));
            return;
        }

        Comparator<? super T> c = this.c;
        int minGallop = this.minGallop;
        outer: while (true) {
            long count1 = 0, count2 = 0;

            do {
                assert len1 > 1 && len2 > 0;
                if(c.compare(a.get(cursor2), tmp.get(cursor1)) < 0) {
                    a.set(dest++, a.get(cursor2++));
                    count2++;
                    count1 = 0;
                    if(--len2 == 0) break outer;
                } else {
                    a.set(dest++, tmp.get(cursor1++));
                    count1++;
                    count2 = 0;
                    if(--len1 == 1) break outer;
                }
            } while ((count1 | count2) < minGallop);

            do {
                assert len1 > 1 && len2 > 0;
                count1 = gallopRight(a.get(cursor2), tmp, cursor1, len1, 0, c);
                if (count1 != 0) {
                    PointerHelper.copyMultiple(tmp.add(cursor1), a.add(dest), count1);
                    dest += count1;
                    cursor1 += count1;
                    len1 -= count1;
                    if (len1 <= 1) break outer;
                }
                a.set(dest++, a.get(cursor2++));
                if (--len2 == 0) break outer;

                count2 = gallopLeft(tmp.get(cursor1), a, cursor2, len2, 0, c);
                if (count2 != 0) {
                    PointerHelper.copyMultiple(a.add(cursor2), a.add(dest), count2);
                    dest += count2;
                    cursor2 += count2;
                    len2 -= count2;
                    if (len2 == 0) break outer;
                }
                a.set(dest++, tmp.get(cursor1++));
                if (--len1 == 1) break outer;
                minGallop--;
            } while (count1 >= MIN_GALLOP | count2 >= MIN_GALLOP);

            if (minGallop < 0) minGallop = 0;
            minGallop += 2;
        }
        this.minGallop = Math.max(minGallop, 1);

        if (len1 == 1) {
            assert len2 > 0;
            PointerHelper.copyMultiple(a.add(cursor2), a.add(dest), len2);
            a.set(dest + len2, tmp.get(cursor1));
        } else if (len1 == 0) {
            throw new IllegalArgumentException("Comparison method violates its general contract!");
        } else {
            assert len2 == 0;
            PointerHelper.copyMultiple(tmp.add(cursor1), a.add(dest), len1);
        }
    }

    private void mergeHigh(long base1, long len1, long base2, long len2) {
        assert len1 > 0 && len2 > 0 && base1 + len1 == base2;

        Pointer<T> a = this.a;
        Pointer<T> tmp = ensureCapacity(len2);
        long tmpBase = this.tmpBase;
        PointerHelper.copyMultiple(a.add(base2), tmp.add(tmpBase), len2);

        long cursor1 = base1 + len1 - 1;
        long cursor2 = tmpBase + len2 - 1;
        long dest = base2 + len2 - 1;

        a.set(dest--, a.get(cursor1--));
        if (--len1 == 0) {
            PointerHelper.copyMultiple(tmp.add(tmpBase), a.add(dest - (len2 - 1)), len2);
            return;
        }
        if (len2 == 1) {
            dest -= len1;
            cursor1 -= len1;
            PointerHelper.copyMultiple(a.add(cursor1 + 1), a.add(dest + 1), len1);
            a.set(dest, tmp.get(cursor2));
            return;
        }

        Comparator<? super T> c = this.c;
        int minGallop = this.minGallop;
        outer: while (true) {
            long count1 = 0;
            long count2 = 0;

            do {
                assert len1 > 0 && len2 > 1;
                if (c.compare(tmp.get(cursor2), a.get(cursor1)) < 0) {
                    a.set(dest--, a.get(cursor1--));
                    count1++;
                    count2 = 0;
                    if(--len1 == 0) break outer;
                } else {
                    a.set(dest--, tmp.get(cursor2--));
                    count2++;
                    count1 = 0;
                    if(--len2 == 1) break outer;
                }
            } while ((count1 | count2) < minGallop);

            do {
                assert len1 > 0 && len2 > 1;
                count1 = len1 - gallopRight(tmp.get(cursor2), a, base1, len1, len1 - 1, c);
                if (count1 != 0) {
                    dest -= count1;
                    cursor1 -= count1;
                    len1 -= count1;
                    PointerHelper.copyMultiple(a.add(cursor1 + 1), a.add(dest + 1), count1);
                    if(len1 == 0) break outer;
                }
                a.set(dest--, tmp.get(cursor2--));
                if(--len2 == 1) break outer;

                count2 = len2 - gallopLeft(a.get(cursor1), tmp, tmpBase, len2, len2 - 1, c);
                if (count2 != 0) {
                    dest -= count2;
                    cursor2 -= count2;
                    len2 -= count2;
                    PointerHelper.copyMultiple(tmp.add(cursor2 + 1), a.add(dest + 1), count2);
                    if (len2 <= 1) break outer;
                }
                a.set(dest--, a.get(cursor1--));
                if(--len1 == 0) break outer;
                minGallop--;
            } while (count1 >= MIN_GALLOP | count2 >= MIN_GALLOP);

            if (minGallop < 0) minGallop = 0;
            minGallop += 2;
        }
        this.minGallop = Math.max(minGallop, 1);

        if (len2 == 1) {
            assert len1 > 0;
            dest -= len1;
            cursor1 -= len1;
            PointerHelper.copyMultiple(a.add(cursor1 + 1), a.add(dest + 1), len1);
            a.set(dest, tmp.get(cursor2));
        } else if (len2 == 0) {
            throw new IllegalArgumentException("Comparison method violates its general contract!");
        } else {
            assert len1 == 0;
            PointerHelper.copyMultiple(tmp, a.add(dest - (len2 - 1)), len2);
        }
    }

    private Pointer<T> ensureCapacity(long minCapacity) {
        if (tmpLength < minCapacity) {
            long newSize = minCapacity;
            newSize |= newSize >> 1;
            newSize |= newSize >> 2;
            newSize |= newSize >> 4;
            newSize |= newSize >> 8;
            newSize |= newSize >> 16;
            newSize |= newSize >> 32;
            newSize++;

            if (newSize < 0) {
                newSize = minCapacity;
            } else {
                newSize = Math.min(newSize, this.length >>> 1);
            }

            tmp = this.a.getType().newArray(newSize);
            tmpLength = newSize;
            tmpBase = 0;
        }
        return tmp;
    }
}
