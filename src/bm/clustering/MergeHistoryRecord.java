package bm.clustering;

/**
 * Classe che descrive un operazione di merge effettuata dall'algoritmo di clustering.
 * */
public class MergeHistoryRecord implements Comparable{

    private int c1;
    private int c2;
    private float dist;
    private int cnt;

    /**
     * Costruisce un nuovo record.
     * @param c1 id del primo cluster mergiato.
     * @param c2 id del secondo cluster mergiato.
     * @param dist distanza dei due cluster.
     * @param cnt numero di cluster rimanenti dopo l'operazione di merge.
     * */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MergeHistoryRecord that = (MergeHistoryRecord) o;

        if (c1 != that.c1) return false;
        if (c2 != that.c2) return false;
        return Float.compare(that.dist, dist) == 0 && cnt == that.cnt;
    }

    @Override
    public int compareTo(Object o) {
        MergeHistoryRecord record = (MergeHistoryRecord) o;
         /*
            a negative int if this < that
            0 if this == that
            a positive int if this > that
        * */
        if (this.equals(record)){
            return 0;
        }
        if (this.dist <= record.dist) {
            return -1;
        } else {
            return 1;
        }
    }

    public void setC1(Integer c1) {
        this.c1 = c1;
    }
    public void setC2(Integer c2) {
        this.c2 = c2;
    }
}
