package bm.clustering;

public class MinDistancePair {

    static MinDistancePair min(MinDistancePair p1, MinDistancePair p2){
        if (p1.dist < p2.dist){
            return p1;
        } else {
            return p2;
        }
    }

    private int r;
    private int s;
    private float dist;

    MinDistancePair(int r, int s, float dist) {
        this.r = r;
        this.s = s;
        this.dist = dist;
    }

    int getR() {
        return r;
    }

    int getS() {
        return s;
    }

    float getDist() {
        return dist;
    }

    int compareTo(MinDistancePair aPair) {
        /*
            a negative int if this < that
            0 if this == that
            a positive int if this > that
        * */
        if (this.dist < aPair.dist){
            return -1;
        }
        if (this.dist == aPair.dist){
            return 0;
        }
        // this.dist > aPair.dist
        return 1;
    }

    @Override
    public String toString() {
        return "MinDistancePair{" +
                "r=" + r +
                ", s=" + s +
                ", dist=" + dist +
                '}';
    }
}
