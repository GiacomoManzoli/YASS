package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
            List<MinDistancePair> minDistancePairs = manager.findMinDistancePairs();
            // indici dei cluster che ho mergiato in questa iterazione
            // Questo perché lo stesso cluster può comparire in più coppie a distanza minima
            Set<Integer> mergedCluster = new HashSet<>();
            List<Cluster> newClusters = new ArrayList<>();

            for (MinDistancePair pair: minDistancePairs) {
                int r = pair.getR();
                int s = pair.getS();
                if (mergedCluster.contains(r) || mergedCluster.contains(s)){
                    continue; // Se uno dei due indici è già stato mergiato, salto la coppia
                }
                // r < s
                newClusters.add(Cluster.merge(nextId, manager.getCluster(r), manager.getCluster(s)));
                mergedCluster.add(r);
                mergedCluster.add(s);
                nextId++;

                historyRecords.add(new MergeHistoryRecord(
                        manager.getCluster(r).getId(),
                        manager.getCluster(s).getId(),
                        pair.getDist(),
                        manager.size() - newClusters.size())
                );


            }

            List<Integer> toDelete = new ArrayList<>();
            toDelete.addAll(mergedCluster);
            manager.deleteClusters(toDelete);

            for (Cluster c : newClusters) {
                manager.insert(c);
            }

            cntIter++;

            if (cntIter % (printInterval)  == 0) {
                System.out.println("Iterazione: " + cntIter + " numero di cluster presenti: "+ manager.size() + " - Tempo trascorso: "+ (System.currentTimeMillis() - startTime)/1000 + " s");
            }
        }

        return historyRecords;
    }
}
