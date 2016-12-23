package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe che implementa l'algoritmo di clustering agglomerativo gerarchico in complete linkage.
 * */
public class HierarchicalClustering {

    /**
     * Esegue il clustering gerarchico utilizzando come misura di distanza {@code d} e come parole quelle presenti
     * nella lista {@code words}.
     * @param d misura di distanza.
     * @param words lista di parole da clusterizzare. Deve essere ordinata in ordine lessicografico.
     * @return storico delle operazioni di merge effettuate dall'algoritmo.
     * */
    public static List<MergeHistoryRecord> calculateClusters(DistanceMeasure d, List<String> words){
        int n = words.size();
        int printInterval = (int)Math.max(100, n*0.005);

        // Crea n cluster, ognuno contenente una parola
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < words.size(); i++){
            List<String> clusterWords = new ArrayList<>();
            clusterWords.add(words.get(i));
            clusters.add(new Cluster(i, clusterWords));
        }
        int nextId = n;

        // Crea il cluster manager, l'oggetto che contiene la matrice delle distanze.
        ClusterManager manager = new ClusterManager(clusters, d);

        List<MergeHistoryRecord> historyRecords = new ArrayList<>();
        int cntIter = 0;
        long startTime = System.currentTimeMillis();
        while (manager.size() != 1){
            // Cerca le coppie di cluster a distanza minima
            List<MinDistancePair> minDistancePairs = manager.findMinDistancePairs();

            // Tengo traccia dei cluster che ho mergiato in questa iterazione
            // Questo perché lo stesso cluster può comparire in più coppie a distanza minima
            Set<Integer> mergedCluster = new HashSet<>();
            List<Cluster> newClusters = new ArrayList<>();

            // Effettua il merge di tutte le coppie a distanza minima
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

            // Cancella dal manager i cluster che sono stati mergiati tra loro.
            List<Integer> toDelete = new ArrayList<>();
            toDelete.addAll(mergedCluster);
            manager.deleteClusters(toDelete);

            // Aggiunge al manager i nuovi cluster.
            //
            // IMPORTANTE: prima di effettuare l'inserimento devono essere stati eliminati i vecchi cluster, altrimenti
            //             non c'è posto all'interno della matrice delle distanze del manager.
            //
            for (Cluster c : newClusters) {
                manager.insert(c);
            }

            // Per questa iterazione ho finito tutte le delete ed insert, posso quindi effettuare
            // il resize della matrice e provare a liberare un po' di memoria.
            //
            // IMPORTANTE: questa operazione deve essere fatta DOPO tutte le cancellazioni e insierimenti
            //
            manager.resize();

            cntIter++;
            if (cntIter % (printInterval)  == 0) {
                System.out.println("Iterazione: " + cntIter + " numero di cluster presenti: "+ manager.size() +
                        " - Tempo trascorso: "+ (System.currentTimeMillis() - startTime)/1000 + " s");
            }
        }
        System.out.println("Iterazioni necessarie: "+cntIter);
        return historyRecords;
    }
}
