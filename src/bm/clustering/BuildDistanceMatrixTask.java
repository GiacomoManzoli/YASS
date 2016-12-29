package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.concurrent.RecursiveAction;


/**
 * Classe che implementa il calcolo parallelo della matrice delle distanze secondo una strategia divede-et-impera
 * */
public class BuildDistanceMatrixTask extends RecursiveAction {

    private static long SEQUENTIAL_THRESHOLD = 100000;

    static void buildDistanceMatrix(ClusterManager manager, DistanceMeasure d){
        // Cerco di bilanciare la soglia di split in base al numero di core disponibili sulla macchina
        int cores = Runtime.getRuntime().availableProcessors();
        SEQUENTIAL_THRESHOLD = (long)Math.ceil((double) manager.dist.getSize() / (4.0*cores));
        ClusterManager.commonPool.invoke(new BuildDistanceMatrixTask(manager,d, 0, manager.dist.getSize()));
    }

    private ClusterManager manager;
    private long start;
    private long end;
    private DistanceMeasure d;

    private BuildDistanceMatrixTask(ClusterManager manager, DistanceMeasure d, long start, long end) {
        this.manager = manager;
        this.start = start;
        this.end = end;
        this.d = d;
    }

    @Override
    protected void compute() {
        if(end - start <= SEQUENTIAL_THRESHOLD){
            // do sequential work
            for (long k = start; k < end; k++){
                int i = manager._i(k);
                int j = manager._j(k);
                manager.dist.set(k, manager.getCluster(i).distance(manager.getCluster(j), d));
            }
        } else {
            long mid = start + (end - start) / 2;
            BuildDistanceMatrixTask left  = new BuildDistanceMatrixTask(manager, d, start, mid);
            BuildDistanceMatrixTask right = new BuildDistanceMatrixTask(manager, d, mid, end);
            left.fork();
            right.compute();
            left.join();
        }
    }
}
