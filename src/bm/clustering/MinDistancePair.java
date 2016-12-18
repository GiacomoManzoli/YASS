package bm.clustering;

/**
 * Created by gmanzoli on 18/12/16.
 */
public class MinDistancePair {
    private int r;
    private int s;
    private float dist;

    public MinDistancePair(int r, int s, float dist) {
        this.r = r;
        this.s = s;
        this.dist = dist;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }

    public float getDist() {
        return dist;
    }
}
