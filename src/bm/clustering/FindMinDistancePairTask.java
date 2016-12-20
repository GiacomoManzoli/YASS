package bm.clustering;

import java.util.concurrent.RecursiveTask;

public class FindMinDistancePairTask extends RecursiveTask<MinDistancePair>{
    private static long SEQUENTIAL_THRESHOLD = 100000;

    private ClusterManager manager;
    private long start;
    private long end;

    private FindMinDistancePairTask(ClusterManager manager, long start, long end) {
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

            MyCustomBigArray distances = manager.dist;
            for (long k = start; k < end; k++){
                if (distances.get(k) <= minDist){ // <= perché la distanza può essere infinita!
                    minDist = distances.get(k);
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
            long mid = start + (end - start) / 2;
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
        long n = manager.size();
        long last = (n * (n-1))/(long)2;
        // Cerco di bilanciare la soglia di split in base al numero di core disponibili sulla macchina
        int cores = Runtime.getRuntime().availableProcessors();
        SEQUENTIAL_THRESHOLD = (long)Math.ceil((double) last / (4.0*cores));

        return ClusterManager.commonPool.invoke(new FindMinDistancePairTask(manager, 0, last));
    }
}
