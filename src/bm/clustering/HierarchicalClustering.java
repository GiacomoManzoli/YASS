package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.ArrayList;
import java.util.List;


public class HierarchicalClustering {

    public static List<MergeHistoryRecord> calculateClusters(DistanceMeasure d, List<String> words){
        /*
            Effettua il clustering gerarchico agglomerativo a partire da una lista di parole (words) ordinata
            in oridine lessicografico.

            :param d: Funzione per il calcolo della distanza
            :param words: Lexicon
            :return: Sequenza di merge effettuati durante il clustering
         */
        int n = words.size();
        int printInterval = (int)Math.max(100, n*0.005);

        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < words.size(); i++){
            List<String> clusterWords = new ArrayList<>();
            clusterWords.add(words.get(i));
            clusters.add(new Cluster(i, clusterWords));
        }
        int nextId = n;

        ClusterManager manager = new ClusterManager(clusters, d);
        List<MergeHistoryRecord> historyRecords = new ArrayList<>();
        int cntIter = 0;
        long startTime = System.currentTimeMillis();
        while (manager.size() != 1){
            MinDistancePair minDistancePair = manager.findMinDistancePair();
            final int r = minDistancePair.getR();
            final int s = minDistancePair.getS();
            // minDistancePair.getR() < minDistancePair.getS()

            historyRecords.add(new MergeHistoryRecord(
                    manager.getCluster(r).getId(),
                    manager.getCluster(s).getId(),
                    minDistancePair.getDist(),
                    manager.size() -1)
            );
            Cluster merged = Cluster.merge(nextId, manager.getCluster(r), manager.getCluster(s));
            nextId++;
            manager.deleteClusters(r,s);
            manager.insert(merged);

            cntIter++;

            if (cntIter % (printInterval)  == 0) {
                System.out.println("Iterazione: " + cntIter + " numero di cluster presenti: "+ manager.size() + " - Tempo trascorso: "+ (System.currentTimeMillis() - startTime)/1000 + " s");
            }
        }

        return historyRecords;
    }
}
