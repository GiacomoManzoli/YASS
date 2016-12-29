package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Classe che implementa il calcolo parallelo della distanza in complete linkage tra due cluster secondo una strategia
 * divide-et-impera.
 * La divisione viene effettauta enumerando tutte le coppie di parole da considerare.
 * */
public class ClusterDistanceTask extends RecursiveTask<Float> {

    private static long SEQUENTIAL_THRESHOLD = 5000;

    static float calculateClusterDistance(Cluster c1, Cluster c2, DistanceMeasure d) {
        // Da notare
        int n1 = c1.getWords().size();
        int n2 = c2.getWords().size();
        long last = n1 * n2;
        // Cerco di bilanciare la soglia di split in base al numero di core disponibili sulla macchina
        int cores = Runtime.getRuntime().availableProcessors();
        SEQUENTIAL_THRESHOLD = (long)Math.ceil((double) last / (4.0*cores));
        return ClusterManager.commonPool.invoke(new ClusterDistanceTask(c1, c2, d, 0, last));
    }

    private Cluster c1;
    private Cluster c2;
    private long start;
    private long end;
    private DistanceMeasure d;
    private int n2;

    private ClusterDistanceTask(Cluster c1, Cluster c2, DistanceMeasure d, long start, long end) {
        this.c1 = c1;
        this.c2 = c2;
        this.start = start;
        this.end = end;
        this.d = d;
        this.n2 = c2.getWords().size();
        /*
        * Per suddividere il lavoro tra più thread enumero le coppie
        * di parole allo stesso modo in cui enumero i cluster in ClusterManager
        * */
    }

    private int _i(long k){
        return (int)(k / n2); // divisione intera
    }

    private int _j(long k){
        return (int)(k % n2); // resto
    }

    @Override
    protected Float compute() {
        if(end - start <= SEQUENTIAL_THRESHOLD){
            // do sequential work
            float maxDist = 0;

            List<String> words1 = c1.getWords();
            List<String> words2 = c2.getWords();
            for (long k = start; k < end; k++){
                int i = _i(k);
                int j = _j(k);
                float distance = d.calculate(words1.get(i), words2.get(j));
                if (distance > maxDist){
                    maxDist = distance;
                }
            }
            return maxDist;
        } else {
            // Troppo lavoro, lo divido!
            long mid = start + (end - start) / 2;
            ClusterDistanceTask left  = new ClusterDistanceTask(c1, c2, d, start, mid);
            ClusterDistanceTask right = new ClusterDistanceTask(c1, c2, d, mid, end);
            left.fork();
            float rightAns = right.compute();
            float leftAns  = left.join();
            return Math.max(rightAns, leftAns);
        }
    }
}
