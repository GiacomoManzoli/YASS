package bm.clustering;

/**
 * Created by gmanzoli on 18/12/16.
 */
public class MergeHistoryRecord {

    private int c1;
    private int c2;
    private float dist;
    private int cnt;

    public MergeHistoryRecord(int c1, int c2, float dist, int cnt) {
        this.c1 = c1;
        this.c2 = c2;
        this.dist = dist;
        this.cnt = cnt;
    }

    public int getC1() {
        return c1;
    }

    public int getC2() {
        return c2;
    }

    public float getDist() {
        return dist;
    }

    public int getCnt() {
        return cnt;
    }

    @Override
    public String toString() {
        return "MergeHistoryRecord{" +
                "c1=" + c1 +
                ", c2=" + c2 +
                ", dist=" + dist +
                ", cnt=" + cnt +
                '}';
    }
}
