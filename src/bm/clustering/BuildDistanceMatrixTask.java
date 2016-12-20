package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.concurrent.RecursiveAction;

public class BuildDistanceMatrixTask extends RecursiveAction {

    private static int SEQUENTIAL_THRESHOLD = 100000;

    static void buildDistanceMatrix(ClusterManager manager, DistanceMeasure d){
        // Cerco di bilanciare la soglia di split in base al numero di core disponibili sulla macchina
        int cores = Runtime.getRuntime().availableProcessors();
        SEQUENTIAL_THRESHOLD = (int)Math.ceil((double) manager.dist.length / (4.0*cores));
        ClusterManager.commonPool.invoke(new BuildDistanceMatrixTask(manager,d, 0, manager.dist.length));
    }

    private ClusterManager manager;
    private int start;
    private int end;
    private DistanceMeasure d;

    private BuildDistanceMatrixTask(ClusterManager manager, DistanceMeasure d, int start, int end) {
        this.manager = manager;
        this.start = start;
        this.end = end;
        this.d = d;
    }

    @Override
    protected void compute() {
        if(end - start <= SEQUENTIAL_THRESHOLD){
            // do sequential work
            for (int k = start; k < end; k++){
                int i = manager._i(k);
                int j = manager._j(k);
                manager.dist[k] =  manager.getCluster(i).distance(manager.getCluster(j), d);
            }

        } else {

            int mid = start + (end - start) / 2;
            BuildDistanceMatrixTask left  = new BuildDistanceMatrixTask(manager, d, start, mid);
            BuildDistanceMatrixTask right = new BuildDistanceMatrixTask(manager, d, mid, end);
            left.fork();
            right.compute();
            left.join();

        }
    }
}
