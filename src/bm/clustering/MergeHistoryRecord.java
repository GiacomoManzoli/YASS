package bm.clustering;

/**
 * Classe che descrive un operazione di merge effettuata dall'algoritmo di clustering.
 * */
public class MergeHistoryRecord {

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
}
