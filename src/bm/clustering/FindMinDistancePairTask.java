package bm.clustering;

import java.util.concurrent.RecursiveTask;

public class FindMinDistancePairTask extends RecursiveTask<MinDistancePair>{
    private static int SEQUENTIAL_THRESHOLD = 100000;

    private ClusterManager manager;
    private int start;
    private int end;

    private FindMinDistancePairTask(ClusterManager manager, int start, int end) {
        this.manager = manager;
        this.start = start;
        this.end = end;
    }

    @Override
    protected MinDistancePair compute() {
        if(end - start <= SEQUENTIAL_THRESHOLD){
            // do sequential work
            float minDist = Float.POSITIVE_INFINITY;
            int r = -1;
            int s = -1;  // Coppia di cluster a distanza minima

            float[] distances = manager.dist;
            for (int k = start; k < end; k++){
                if (distances[k] <= minDist){ // <= perché la distanza può essere infinita!
                    minDist = distances[k];
                    r = manager._i(k);
                    s = manager._j(k);
                }
            }
            // Devo stare attento all'ordine in cui cancello i dati
            // perché dopo che ho tolto un elemento l'indice dell'altro cambia.
            // assum0 che r sia l'indice minore
            if (s < r) {
                int t = r;
                r = s;
                s = t;
            }
            return new MinDistancePair(r,s, minDist);
        } else {
            // Troppo lavoro, lo divido!
            int mid = start + (end - start) / 2;
            FindMinDistancePairTask left  = new FindMinDistancePairTask(manager, start, mid);
            FindMinDistancePairTask right = new FindMinDistancePairTask(manager, mid, end);
            left.fork();
            MinDistancePair rightAns = right.compute();
            MinDistancePair leftAns  = left.join();
            return MinDistancePair.min(rightAns, leftAns);
        }
    }

    static MinDistancePair findMinDistancePair(ClusterManager manager) {
        // Da notare
        int n = manager.size();
        int last = (n * (n-1))/2;
        // Cerco di bilanciare la soglia di split in base al numero di core disponibili sulla macchina
        int cores = Runtime.getRuntime().availableProcessors();
        SEQUENTIAL_THRESHOLD = (int)Math.ceil((double) last / (4.0*cores));

        return ClusterManager.commonPool.invoke(new FindMinDistancePairTask(manager, 0, last));
    }
}
